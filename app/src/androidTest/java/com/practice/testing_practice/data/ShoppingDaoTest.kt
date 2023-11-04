package com.practice.testing_practice.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.practice.testing_practice.data.local.ShoppingDao
import com.practice.testing_practice.data.local.ShoppingItem
import com.practice.testing_practice.data.local.ShoppingItemDatabase
import com.practice.testing_practice.getOrAwaitValue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ShoppingDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ShoppingItemDatabase
    private lateinit var dao: ShoppingDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            context = ApplicationProvider.getApplicationContext(),
            klass = ShoppingItemDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.shoppingDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertShoppingItem() = runTest {
        // shopping item에 대한 데이터클래스 인스턴스를 만들어 투입해본다.
        val shoppingItem = ShoppingItem(
            name = "name", amount = 1, price = 1f, imageUrl = "adf", id = 1
        )
        // 투입
        dao.insertShoppingItem(shoppingItem)
        // 확인

        // 문제점 : 순수 List가 아닌 List가 들어있는 LiveData를 반환한다는 것.
        // 하지만 LiveData는 비동기적으로 동작하기 때문에, 테스트에 그대로 사용할 수는 없음
        // 구글에서 사용하는 헬퍼 클래스를 활용 : 테스트 루트 패키지에 둔다. LiveDataUtilAndroidTest
        // LiveData.getOrAwaitValue 함수는 라이브데이터 내부에 있는 데이터를 얻어올 때까지 대기하는 목적으로 활용됨
        // 매개변수로 정해놓은 시간을 초과하면, TimeOutException 표출
        val allShoppingItems = dao.observeAllShoppingItems().getOrAwaitValue()

        // 검증
        assertThat(allShoppingItems).contains(shoppingItem)

    }

    @Test
    fun deleteShoppingItem() = runTest {
        // 각각의 테스트케이스는 독립적으로 동작한다. 따라서, 삭제 전 넣어주는 작업도 수행 필요
        val shoppingItem = ShoppingItem(
            name = "name", amount = 1, price = 1f, imageUrl = "adf", id = 1
        )
        // 투입
        dao.insertShoppingItem(shoppingItem)

        // 삭제
        dao.deleteShoppingItem(shoppingItem)

        val allShoppingItems = dao.observeAllShoppingItems().getOrAwaitValue()

        assertThat(allShoppingItems).doesNotContain(shoppingItem)
    }

    // total price
    @Test
    fun observeTotalPriceSum() = runTest {
        val shoppingItem1 = ShoppingItem(
            name = "name", amount = 2, price = 10f, imageUrl = "adf", id = 1
        )

        val shoppingItem2 = ShoppingItem(
            name = "name", amount = 4, price = 5.5f, imageUrl = "adf", id = 2
        )

        val shoppingItem3 = ShoppingItem(
            name = "name", amount = 0, price = 100f, imageUrl = "adf", id = 3
        )

        // 투입
        dao.insertShoppingItem(shoppingItem1, shoppingItem2, shoppingItem3)

        val totalPriceSum = dao.observeTotalPrice().getOrAwaitValue()

        assertThat(totalPriceSum).isEqualTo(2 * 10f + 4 * 5.5f)
    }
}