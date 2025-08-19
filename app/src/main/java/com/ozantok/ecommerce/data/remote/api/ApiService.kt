package com.ozantok.ecommerce.data.remote.api


import com.ozantok.ecommerce.data.local.db.ProductEntity
import retrofit2.http.GET

interface ApiService {
    @GET("products")
    suspend fun getProducts(): List<ProductEntity>
}