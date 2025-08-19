package com.ozantok.ecommerce.data.local.db

import androidx.paging.PagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsDaoTest : BaseRoomTest() {

    private lateinit var dao: ProductsDao

    @Before
    fun init() {
        dao = db.productsDao()
    }

    private fun p(
        id: String,
        createdAt: String,
        name: String,
        brand: String,
        model: String,
        price: String,
        desc: String = "desc",
        image: String = "img"
    ) = ProductEntity(
        id = id,
        createdAt = createdAt,
        name = name,
        image = image,
        price = price,
        description = desc,
        model = model,
        brand = brand
    )

    @Test
    fun insertAll_and_getById() = runTest {
        val items = listOf(
            p("p1", "2025-08-01T10:00:00", "Alpha", "BrandA", "M1", "10.00"),
            p("p2", "2025-08-02T10:00:00", "Beta", "BrandB", "M2", "20.00")
        )
        dao.insertAll(items)

        val loaded1 = dao.getById("p1")
        assertNotNull(loaded1)
        assertEquals("Alpha", loaded1!!.name)

        val loaded2 = dao.getById("p2")
        assertEquals("Beta", loaded2!!.name)
    }

    @Test
    fun clearAll_removes_everything() = runTest {
        dao.insertAll(
            listOf(
                p("p1", "2025-08-01T10:00:00", "Alpha", "BrandA", "M1", "10.00")
            )
        )
        dao.clearAll()
        assertNull(dao.getById("p1"))
    }

    @Test
    fun distinct_brands_and_models() = runTest {
        dao.insertAll(
            listOf(
                p("p1", "2025-08-01T10:00", "Alpha", "BrandA", "M1", "10"),
                p("p2", "2025-08-02T10:00", "Beta", "BrandB", "M2", "20"),
                p("p3", "2025-08-03T10:00", "Gamma", "BrandA", "M2", "30")
            )
        )
        val brands = dao.getDistinctBrands()
        val models = dao.getDistinctModels()
        assertEquals(listOf("BrandA", "BrandB"), brands)
        assertEquals(listOf("M1", "M2"), models)
    }

    @Test
    fun observeById_emits() = runTest {

        val flow = dao.observeById("pX")
        assertNull(flow.first())

        dao.insertAll(listOf(p("pX", "2025-08-04T10:00", "X", "B", "M", "15.5")))
        val e = flow.first()
        assertNotNull(e)
        assertEquals("X", e!!.name)
    }

    @Test
    fun pagingAll_filters_by_name_and_orders_by_createdAt_desc() = runTest {
        dao.insertAll(
            listOf(
                p("p1", "2025-08-01T10:00", "Alpha One", "BrandA", "M1", "10"),
                p("p2", "2025-08-02T10:00", "Alpha Two", "BrandA", "M1", "12"),
                p("p3", "2025-07-31T10:00", "Beta", "BrandB", "M2", "9")
            )
        )
        val ps = dao.pagingAll("Alpha")
        val page = ps.load(
            PagingSource.LoadParams.Refresh(
                key = null, loadSize = 20, placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page
        val names = page.data.map { it.name }
        assertEquals(listOf("Alpha Two", "Alpha One"), names)
    }

    @Test
    fun pagingBrandModel_combined_filtering() = runTest {
        dao.insertAll(
            listOf(
                p("p1", "2025-08-01", "Phone X", "Acme", "L1", "100"),
                p("p2", "2025-08-02", "Phone Y", "Acme", "L2", "150"),
                p("p3", "2025-08-03", "Phone Z", "Bravo", "L2", "200")
            )
        )
        val ps = dao.pagingBrandModel(
            query = "Phone",
            brands = listOf("Acme"),
            models = listOf("L2")
        )
        val page = ps.load(
            PagingSource.LoadParams.Refresh(null, 20, false)
        ) as PagingSource.LoadResult.Page
        val ids = page.data.map { it.id }
        assertEquals(listOf("p2"), ids)
    }

    @Test
    fun pagingFiltered_various_sorts() = runTest {
        dao.insertAll(
            listOf(
                p("p1", "2025-08-01T09:00", "A", "B1", "M1", "10"),
                p("p2", "2025-08-02T09:00", "B", "B1", "M2", "30"),
                p("p3", "2025-08-03T09:00", "C", "B2", "M1", "20")
            )
        )

        suspend fun load(
            sort: String,
            brands: List<String> = emptyList(),
            models: List<String> = emptyList()
        ): List<String> {
            val page = dao.pagingFiltered(
                q = null,
                brands = brands,
                models = models,
                brandsEmpty = if (brands.isEmpty()) 1 else 0,
                modelsEmpty = if (models.isEmpty()) 1 else 0,
                sort = sort
            ).load(PagingSource.LoadParams.Refresh(null, 50, false)) as PagingSource.LoadResult.Page
            return page.data.map { it.id }
        }

        assertEquals(listOf("p3", "p2", "p1"), load("DATE_NEW_TO_OLD"))

        assertEquals(listOf("p1", "p2", "p3"), load("DATE_OLD_TO_NEW"))

        assertEquals(listOf("p2", "p3", "p1"), load("PRICE_HIGH_TO_LOW"))

        assertEquals(listOf("p1", "p3", "p2"), load("PRICE_LOW_TO_HIGH"))

        assertEquals(listOf("p2", "p1"), load("DATE_NEW_TO_OLD", brands = listOf("B1")))

        assertEquals(listOf("p3", "p1"), load("DATE_NEW_TO_OLD", models = listOf("M1")))
    }
}
