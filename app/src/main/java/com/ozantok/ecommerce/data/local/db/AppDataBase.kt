package com.ozantok.ecommerce.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class, CartItemEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productsDao(): ProductsDao
    abstract fun cartDao(): CartDao
    abstract fun favoritesDao(): FavoritesDao
}