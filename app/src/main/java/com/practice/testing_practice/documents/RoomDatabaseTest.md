## TDD
* Testing a real android project with the components
  * LiveData, Coroutines, Room ...

## Testing For Room
* TDD에서는 기본적으로 함수에 대한 실제 구현에 들어가기 전 테스트 케이스를 작성하는 것이 필요
* 하지만, Room의 경우, interface dao로만 함수를 정의하는 데 그치고 함수를 구현하지는 않기 때문에, 필요한 클래스를 구축한 후 테스트 케이스를 작성

* Entity
```
@Entity(tableName = "shopping_items")
data class ShoppingItem(
    var name: String,
    var amount: Int,
    var price: Float,
    var imageUrl: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
)
```

* Dao
  * 그 중, 모든 아이템을 LiveData 형태로 불러올 수 있도록 하는 function을 Room에서 지원하므로 반환값으로 활용
  * 이 때 해당 함수는 suspend function으로 설정하지 않음
    * 이유 : LiveData 객체는 이미 기본적으로 비동기적으로 동작하기 때문에 suspend function으로 정의하면 room과 적절히 동작할 수 없음
    * 따라서, Room Database로부터 LiveData 형태로 반환받고자 한다면, 함수를 suspend로 만들지 않도록 한다.

  * 특정 칼럼의 합을 구하고 싶다면 SUM, 평균을 구하고 싶다면 AVG를 활용
```
@Dao
interface ShoppingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(shoppingItem: ShoppingItem)

    @Delete
    suspend fun deleteShoppingItem(shoppingItem: ShoppingItem)
    
    @Query("SELECT * FROM shopping_items")
    fun observeAllShoppingItems(): LiveData<List<ShoppingItem>>
    
    @Query("SELECT SUM(price * amount) FROM shopping_items")
    fun observeTotalPrice(): LiveData<Float>
}
```

* Database
```
@Database(
    entities = [ShoppingItem::class],
    version = 1
)
abstract class ShoppingItemDatabase : RoomDatabase() {
    abstract fun shoppingDao(): ShoppingDao
}
```

### Room 테스트 진행해보기
* Room database의 경우, Dao 인터페이스에 대해 테스트를 진행한다.
* LiveData를 반환하는 메소드의 경우, local unit test를 진행할 수 없고, instrumented unit test를 진행해야 한다.
  * 이유는 안드로이드 프레임워크를 활용하기 때문 
  * RoomDatabase를 활용하기 위해서는, Context라는 것이 필요하다.
  * 또한, 데이터의 흐름이 안드로이드 디바이스에서 동작하는지를 테스트하고자 하는 것이기 때문에 instrumented unit test로 진행해야 한다.

* 테스트를 진행할 때, 보통 main에서의 패키지 구조와 같은 구조로 가는 것이 정석이다.
* Dao는 인터페이스 형태지만, 테스트의 경우 모두 클래스 형태로 만들며, 파일명은 기존의 클래스명 + Test로 한다.

* androidTest > ... > 프로젝트명 > data > ShoppingDaoTest.kt

* 필요 annotations
  * @RunWith(AndroidJUnit4::class) : instrumented test(androidTest)이기 때문에 추가
    * 본래 JUnit은 로컬 유닛 테스트를 위한 테스팅 라이브러리이다.(jvm위에서 동작하는 kotlin 혹은 java 코드를 테스트하기 위한 목적)
    * 하지만, 현재 테스트하고자 하는 메소드들은 순수한 java/kotlin환경에서 테스트하는 것이 아니다.
    * JVM이 아닌 android emulator에서 실행되기 때문에, 그리고 Android 생태계에서 동작하는 코드에 대한 테스트를 진행하므로, 다른 접근 방식이 필요
    * 해당 annotation은 해당 클래스 내부의 테스트 코드들은 에뮬레이터에서 동작해야 함을 JUnit에게 알리기 위한 목적
  * @SmallTest : Unit Test(소규모 단위의 유닛 테스트)임을 의미
    * Test의 종류로, Unit / Integration / UI 테스트가 있으며, UI 테스트로 갈수록 규모는 커진다.
    * JUnit에게 해당 테스트가 Unit test임을 알림
    * 필수는 아니지만, 명시하는 것이 좋음
    * 그 외 MediumTest, LargeTest가 존재
    ```
    Annotation to assign a small test size qualifier to a test. This annotation can be used at a method or class level.
    Test size qualifiers are a great way to structure test code and are used to assign a test to a test suite of similar run time.
    Execution time: <200ms
    Small tests should be run very frequently. Focused on units of code to verify specific logical conditions. These tests should runs in an isolated environment and use mock objects for external dependencies. Resource access (such as file system, network, or databases) are not permitted. Tests that interact with hardware, make binder calls, or that facilitate android instrumentation should not use this annotation.
    Note: This class replaces the deprecated Android platform size qualifier android.test.suitebuilder.annotation.SmallTest  and is the recommended way to annotate tests written with the AndroidX Test Library.
    ```
    
