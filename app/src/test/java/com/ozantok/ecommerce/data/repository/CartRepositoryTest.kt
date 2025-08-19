package com.ozantok.ecommerce.data.repository

import com.ozantok.ecommerce.data.local.db.CartDao
import com.ozantok.ecommerce.data.local.db.CartItemEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class CartRepositoryTest {

    @Test
    fun `addOne when inc returns 1 then do not upsert`() = runTest {
        val dao = mock<CartDao> {
            onBlocking { inc("p1") } doReturn 1
        }
        val repo = CartRepository(dao)

        repo.addOne(id = "p1", name = "Name", image = "img", unitPrice = 9.99)

        verify(dao).inc("p1")
        verify(dao, never()).upsert(any())
    }

    @Test
    fun `addOne when inc returns 0 then upsert with quantity one`() = runTest {
        val dao = mock<CartDao> {
            onBlocking { inc("p1") } doReturn 0
        }
        val repo = CartRepository(dao)

        repo.addOne(id = "p1", name = "Name", image = "img", unitPrice = 9.99)

        verify(dao).inc("p1")
        val captor = argumentCaptor<CartItemEntity>()
        verify(dao).upsert(captor.capture())
        val inserted = captor.firstValue
        assert(inserted.productId == "p1")
        assert(inserted.name == "Name")
        assert(inserted.image == "img")
        assert(inserted.unitPrice == 9.99)
        assert(inserted.quantity == 1)
    }

    @Test
    fun `observeTotal sums unitPrice times quantity`() = runTest {
        val dao = mock<CartDao> {
            on { observeCart() } doReturn flowOf(
                listOf(
                    CartItemEntity("a", "A", "img", 10.0, 2),
                    CartItemEntity("b", "B", "img", 5.5,  3)
                )
            )
        }
        val repo = CartRepository(dao)

        val total = repo.observeTotal().first()
        assert(kotlin.math.abs((total ?: 0.0) - 36.5) < 0.0001)
    }

    @Test
    fun `updateQuantity and deleteById delegate to dao`() = runTest {
        val dao = mock<CartDao>()
        val repo = CartRepository(dao)

        repo.updateQuantity("x", 4)
        verify(dao).updateQuantity("x", 4)

        repo.deleteById("x")
        verify(dao).deleteById("x")
    }
}
