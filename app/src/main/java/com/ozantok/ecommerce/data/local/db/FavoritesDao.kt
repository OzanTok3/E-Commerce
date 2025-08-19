package com.ozantok.ecommerce.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE productId = :id")
    suspend fun delete(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE productId = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Query("SELECT productId FROM favorites")
    fun observeIds(): Flow<List<String>>

    @Query(
        """
        SELECT p.* FROM products p
        INNER JOIN favorites f ON f.productId = p.id
    """
    )
    fun observeFavoriteProducts(): Flow<List<ProductEntity>>
}