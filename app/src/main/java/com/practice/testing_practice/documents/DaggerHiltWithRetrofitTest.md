## Testing With Dagger-Hilt and Retrofit

### 1. 제반 사항 설정

* 앱키를 설정 - gradle.properties에 두고 gitignore를 설정하여 보호

* Api Service Interface
```
interface PixabayApi {
    @GET("/api/")
    suspend fun searchForImage(
        @Query("q") searchQuery: String,
        @Query("key") apkKey: String = BuildConfig.API_KEY
    ): Result<ImageResponse>
}
```

* Dagger Hilt 관련 설정들
```
@HiltAndroidApp
class ShoppingApplication : Application() {
}
// 이후, AndroidManifest의 application 태그에 name 설정
// 또한, Activity에도 추가적으로 @AndroidEntryPoint 어노테이션 설정
```

```
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideShoppingDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, ShoppingItemDatabase::class.java, DATABASE_NAME)
    
    @Provides
    @Singleton
    fun provideDatabaseDao(
        database: ShoppingItemDatabase
    ) = database.shoppingDao()
    
    @Provides
    @Singleton
    fun providePixabayApi(): PixabayApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PixabayApi::class.java)  
    } 
}
```

### 2. 테스트 코드 작성하기
* 테스트의 가장 중요한 요소들 중 하나는 속도이기 때문에,
* 따라서 코드를 테스트하기 위해 실제로 네트워크 호출을 하지는 않음
* 다른 방법으로 적용할 수 있는 것이 바로 Test Doubles
  * 특정 클래스에 대해 테스트해보고 싶으면, 다른 버전의 클래스를 하나 더 만들어서 테스트해보는 것
  * 즉, 오직 테스트만을 위한 클래스를 만드는 것 -> 테스트 케이스에 적절한 코드를 작성
  * 만약 Repository에 대한 코드를 테스트해보고 싶다면, 실제로 사용할 Repository와 테스트만을 위해 사용할 Repository를 별도로 만드는 것이 그 예시


* Wrapper class 이용
```
data class Resource<out T>(
    val status: Status,
    val data: T?,
    val message: String?
) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(message: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, message)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}
```

0. 인터페이스 작성하기
```
interface ShoppingRepository {

    suspend fun insertShoppingItem(shoppingItem: ShoppingItem)

    suspend fun deleteShoppingItem(shoppingItem: ShoppingItem)

    fun observeAllShoppingItems(): LiveData<List<ShoppingItem>>

    fun observeTotalPrice(): LiveData<Float>

    suspend fun searchForImage(imageQuery: String): Resource<ImageResponse>
}
```


1. 실제 사용할 Repository 작성하기
```
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
```

2. 테스트용 Repository 작성

* Fake Test Double 작성 - 목적 : 실제로 사용할 Repository이 어떤 방향으로 동작할 수 있을지 알아보고 시뮬레이션해보는 것
  * Test Double의 종류로는 여러 가지가 있지만, 가장 널리 사용되는 것들은 fakes, mocks
  * 그 중 Fake Test Double은 테스트케이스에 적절히 활용될 수 있고 실제 활용으로는 부적절한 클래스
  * API의 동작에 대한 테스트를 하려 하지만, 실제 network 요청은 하고 싶지 않은 경우 활용
  * Fake Repository를 작성하는 목적은 실제 활용될 Repository의 동작을 테스트하고자 하는 목적이 아니다.
    * Repository를 생성자로 활용할 ViewModel을 테스트하기 위하여 Fake Repository를 만드는 것이다.
    * ViewModel의 생성자로 Repository를 넣어 사용하기 때문
    * 따라서, 실제 database나 api를 사용하지 않고 리스트 형태로 활용

* 필요한 두 가지
  * 네트워크로부터 올 법한 응답을 직접 만들기
  * 네트워크가 활성화되어있는지에 대한 Boolean값과 그것을 설정할 수 있는 함수 하나
    * api 에러 발생 시뮬레이션을 위함 => 테스트 시 클래스 외부에서 설정

```
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
```