package com.ozantok.ecommerce.data.local.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class CartDaoTest : BaseRoomTest() {

    @get:Rule
    val instant = InstantTaskExecutorRule()

    private lateinit var dao: CartDao

    @Before
    fun init() {
        dao = db.cartDao()
    }

    private fun item(id: String, qty: Int = 1) = CartItemEntity(
        productId = id, name = "N", image = "img", unitPrice = 10.0, quantity = qty
    )

    @Test
    fun upsert_and_getQuantity() = runTest {
        dao.upsert(item("p1", qty = 2))
        assertEquals(2, dao.getQuantity("p1"))

        dao.upsert(item("p1", qty = 5))
        assertEquals(5, dao.getQuantity("p1"))
    }

    @Test
    fun updateQuantity_and_inc() = runTest {
        dao.upsert(item("p1", qty = 1))
        dao.updateQuantity("p1", 3)
        assertEquals(3, dao.getQuantity("p1"))

        val affected = dao.inc("p1")
        assertEquals(1, affected) // etkilenen satır sayısı
        assertEquals(4, dao.getQuantity("p1"))
    }

    @Test
    fun deleteById_and_clearAll() = runTest {
        dao.upsert(item("p1"))
        dao.upsert(item("p2"))
        dao.deleteById("p1")
        assertNull(dao.getQuantity("p1"))
        assertNotNull(dao.getQuantity("p2"))

        dao.clearAll()
        assertNull(dao.getQuantity("p2"))
    }

    @Test
    fun observeCart_and_observeCount() = runTest {
        // observeCart: initial’a hiç bakmıyoruz; sadece bizim insert sonrası beklenen büyüklüğe gelmesini bekliyoruz
        withTimeout(5.seconds) {
            dao.upsert(item("p1", 2))
            val afterP1 = dao.observeCart().first { it.size == 1 }
            assertEquals(1, afterP1.size)
            assertEquals(2, afterP1.first().quantity)

            dao.upsert(item("p2", 3))
            val afterP2 = dao.observeCart().first { it.size == 2 }
            assertEquals(2, afterP2.size)
        }

        // observeCount: null → 0 eşleyip, bizim insertlerden sonra hedef değeri bekle
        withTimeout(5.seconds) {
            // tablo şu an p1(2) + p2(3) olabilir; sayımı sıfırdan kurmak için temizleyip deterministik ilerleyelim
            dao.clearAll()

            val countFlow = dao.observeCount().map { it ?: 0 }

            dao.upsert(item("c1", 2))
            val c2 = countFlow.first { it == 2 }
            assertEquals(2, c2)

            dao.upsert(item("c2", 3))
            val c5 = countFlow.first { it == 5 }
            assertEquals(5, c5)
        }
    }
}
