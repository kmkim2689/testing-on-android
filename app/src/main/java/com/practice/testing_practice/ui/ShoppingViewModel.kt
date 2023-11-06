package com.practice.testing_practice.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.testing_practice.data.local.ShoppingItem
import com.practice.testing_practice.data.remote.ImageResponse
import com.practice.testing_practice.repository.ShoppingRepository
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
        // TODO After writing test case
    }

    fun searchForImage(imageQuery: String) {
        // TODO After writing test case
    }
}