package com.ozantok.ecommerce.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val name: String,
    val image: String,
    val unitPrice: Double,
    val quantity: Int = 1
)