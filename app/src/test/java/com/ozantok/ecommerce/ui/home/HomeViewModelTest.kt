package com.ozantok.ecommerce.ui.home

import app.cash.turbine.test
import androidx.paging.PagingData
import com.ozantok.ecommerce.data.local.db.ProductEntity
import com.ozantok.ecommerce.data.repository.ProductRepository
import com.ozantok.ecommerce.rules.MainDispatcherRule
import com.ozantok.ecommerce.ui.home.filter.SortOption
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    private lateinit var repo: ProductRepository
    private lateinit var vm: HomeViewModel

    @Before
    fun setup() {
        repo = mock()
        whenever(
            repo.getPagedProducts(
                any(), any(), any(),
                any(), any()
            )
        ).thenReturn(flowOf(PagingData.empty()))

        vm = HomeViewModel(repo)
    }

    @Test
    fun `init - pager built with default filters`() = runTest {
        verify(repo).getPagedProducts(
            eq(""),
            eq(emptyList()),
            eq(emptyList()),
            eq(SortOption.DATE_OLD_TO_NEW),
            eq(20)
        )
    }

    @Test
    fun `onSearchChanged - triggers pager rebuild with new query`() = runTest {
        clearInvocations(repo)
        whenever(
            repo.getPagedProducts(any(), any(), any(), any(), any())
        ).thenReturn(flowOf(PagingData.empty()))

        vm.onSearchChanged("phone")

        verify(repo).getPagedProducts(
            eq("phone"),
            eq(emptyList()),
            eq(emptyList()),
            eq(SortOption.DATE_OLD_TO_NEW),
            eq(20)
        )
    }

    @Test
    fun `updateFilters - uses sort+brand+model`() = runTest {
        clearInvocations(repo)
        val brands = listOf("Apple", "Samsung")
        val models = listOf("iPhone 15")
        vm.updateFilters(SortOption.PRICE_HIGH_TO_LOW, brands, models)

        verify(repo).getPagedProducts(
            eq(""),
            eq(brands),
            eq(models),
            eq(SortOption.PRICE_HIGH_TO_LOW),
            eq(20)
        )

        vm.uiState.test {
            val s = awaitItem()
            assertEquals(SortOption.PRICE_HIGH_TO_LOW, s.filters.sort)
            assertEquals(brands.toSet(), s.filters.brands)
            assertEquals(models.toSet(), s.filters.models)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