* 클래스 내부에서 필요한 것
  * Database 객체
    * 주의 : 각 테스트케이스 함수 별로, 다른 데이터베이스 객체를 가지고 있어야 한다. 공유한다면 원활한 테스트 불가능
      * 공유한다면 한 곳에서 데이터를 집어넣었을 때, 다른 곳에도 그 데이터베이스를 사용하기 때문에 테스트의 독립성이 깨지기 때문
    * 따라서, @Before annotation을 활용 : 각각의 테스트케이스 function이 실행되기 전마다 한번씩 실행됨 -> 초기화 역할
    * 주의할 점은, 보통 데이터베이스 객체를 만들기 위해 Room.databaseBuilder()를 활용하지만, 테스트에서는 inMemoryDatabaseBuilder()를 활용해야 한다는 것
      * 이 방식으로 제작한 객체는 진정한 의미의 데이터베이스가 아님
      * ram에만 데이터를 보존하는 데이터베이스로서, 영구 저장소에 저장되지 않는다.
      * 일회성이므로 테스트 케이스를 작성할 때 유용하게 사용 가능
      * allowMainThreadQueries : 말 그대로 데이터베이스를 메인 쓰레드에서도 접근할 수 있도록 하기 위함
        * 보통 blocking call(시간이 오래 걸리는 작업)이므로 백그라운드 쓰레드에서 접근하는 것이 맞지만,
        * 테스트 케이스에서는, 데이터베이스 호출이 하나의 쓰레드에서만 이뤄지는 것을 원한다.
          * 이유는 테스트케이스에 멀티쓰레딩을 적용한다면, 여러 쓰레드로부터 데이터베이스를 변경시킬 수 있기 때문이다.
          * 결국 각 테스트케이스 간 독립성을 유지하기 위함이다. 한 쓰레드에서만 진행시킴으로써 한 액션이 실행되고 다른 액션이 실행되도록 할 수 있음
    
  * Dao
  
  ```
  @RunWith(AndroidJUnit4::class)
  @SmallTest
  class ShoppingDaoTest {

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
  }
  ```
  
  * @After를 활용하여, 매 테스트케이스가 끝난 이후'마다' 실행되어야 할 코드를 작성
  * 함수 하나가 끝날 때마다 데이터베이스를 close

  ```
  @RunWith(AndroidJUnit4::class)
  @SmallTest
  class ShoppingDaoTest {

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

  } 
  ```
  
  * 본격적으로 테스트 작성
    * dao에 있는 모든 funciton들을 테스트
    * suspend function인 경우, coroutine 스코프 내부에서 테스트를 진행해야 할 것이다.
      * 이 경우, scope로 runBlocking을 활용한다. - 코루틴을 main thread에서 실행하도록 하는 방법. suspend call이 진행될 때, main thread가 block된다.
      * 테스트 케이스에서는 여러 테스트 쿼리들이 동시에 실행되는 것을 원하지 않기 때문
      * multithreading을 방지하기 위하여, runBlocking을 활용한다.
        * 그 중, runTest라는 테스트용 코루틴 빌더를 통해 테스트한다. runBlockingTest는 deprecated되었다.
        * 이것은 테스트에 최적화된 코루틴 빌더로, delay() 함수는 건너뛴다.
        * 내부에서 사용할 수 있는 것들(스코프 내부에서 Ctrl + Spacebar를 통해 확인 가능)
          * advanceTimeBy() : modify the time of the coroutine
    * LiveData는 비동기적으로 동작하는 만큼, 테스트에 직접 사용하기에는 부적절하다. 따라서 구글에서 제시한 함수를 활용하면 LiveData를 활용한 테스트를 할 수 있다.
          
  ```
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
  ```
  
  * 이 과정을 거친 후 테스트를 실행해보면 테스트가 실패한다.
    * runBlocking 내부에 코드를 작성해도 LiveData가 비동기적으로 동작하기 때문
    * 해결 : 테스트 클래스에 있는 모든 테스트 케이스들을 실행할 때 하나가 끝나고 다른 하나가 실행되도록 명시해줘야 함
    * @Rule : instant task executor rule
      InstantTaskExecutorRule을 이용하면 안드로이드 구성요소 관련 작업들을 모두 한 스레드에서 실행되게 한다.
      그러므로 모든 작업이 synchronous하게 동작하여 테스팅을 원활하게 할 수 있다.
      즉 동기화 때문에 고민할 필요가 없어진다.
      특히 LiveData를 이용한다면 필수적으로 InstantTaskExecutorRule를 사용해야할 것이다.
  * ```
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    ```
    

  ```
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
  ```