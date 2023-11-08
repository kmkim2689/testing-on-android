package com.practice.testing_practice.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.testing_practice.data.local.ShoppingItem
import com.practice.testing_practice.data.remote.ImageResponse
import com.practice.testing_practice.repository.ShoppingRepository
import com.practice.testing_practice.util.Constants
import com.practice.testing_practice.util.Event
import com.practice.testing_practice.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val repository: ShoppingRepository
): ViewModel() {

    val shoppingItems = repository.observeAllShoppingItems()

    val totalPrice = repository.observeTotalPrice()

    // live data for query in image responses
    // if we make a request to the api, get a list of images that match search keyword
    // live data that holds the list of the responses
    private val _images = MutableLiveData<Event<Resource<ImageResponse>>>()
    val images: LiveData<Event<Resource<ImageResponse>>>
        get() = _images

    // when we select an image from the recyclerview
    // to save the image url for glide in another screen
    private val _currentImageUrl = MutableLiveData<String>()
    val currentImageUrl: LiveData<String>
        get() = _currentImageUrl

    // valid status
    private val _insertShoppingItemStatus = MutableLiveData<Event<Resource<ShoppingItem>>>()
    val insertShoppingItemStatus: LiveData<Event<Resource<ShoppingItem>>>
        get() = _insertShoppingItemStatus

    fun setCurrentImageUrl(url: String) {
        _currentImageUrl.postValue(url)
    }

    fun deleteShoppingItem(shoppingItem: ShoppingItem) = viewModelScope.launch {
        repository.deleteShoppingItem(shoppingItem)
    }

    fun insertShoppingItemIntoDb(shoppingItem: ShoppingItem) = viewModelScope.launch {
        repository.insertShoppingItem(shoppingItem)
    }

    // validate the user input
    fun insertShoppingItem(name: String, amountAsString: String, priceAsString: String) {
        if (name.isEmpty() || amountAsString.isEmpty() || priceAsString.isEmpty()) {
            _insertShoppingItemStatus.postValue(Event(Resource.error("the fields should not be empty", null)))
            return // @insertShoppingItem
        }

        if (name.length > Constants.MAX_NAME_LENGTH) {
            _insertShoppingItemStatus.postValue(Event(Resource.error("the name of the itme" +
                    "must not exceed ${Constants.MAX_NAME_LENGTH} characters", null)))
            return // @insertShoppingItem
        }

        if (priceAsString.length > Constants.MAX_PRICE_LENGTH) {
            _insertShoppingItemStatus.postValue(Event(Resource.error("the price of the itme" +
                    "must not exceed ${Constants.MAX_PRICE_LENGTH} characters", null)))
            return // @insertShoppingItem
        }

        val amount = try {
            amountAsString.toInt()
        } catch (e: Exception) {
            _insertShoppingItemStatus.postValue(Event(Resource.error("please enter a valid amount", null)))
            return // @insertShoppingItem
        }

        val shoppingItem = ShoppingItem(
            name = name,
            amount = amount,
            price = priceAsString.toFloat(),
            imageUrl = _currentImageUrl.value ?: ""
        )

        insertShoppingItemIntoDb(shoppingItem)
        setCurrentImageUrl("") // 입력이 완료되면 초기화해야함. 여기서는 ViewModel을 여러 화면에서 공유하기 때문

        // 마지막으로, 모든 작업이 성공적으로 이뤄졌음을 emit
        _insertShoppingItemStatus.postValue(Event(Resource.success(shoppingItem)))
    }

    fun searchForImage(imageQuery: String) {
        if (imageQuery.isEmpty()) {
            return
        }
        _images.value = Event(Resource.loading(null))

        viewModelScope.launch {
            val response = repository.searchForImage(imageQuery)
            _images.value = Event(response)
        }
    }
}