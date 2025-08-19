package com.ozantok.ecommerce.data.local.db

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesDaoTest : BaseRoomTest() {

    private lateinit var favDao: FavoritesDao
    private lateinit var prodDao: ProductsDao

    @Before
    fun init() {
        favDao = db.favoritesDao()
        prodDao = db.productsDao()
    }

    private fun p(id: String, brand: String = "B", model: String = "M") = ProductEntity(
        id = id,
        createdAt = "2025-08-01T10:00:00",
        name = "Name$id",
        image = "img",
        price = "10.00",
        description = "desc",
        model = model,
        brand = brand
    )

    @Test
    fun insert_ignore_duplicates_and_isFavorite_delete() = runTest {
        assertFalse(favDao.isFavorite("p1"))
        favDao.insert(FavoriteEntity("p1"))
        assertTrue(favDao.isFavorite("p1"))

        favDao.insert(FavoriteEntity("p1"))
        assertTrue(favDao.isFavorite("p1"))

        favDao.delete("p1")
        assertFalse(favDao.isFavorite("p1"))
    }

    @Test
    fun observeIds_emits_changes() = runTest {
        favDao.observeIds().test {
            assertTrue(awaitItem().isEmpty())
            favDao.insert(FavoriteEntity("p1"))
            assertEquals(listOf("p1"), awaitItem())
            favDao.insert(FavoriteEntity("p2"))
            assertEquals(listOf("p1","p2"), awaitItem().sorted())
            favDao.delete("p1")
            assertEquals(listOf("p2"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeFavoriteProducts_join_with_products() = runTest {
        prodDao.insertAll(listOf(p("p1"), p("p2")))
        favDao.observeFavoriteProducts().test {
            assertTrue(awaitItem().isEmpty())
            favDao.insert(FavoriteEntity("p1"))
            val list1 = awaitItem()
            assertEquals(1, list1.size)
            assertEquals("p1", list1.first().id)

            favDao.insert(FavoriteEntity("p2"))
            val list2 = awaitItem()
            val ids = list2.map { it.id }.sorted()
            assertEquals(listOf("p1","p2"), ids)

            favDao.delete("p1")
            val list3 = awaitItem()
            assertEquals(listOf("p2"), list3.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
