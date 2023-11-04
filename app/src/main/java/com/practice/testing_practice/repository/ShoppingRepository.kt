package com.practice.testing_practice.repository

import androidx.lifecycle.LiveData
import com.practice.testing_practice.data.local.ShoppingItem
import com.practice.testing_practice.data.remote.ImageResponse
import com.practice.testing_practice.util.Resource

interface ShoppingRepository {

    suspend fun insertShoppingItem(shoppingItem: ShoppingItem)

    suspend fun deleteShoppingItem(shoppingItem: ShoppingItem)

    fun observeAllShoppingItems(): LiveData<List<ShoppingItem>>

    fun observeTotalPrice(): LiveData<Float>

    suspend fun searchForImage(imageQuery: String): Resource<ImageResponse>
}