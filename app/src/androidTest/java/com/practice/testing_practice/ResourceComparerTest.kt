package com.practice.testing_practice

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class ResourceComparerTest {

    // reference to a context
    // rely on a class we need an instance of

    // bad practice1 : globally declaring an instance - don't do that...
    // 이유 : 좋은 테스트케이스의 조건은, 각 테스트 케이스들이 독립적으로 동작해야 한다는 것이다.
    // 하지만, 이러한 식으로 전역 인스턴스를 선언해버리면, 여러 개의 테스트 케이스가 하나의 인스턴스에 의존하게 되는 모습을 보이게 된다.
    // 즉, 여러 개의 테스트 케이스가 더 이상 독립적이지 않게 된다.
    // 특히나, 만약 해당 클래스가 dependency를 가지는 클래스라면, 각 테스트 케이스마다 다른 형태의 다른 instance를 넣어야 할 것이다.
    // 이러한 여러가지 상황을 고러했을 때, 이것은 좋지 않은 방법
    // private val resourceComparer = ResourceComparer()

    // bad practice2 : lateinit 사용하고, 함수 내부에서 할당하는 방식
    // 각 테스트케이스 함수마다 할당해주는 작업을 수행해야 하며, 이는 매우 반복적이고 귀찮은 일
    // private lateinit var resourceComparer: ResourceComparer
    // 함수 내에서 resourceComparer = ResourceComparer()

    // junit에서 제공하는 best practice : @Before + setUp function
    // lateinit으로 먼저 선언
    private lateinit var resourceComparer: ResourceComparer


    // Before : 해당 클래스 내부의 테스트 케이스 하나가 실행되기 시작하기 전마다 실행되는 코드
    @Before
    fun setUp() {
        // write the logic we need to execute 'before' the running of every test case
        resourceComparer = ResourceComparer()
    }

    // @After annotation
    // teardown 함수와 함께 사용
    // 여기서 선언하였던 객체들을 destroy시킬 수 있음
    // 다만 이 경우에는 garbage collector가 이 일을 대신 해주기 때문에 굳이 필요 없음
    // 만약 room database를 테스트가 끝나고 database를 close하고 싶을 때 등에 활용
    // 그 외 mvvm을 테스트 할 때 등
    @After
    fun teardown() {

    }

    // androidTest에서는 backtick을 이용한 함수명 설정 불가능
    @Test
    fun stringResourceSameAsGivenString_returnsTrue() {
        // android test에서 context를 활용하는 방법
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = resourceComparer.isEqual(context, R.string.app_name, "testing-practice")
        assertThat(result).isTrue()
    }

    @Test
    fun stringResourceDifferentAsGivenString_returnsFalse() {
        // android test에서 context를 활용하는 방법
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = resourceComparer.isEqual(context, R.string.app_name, "testing-pr")
        assertThat(result).isFalse()
    }


}