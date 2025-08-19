package com.ozantok.ecommerce.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items")
    fun observeCart(): Flow<List<CartItemEntity>>

    @Query("SELECT SUM(quantity) FROM cart_items")
    fun observeCount(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :newQuantity WHERE productId = :id")
    suspend fun updateQuantity(id: String, newQuantity: Int)

    @Query("DELETE FROM cart_items WHERE productId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearAll()

    @Query("SELECT quantity FROM cart_items WHERE productId = :id")
    suspend fun getQuantity(id: String): Int?

    @Query("UPDATE cart_items SET quantity = quantity + 1 WHERE productId = :id")
    suspend fun inc(id: String): Int
}