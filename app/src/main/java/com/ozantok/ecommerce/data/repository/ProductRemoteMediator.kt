package com.ozantok.ecommerce.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ozantok.ecommerce.data.local.db.AppDatabase
import com.ozantok.ecommerce.data.local.db.ProductEntity
import com.ozantok.ecommerce.data.remote.api.ApiService
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class ProductRemoteMediator(
    private val api: ApiService,
    private val db: AppDatabase
) : RemoteMediator<Int, ProductEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ProductEntity>
    ): MediatorResult {
        return try {
            if (loadType == LoadType.REFRESH) {
                val products = api.getProducts()
                db.withTransaction {
                    db.productsDao().clearAll()
                    db.productsDao().insertAll(products)
                }
            }
            MediatorResult.Success(endOfPaginationReached = true)

        } catch (ioe: IOException) {
            MediatorResult.Error(ioe)
        } catch (he: HttpException) {
            MediatorResult.Error(he)
        }
    }
}
