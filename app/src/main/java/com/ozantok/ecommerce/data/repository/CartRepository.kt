package com.ozantok.ecommerce.data.repository

import com.ozantok.ecommerce.data.local.db.CartDao
import com.ozantok.ecommerce.data.local.db.CartItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val cartDao: CartDao
) {
    fun observeCart() = cartDao.observeCart()
    fun observeCount(): Flow<Int?> = cartDao.observeCount()
    fun observeTotal(): Flow<Double?> =
        cartDao.observeCart().map { list -> list.sumOf { it.unitPrice * it.quantity } }

    suspend fun addOne(id: String, name: String, image: String, unitPrice: Double) {
        val affected = cartDao.inc(id)
        if (affected == 0) {
            cartDao.upsert(
                CartItemEntity(
                    productId = id,
                    name = name,
                    image = image,
                    unitPrice = unitPrice,
                    quantity = 1
                )
            )
        }
    }

    suspend fun upsert(item: CartItemEntity) = cartDao.upsert(item)
    suspend fun updateQuantity(id: String, newQuantity: Int) =
        cartDao.updateQuantity(id, newQuantity)

    suspend fun deleteById(id: String) = cartDao.deleteById(id)
    suspend fun clearAll() = cartDao.clearAll()
}