package com.ozantok.ecommerce.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ozantok.ecommerce.data.local.db.AppDatabase
import com.ozantok.ecommerce.data.local.db.ProductEntity
import com.ozantok.ecommerce.data.remote.api.ApiService
import com.ozantok.ecommerce.ui.home.filter.SortOption
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val api: ApiService,
    private val db: AppDatabase
) {

    @OptIn(ExperimentalPagingApi::class)
    fun getPagedProducts(
        query: String,
        brands: List<String>,
        models: List<String>,
        sort: SortOption,
        pageSize: Int = 20
    ): Flow<PagingData<ProductEntity>> {
        val dao = db.productsDao()

        val pagingSourceFactory = {
            dao.pagingFiltered(
                q = query,
                brands = brands,
                models = models,
                brandsEmpty = if (brands.isEmpty()) 1 else 0,
                modelsEmpty = if (models.isEmpty()) 1 else 0,
                sort = sort.name
            )
        }

        return Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            remoteMediator = ProductRemoteMediator(api, db),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getPagedProducts(
        query: String,
        brands: List<String>,
        models: List<String>,
        pageSize: Int = 20
    ): Flow<PagingData<ProductEntity>> =
        getPagedProducts(
            query = query,
            brands = brands,
            models = models,
            sort = SortOption.DATE_OLD_TO_NEW,
            pageSize = pageSize
        )

    suspend fun getProductById(id: String): ProductEntity? =
        db.productsDao().getById(id)

    fun observeProductById(id: String): Flow<ProductEntity?> =
        db.productsDao().observeById(id)

    suspend fun getDistinctBrands(): List<String> =
        db.productsDao().getDistinctBrands()

    suspend fun getDistinctModels(): List<String> =
        db.productsDao().getDistinctModels()
}
