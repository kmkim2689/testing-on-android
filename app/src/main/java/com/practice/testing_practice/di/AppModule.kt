package com.practice.testing_practice.di

import android.content.Context
import androidx.room.Room
import com.practice.testing_practice.data.local.ShoppingDao
import com.practice.testing_practice.data.local.ShoppingItemDatabase
import com.practice.testing_practice.data.remote.PixabayApi
import com.practice.testing_practice.repository.DefaultShoppingRepository
import com.practice.testing_practice.repository.ShoppingRepository
import com.practice.testing_practice.util.Constants.BASE_URL
import com.practice.testing_practice.util.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideShoppingDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        ShoppingItemDatabase::class.java,
        DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideDatabaseDao(
        database: ShoppingItemDatabase
    ) = database.shoppingDao()

    @Provides
    @Singleton
    fun providePixabayApi(): PixabayApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PixabayApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDefaultShoppingRepository(
        dao: ShoppingDao,
        api: PixabayApi
    ): ShoppingRepository = DefaultShoppingRepository(dao, api)

}

