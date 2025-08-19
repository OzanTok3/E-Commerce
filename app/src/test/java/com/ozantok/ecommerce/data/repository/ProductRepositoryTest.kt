package com.ozantok.ecommerce.data.repository

import com.ozantok.ecommerce.data.local.db.AppDatabase
import com.ozantok.ecommerce.data.local.db.ProductEntity
import com.ozantok.ecommerce.data.local.db.ProductsDao
import com.ozantok.ecommerce.data.remote.api.ApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProductRepositoryTest {

    @Test
    fun `getProductById delegates to dao`() = runTest {
        val dao = mock<ProductsDao> {
            onBlocking { getById("p1") } doReturn
                    ProductEntity(
                        id = "p1", name = "N", image = "img",
                        description = "d", price = "10,00",
                        brand = "B", model = "M",
                        createdAt = "2023-07-17T07:21:02.529Z"
                    )
        }
        val db = mock<AppDatabase> { on { productsDao() } doReturn dao }
        val api = mock<ApiService>()
        val repo = ProductRepository(api, db)

        val p = repo.getProductById("p1")
        verify(dao).getById("p1")
        assert(p?.id == "p1")
    }

    @Test
    fun `observeProductById forwards flow from dao`() = runTest {
        val product = ProductEntity(
            id = "p2", name = "N2", image = "img",
            description = "d", price = "20,00",
            brand = "B", model = "M",
            createdAt = "2023-07-17T07:21:02.529Z"
        )
        val dao = mock<ProductsDao> {
            on { observeById("p2") } doReturn flowOf(product)
        }
        val db = mock<AppDatabase> { on { productsDao() } doReturn dao }
        val api = mock<ApiService>()
        val repo = ProductRepository(api, db)

        val emitted = repo.observeProductById("p2").first()
        assert(emitted == product)
        verify(dao).observeById("p2")
    }

    @Test
    fun `getDistinctBrands and getDistinctModels delegate to dao`() = runTest {
        val dao = mock<ProductsDao> {
            onBlocking { getDistinctBrands() } doReturn listOf("Apple", "Samsung")
            onBlocking { getDistinctModels() } doReturn listOf("A1", "B2")
        }
        val db = mock<AppDatabase> { on { productsDao() } doReturn dao }
        val api = mock<ApiService>()
        val repo = ProductRepository(api, db)

        val brands = repo.getDistinctBrands()
        val models = repo.getDistinctModels()

        verify(dao).getDistinctBrands()
        verify(dao).getDistinctModels()
        assert(brands == listOf("Apple", "Samsung"))
        assert(models == listOf("A1", "B2"))
    }
}
