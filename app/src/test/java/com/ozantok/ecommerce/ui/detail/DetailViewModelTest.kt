package com.ozantok.ecommerce.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.ozantok.ecommerce.data.local.db.ProductEntity
import com.ozantok.ecommerce.data.repository.ProductRepository
import com.ozantok.ecommerce.rules.MainDispatcherRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    @Test
    fun `load success - sets product`() = runTest {
        val repo = mock<ProductRepository>()
        val product = ProductEntity(
            id = "p1",
            name = "Test",
            image = "https://loremflickr.com/640/480/food",
            description = "desc",
            price = "350,00",
            brand = "X",
            model = "Y",
            createdAt = "2023-07-17T07:21:02.529Z"
        )
        whenever(repo.getProductById("p1")).thenReturn(product)

        val vm = DetailViewModel(repo, SavedStateHandle(mapOf(DetailViewModel.ARG_PRODUCT_ID to "p1")))

        val st = vm.state.value
        assertEquals(false, st.isLoading)
        assertNotNull(st.product)
        assertEquals("p1", st.product?.id)
    }

    @Test
    fun `load null - sets error`() = runTest {
        val repo = mock<ProductRepository>()
        whenever(repo.getProductById("pX")).thenReturn(null)

        val vm = DetailViewModel(repo, SavedStateHandle(mapOf(DetailViewModel.ARG_PRODUCT_ID to "pX")))

        val st = vm.state.value
        assertEquals(false, st.isLoading)
        assertEquals("No product found.", st.error)
    }

    @Test
    fun `load throws - sets error with message`() = runTest {
        val repo = mock<ProductRepository>()
        whenever(repo.getProductById("boom")).thenAnswer { throw RuntimeException("network") }

        val vm = DetailViewModel(repo, SavedStateHandle(mapOf(DetailViewModel.ARG_PRODUCT_ID to "boom")))

        val st = vm.state.value
        assertEquals(false, st.isLoading)
        assertEquals("network", st.error)
    }
}
