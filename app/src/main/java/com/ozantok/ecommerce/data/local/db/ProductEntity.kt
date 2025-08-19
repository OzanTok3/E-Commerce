package com.ozantok.ecommerce.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "products",
    indices = [Index(value = ["brand"]), Index(value = ["model"]), Index(value = ["name"])]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val createdAt: String,
    val name: String,
    val image: String,
    val price: String,
    val description: String,
    val model: String,
    val brand: String
) {
    val priceValue: Double get() = price.toDoubleOrNull() ?: 0.0
}