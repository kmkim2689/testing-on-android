## Testing ViewModels

* ViewModel 파일 > 바로가기 메뉴 > Generate > Test
  * JUnit4 선택

* ViewModel의 경우, androidTest 폴더가 아닌 test 폴더에서 진행한다.
  * ViewModel은 안드로이드 컴포넌트가 아니기 때문에 jvm에서 테스트 가능

### Procedure
1. ViewModel의 인스턴스가 필요
* Before Annotation을 활용하여 테스트케이스 함수가 진행되기 전 ViewModel을 초기화
* 이 과정에서, ViewModel의 생성자로 Repository가 필요한데, 실제 Repository 클래스가 아닌 테스트를 위한 Fake Repository를 넘겨주어야 함
  * 해당 Repository는 실제 network call이 이뤄지지 않는 repository로 효율적인 테스트를 가능하도록 함

```
private lateinit var viewModel: ShoppingViewModel

@Before
fun setup() {
    viewModel = ShoppingViewModel(FakeShoppingRepository())
}
```

2. ViewModel에서 설계했던 각 함수들에 대한 테스트케이스 작성
```
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
```

3. 테스트 해보기
* 에러가 발생
```
Method getMainLooper in android.os.Looper not mocked. See https://developer.android.com/r/studio-ui/build/not-mocked for details.
java.lang.RuntimeException: Method getMainLooper in android.os.Looper not mocked.
```

* 이유 : 실제 ViewModel에서 아직 구현하지 않았기 때문
* 또한, 데이터베이스 dao 함수들을 테스트할때와 마찬가지로, instant task executor를 추가해야함
```
// 모든 테스트 코드를 하나의 쓰레드 안에서 실행시킴으로써, 동시에 여러 테스트가 일어나지 않도록 해야함
@get:Rule
var instantTaskExecutorRule = InstantTaskExecutorRule()
```
* @get: Rule 어노테이션을 통해 이 규칙을 정의하면 해당 클래스에서 발생하는 모든 아키텍처 컴포넌트(LiveData)의 백그라운드 실행을 하나의 스레드에서 작동시켜준다. 그로 인해 thread safe 한 상태에서 테스트를 동작시킬 수 있다.
출처: https://programmmingphil.tistory.com/18 [원해성개발블로그:티스토리]

4. ViewModel의 실제 함수 구현하기

* try... catch에서의 return문

* MutableLiveData의 postValue와 (set)value의 차이점
  * value setter의 경우, MutableLiveData의 모든 변화를 옵저버에게 알림
  * postValue의 경우, 여러 차례에 걸쳐 짧은 시간 안에 수행될 경우, 마지막 것만 옵저버에게 알림
  ```
  The value "b" would be set at first and later the main thread would override it with the value "a".
  If you called this method multiple times before a main thread executed a posted task, only the last value would be dispatched.
  ```
  * 실제 앱의 사용에서는 그리 중요하지는 않은 차이지만, 테스트케이스 작성에 있어 매우 중요한 차이임
    * 어떤 것을 검색하는 동작을 테스트할 때는 반드시 loading이 발행된 후 그 결과(success/error)가 발행되는지를 테스트하는데,
    * 이것을 postValue로 구현하다 보면 짧은 시간 안에 발행었을 때 loading이 누락된다. 따라서 매우 중요함
    

5. 다시 테스트 진행해보기
> Exception in thread "Test worker" java.lang.IllegalStateException: Module with the Main dispatcher had failed to initialize.

* 이유?
  * ViewModel 함수에서 Coroutine을 활용하기 때문
  * ViewModel에서 Main Dispatcher를 활용하는데(room의 경우 main 문맥에서 수행할 수 있도록 기본적으로 설계), 이것을 사용하는 것이 테스트 클래스에서는 불가능 
    * Main Dispatcher는 Main Looper에 의존(실제 앱 동작 시에만 활용 가능)
  * 결론적으로는 Test 클래스에서는 Main Dispatcher에 접근할 권한이 없기 때문에 오류가 발생한 것
    * 이것을 해결하는 가장 쉬운 방법은 androidTest에서 테스트케이스를 작성하는 것이지만, 해당 테스트의 경우 에뮬레이터가 필요없는 테스트이므로 그렇게 하는 것은 비효율적

* 해결 : JUnit rule을 활용하여 Main Coroutine Rule을 제작
  * 역할 : 특별한 Dispatcher를 제작(Main Dispatcher는 아니지만, 코루틴을 활용하는 함수에 대한 테스트를 진행할 수 있도록 해주는 헬퍼 역할)
  * https://stackoverflow.com/questions/71348107/migrate-maincoroutinerule
    ```
    class MainDispatcherRule(
      private val testDispatcher: TestDispatcher = StandardTestDispatcher(),
    ) : TestWatcher() {
      @OptIn(ExperimentalCoroutinesApi::class)
      override fun starting(description: Description) {
      super.starting(description)
      Dispatchers.setMain(testDispatcher)
    }

      @OptIn(ExperimentalCoroutinesApi::class)
      override fun finished(description: Description) {
          super.finished(description)
          Dispatchers.resetMain()
      }
    } 
    ```
  * 테스트 코드에 추가
    ```
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()
    ```