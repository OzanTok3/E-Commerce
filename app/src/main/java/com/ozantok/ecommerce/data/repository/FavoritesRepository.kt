package com.ozantok.ecommerce.data.repository

import com.ozantok.ecommerce.data.local.db.FavoriteEntity
import com.ozantok.ecommerce.data.local.db.FavoritesDao
import com.ozantok.ecommerce.data.local.db.ProductEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val dao: FavoritesDao
) {
    fun observeFavoriteProducts(): Flow<List<ProductEntity>> = dao.observeFavoriteProducts()

    suspend fun toggle(id: String) {
        if (dao.isFavorite(id)) dao.delete(id) else dao.insert(FavoriteEntity(id))
    }
}