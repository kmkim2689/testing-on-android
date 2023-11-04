package com.practice.testing_practice.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.practice.testing_practice.data.local.ShoppingItem
import com.practice.testing_practice.data.remote.ImageResponse
import com.practice.testing_practice.util.Resource

class FakeShoppingRepository : ShoppingRepository {
    // ShoppingRepository를 구현해야함

    // database 대용 list
    private val shoppingItems = mutableListOf<ShoppingItem>()

    // observeAllShoppingItems에 대한 동작 여부를 테스트해보기 위함
    private val observableShoppingItems = MutableLiveData<List<ShoppingItem>>(shoppingItems)
    private val observableShoppingPrice = MutableLiveData<Float>()

    // 가장 바깥에 이 변수를 정의 -> 뷰모델에서 사용해야 하기 때문
    private var shouldReturnNetworkError = false

    override suspend fun insertShoppingItem(shoppingItem: ShoppingItem) {
        shoppingItems.add(shoppingItem)
        // shoppingItems의 변경 사항은 자동으로 livedata에 반영되지 않는다는 것에 유의.
        // 따라서, 아이템을 추가한 후, livedata에 다시 해당 데이터를 post해주는 과정이 필요하다.
        refreshLiveData() // 별도의 함수를 정의하여 사용
    }

    override suspend fun deleteShoppingItem(shoppingItem: ShoppingItem) {
        shoppingItems.remove(shoppingItem)
        refreshLiveData()
    }

    override fun observeAllShoppingItems(): LiveData<List<ShoppingItem>> {
        return observableShoppingItems
    }

    override fun observeTotalPrice(): LiveData<Float> {
        return observableShoppingPrice
    }

    override suspend fun searchForImage(imageQuery: String): Resource<ImageResponse> {
        return if (shouldReturnNetworkError) {
            Resource.error("test error occured", null)
        } else {
            Resource.success(ImageResponse(
                listOf(),
                0,
                0
            ))
        }
    }



    fun setShouldReturnNetworkError(value: Boolean) {
        shouldReturnNetworkError = value
    }

    private fun refreshLiveData() {
        // 백그라운드에서 LiveData의 변경사항을 알리고 반영
        observableShoppingItems.postValue(shoppingItems)
        observableShoppingPrice.postValue(getTotalPrice())
    }

    private fun getTotalPrice(): Float {
        return shoppingItems.sumOf {
            it.price.toDouble()
        }.toFloat()
    }
}