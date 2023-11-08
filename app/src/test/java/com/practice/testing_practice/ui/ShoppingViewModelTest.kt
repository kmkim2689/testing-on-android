package com.practice.testing_practice.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.practice.testing_practice.MainDispatcherRule
import com.practice.testing_practice.getOrAwaitValueTest
import com.practice.testing_practice.repository.FakeShoppingRepository
import com.practice.testing_practice.util.Constants
import com.practice.testing_practice.util.Status
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ShoppingViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ShoppingViewModel

    @Before
    fun setup() {
        viewModel = ShoppingViewModel(FakeShoppingRepository())
    }

    @Test
    fun `insert shopping item with empty field returns error`() {
        // emits the resource class - loading, success, error
        viewModel.insertShoppingItem("name", "", "3.0")

        // get the value of the resource emitted in the viewmodel
        // Event<Resource<ShoppingItem>>
        val value = viewModel.insertShoppingItemStatus.getOrAwaitValueTest()

        // expected : error resource should be emitted
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert shopping item with too long name returns error`() {
        // emits the resource class - loading, success, error
        // longer than the valid length
        // 이러한 류의 테스트는 상수를 사용하는 것이 중요
        // 만약 현재 20자가 제한인데 30자로 변경한다면, 최대 길이 상수만 변경하면 될 것
        val string = buildString {
            // buildString : Kotlin extension to have a string builder
            // Builds new string by populating newly created StringBuilder using provided builderAction and then converting it to String.
            // 최대 길이(경계값)보다 하나 더 많게 하는 것이 중요
            for (i in 1..Constants.MAX_NAME_LENGTH + 1) {
                append(1)
            }
        }

        viewModel.insertShoppingItem(string, "5", "3.0")

        // get the value of the resource emitted in the viewmodel
        // Event<Resource<ShoppingItem>>
        val value = viewModel.insertShoppingItemStatus.getOrAwaitValueTest()

        // expected : error resource should be emitted
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert shopping item with too long price returns error`() {
        val string = buildString {
            for (i in 1..Constants.MAX_PRICE_LENGTH + 1) {
                append(1)
            }
        }

        viewModel.insertShoppingItem("name", "5", string)

        val value = viewModel.insertShoppingItemStatus.getOrAwaitValueTest()

        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    // if we enter an invalid amount
    // 여기에서는 상수를 사용하지 않음
    // amount가 integer 형태이기 때문
    // integer는 float(overflow/underflow 시 infinity 표출)와는 달리 overflow/underflow가 발생하면 오류가 난다는 점에서 상수로 만들 수 없음. 따라서 하드코딩하는 것임
    @Test
    fun `insert shopping item with too high amount returns error`() {
        viewModel.insertShoppingItem("name", "9999999999999999999", "3.0")

        val value = viewModel.insertShoppingItemStatus.getOrAwaitValueTest()

        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    // 정상 동작
    @Test
    fun `insert valid shopping item returns success`() {
        viewModel.insertShoppingItem("name", "5", "3.0")

        val value = viewModel.insertShoppingItemStatus.getOrAwaitValueTest()

        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    // 제출이 완료된 후, imageUrl이 다시 빈 문자열이 되는지 테스트
    @Test
    fun `current image url becomes empty after submitted`() {
        viewModel.insertShoppingItem("name", "5", "3.0")
        val value = viewModel.currentImageUrl.value

        assertThat(value).isEmpty()

    }
}