package com.ozantok.ecommerce.data.repository

import com.ozantok.ecommerce.data.local.db.FavoriteEntity
import com.ozantok.ecommerce.data.local.db.FavoritesDao
import com.ozantok.ecommerce.data.local.db.ProductEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesRepositoryTest {

    @Test
    fun `toggle when already favorite then delete`() = runTest {
        val dao = mock<FavoritesDao> {
            onBlocking { isFavorite("p1") } doReturn true
        }
        val repo = FavoritesRepository(dao)

        repo.toggle("p1")

        verify(dao).isFavorite("p1")
        verify(dao).delete("p1")
        verify(dao, never()).insert(any())
    }

    @Test
    fun `toggle when not favorite then insert`() = runTest {
        val dao = mock<FavoritesDao> {
            onBlocking { isFavorite("p1") } doReturn false
        }
        val repo = FavoritesRepository(dao)

        repo.toggle("p1")

        verify(dao).isFavorite("p1")
        verify(dao).insert(eq(FavoriteEntity("p1")))
        verify(dao, never()).delete(any())
    }

    @Test
    fun `observeFavoriteProducts emits items from dao`() = runTest {
        val flow = MutableSharedFlow<List<ProductEntity>>(replay = 1)
        val dao = mock<FavoritesDao> {
            on { observeFavoriteProducts() } doReturn flow
        }
        val repo = FavoritesRepository(dao)

        val list = listOf(
            ProductEntity(
                id = "p1", name = "N1", image = "img",
                description = "d", price = "10,00", brand = "B",
                model = "M", createdAt = "2023-07-17T07:21:02.529Z"
            )
        )
        flow.tryEmit(list)

        val emitted = repo.observeFavoriteProducts().first()
        assert(emitted == list)
    }
}
