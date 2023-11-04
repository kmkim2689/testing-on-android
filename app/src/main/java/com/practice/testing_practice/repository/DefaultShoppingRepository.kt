package com.practice.testing_practice.repository

import androidx.lifecycle.LiveData
import com.practice.testing_practice.data.local.ShoppingDao
import com.practice.testing_practice.data.local.ShoppingItem
import com.practice.testing_practice.data.remote.ImageResponse
import com.practice.testing_practice.data.remote.PixabayApi
import com.practice.testing_practice.util.Resource
import javax.inject.Inject

class DefaultShoppingRepository @Inject constructor(
    private val shoppingDao: ShoppingDao,
    private val pixabayApi: PixabayApi
): ShoppingRepository {
    override suspend fun insertShoppingItem(shoppingItem: ShoppingItem) {
        shoppingDao.insertShoppingItem(shoppingItem)
    }

    override suspend fun deleteShoppingItem(shoppingItem: ShoppingItem) {
        shoppingDao.deleteShoppingItem(shoppingItem)
    }

    override fun observeAllShoppingItems(): LiveData<List<ShoppingItem>> {
        return shoppingDao.observeAllShoppingItems()
    }

    override fun observeTotalPrice(): LiveData<Float> {
        return shoppingDao.observeTotalPrice()
    }

    override suspend fun searchForImage(imageQuery: String): Resource<ImageResponse> {
        return try {
            val response = pixabayApi.searchForImage(imageQuery)
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.success(it)
                } ?: Resource.error("unknown error", null)
            } else {
                return Resource.error("unknown error", null)
            }
        } catch (e: Exception) {
            Resource.error("server error", null)
        }
    }
}