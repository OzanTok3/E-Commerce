package com.ozantok.ecommerce.ui.favorites

import app.cash.turbine.test
import com.ozantok.ecommerce.data.local.db.ProductEntity
import com.ozantok.ecommerce.data.repository.FavoritesRepository
import com.ozantok.ecommerce.rules.MainDispatcherRule
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    @Test
    fun `favoriteIds maps from favoriteProducts`() = runTest {
        val flow = MutableStateFlow<List<ProductEntity>>(emptyList())
        val repo = mock<FavoritesRepository>()
        whenever(repo.observeFavoriteProducts()).thenReturn(flow)

        val vm = FavoritesViewModel(repo)

        vm.favoriteIds.test {
            assertEquals(emptySet<String>(), awaitItem())
            flow.value = listOf(
                ProductEntity(
                    id = "p1",
                    name = "A",
                    image = "https://loremflickr.com/640/480/food",
                    description = "desc",
                    price = "10,00",
                    brand = "B",
                    model = "M",
                    createdAt = "2023-07-17T07:21:02.529Z"
                ), ProductEntity(
                    id = "p2",
                    name = "B",
                    image = "https://loremflickr.com/640/480/food",
                    description = "desc",
                    price = "20,00",
                    brand = "C",
                    model = "N",
                    createdAt = "2023-07-17T07:21:02.529Z"
                )
            )
            assertEquals(setOf("p1", "p2"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle delegates to repo`() = runTest {
        val repo = mock<FavoritesRepository>()
        whenever(repo.observeFavoriteProducts()).thenReturn(MutableStateFlow(emptyList()))
        val vm = FavoritesViewModel(repo)

        vm.toggle("p1")
        verify(repo).toggle(eq("p1"))
    }
}
