package com.ozantok.ecommerce.ui.cart

import com.ozantok.ecommerce.data.local.db.CartItemEntity
import com.ozantok.ecommerce.data.repository.CartRepository
import com.ozantok.ecommerce.rules.MainDispatcherRule
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    @Test
    fun `derived flows - count and total`() = runTest {
        val flow = MutableStateFlow<List<CartItemEntity>>(emptyList())
        val repo = mock<CartRepository>()
        whenever(repo.observeCart()).thenReturn(flow)

        val vm = CartViewModel(repo)
        advanceUntilIdle()

        withTimeout(5.seconds) {
            // initial (sadece tüket; assert’e zorlamıyoruz)
            vm.count.first()
            vm.total.first()

            // güncelle
            flow.value = listOf(
                CartItemEntity("p1", "A", "", 100.0, 2),
                CartItemEntity("p2", "B", "", 50.0, 1)
            )

            // beklenen nihai değerlere gelmesini bekle
            assertEquals(3, vm.count.first { it == 3 })
            assertEquals(250.0, vm.total.first { it == 250.0 })
        }
    }

    @Test
    fun `addOne delegates to repo`() = runTest {
        val repo = mock<CartRepository>()
        whenever(repo.observeCart()).thenReturn(MutableStateFlow(emptyList()))
        val vm = CartViewModel(repo)

        vm.addOne("id", "Name", "img", 9.9)
        verify(repo).addOne(eq("id"), eq("Name"), eq("img"), eq(9.9))
    }

    @Test
    fun `setQuantity zero - deletes`() = runTest {
        val repo = mock<CartRepository>()
        whenever(repo.observeCart()).thenReturn(MutableStateFlow(emptyList()))
        val vm = CartViewModel(repo)

        vm.setQuantity("id", 0)
        verify(repo).deleteById(eq("id"))
    }

    @Test
    fun `setQuantity positive - updates`() = runTest {
        val repo = mock<CartRepository>()
        whenever(repo.observeCart()).thenReturn(MutableStateFlow(emptyList()))
        val vm = CartViewModel(repo)

        vm.setQuantity("id", 5)
        verify(repo).updateQuantity(eq("id"), eq(5))
    }
}
