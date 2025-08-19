package com.ozantok.ecommerce.data.local.db


import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface ProductsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clearAll()

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun pagingAll(query: String): PagingSource<Int, ProductEntity>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' AND brand IN (:brands) ORDER BY createdAt DESC")
    fun pagingBrand(query: String, brands: List<String>): PagingSource<Int, ProductEntity>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' AND model IN (:models) ORDER BY createdAt DESC")
    fun pagingModel(query: String, models: List<String>): PagingSource<Int, ProductEntity>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' AND brand IN (:brands) AND model IN (:models) ORDER BY createdAt DESC")
    fun pagingBrandModel(
        query: String,
        brands: List<String>,
        models: List<String>
    ): PagingSource<Int, ProductEntity>

    @Query("SELECT DISTINCT brand FROM products ORDER BY brand")
    suspend fun getDistinctBrands(): List<String>

    @Query("SELECT DISTINCT model FROM products ORDER BY model")
    suspend fun getDistinctModels(): List<String>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun observeById(id: String): kotlinx.coroutines.flow.Flow<ProductEntity?>

    @Query(
        """
    SELECT * FROM products
    WHERE
        (:q IS NULL OR :q = '' OR name LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%')
      AND (:brandsEmpty = 1 OR brand IN (:brands))
      AND (:modelsEmpty = 1 OR model IN (:models))
    ORDER BY
        /* DATE_NEW_TO_OLD  */ CASE WHEN :sort = 'DATE_NEW_TO_OLD'  THEN createdAt END DESC,
        /* DATE_OLD_TO_NEW  */ CASE WHEN :sort = 'DATE_OLD_TO_NEW'  THEN createdAt END ASC,
        /* PRICE_HIGH_TO_LOW*/ CASE WHEN :sort = 'PRICE_HIGH_TO_LOW' THEN price     END DESC,
        /* PRICE_LOW_TO_HIGH*/ CASE WHEN :sort = 'PRICE_LOW_TO_HIGH' THEN price     END ASC,
        /* stable secondary key */ id ASC
"""
    )
    fun pagingFiltered(
        q: String?,
        brands: List<String>,
        models: List<String>,
        brandsEmpty: Int,
        modelsEmpty: Int,
        sort: String
    ): PagingSource<Int, ProductEntity>
}