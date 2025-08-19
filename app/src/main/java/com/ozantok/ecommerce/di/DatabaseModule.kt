package com.ozantok.ecommerce.di

import android.content.Context
import androidx.room.Room
import com.ozantok.ecommerce.data.local.db.AppDatabase
import com.ozantok.ecommerce.data.local.db.CartDao
import com.ozantok.ecommerce.data.local.db.FavoritesDao
import com.ozantok.ecommerce.data.local.db.ProductsDao
import com.ozantok.ecommerce.data.remote.api.ApiService
import com.ozantok.ecommerce.data.repository.CartRepository
import com.ozantok.ecommerce.data.repository.FavoritesRepository
import com.ozantok.ecommerce.data.repository.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ecommerce.db"
        ).build()

    @Provides
    fun provideProductsDao(db: AppDatabase): ProductsDao = db.productsDao()

    @Provides
    fun provideCartDao(db: AppDatabase): CartDao = db.cartDao()

    @Provides
    @Singleton
    fun provideProductRepository(api: ApiService, db: AppDatabase): ProductRepository =
        ProductRepository(api, db)

    @Provides
    @Singleton
    fun provideCartRepository(cartDao: CartDao): CartRepository =
        CartRepository(cartDao)

    @Provides
    fun provideFavoritesDao(db: AppDatabase): FavoritesDao = db.favoritesDao()

    @Provides
    @Singleton
    fun provideFavoritesRepository(favoritesDao: FavoritesDao): FavoritesRepository =
        FavoritesRepository(favoritesDao)
}
