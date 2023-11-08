## Hilt를 활용하여 테스팅하기

> 앱의 규모가 커질수록, 작성해야 할 테스트용 클래스도 많아지고 테스트케이스도 많아지게 된다.
> 이 과정에서, 각 클래스마다 필요한 다른 객체들을 일일이 초기화한다면, 이것은 boilerplate 코드를 야기하게 되며 매우 정돈 되지 않은 코드를 야기한다.
> 따라서, @Before setup 함수 대신에, Hilt를 활용하면 깔끔하게 필요한 객체들을 모듈로 관리할 수 있다.
> 한 곳에 저장소의 개념으로 테스트에 필요한 객체들을 보존하고, 필요한 곳에 주입하는 방식이다. 일반적인 Hilt의 사용방식과 동일

* 주의 : Local Test가 아닌 Instrumented Test에서만 동작한다.(androidTest에서만)
  * Hilt는 안드로이드 API이기 때문

* 필요 Dependencies
* 이미 정의해놓은 다른 hilt dependency들과 버전이 같아야함
```
androidTestImplementation("com.google.dagger:hilt-android-testing:2.47")
kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.47")
```

### Procedure
1. Special Appplication Needed
* Application 클래스에서는 @HiltAndroidApp을 추가하지만, Test 클래스에서는 이것이 통하지 않는다.
* 따라서, 특별한 Appcliation 클래스를 만든다.
```
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
        // 본래 className에는 실제 앱애서 사용하는 Application 클래스명을 할당
        // 하지만, HiltTestApplication의 클래스명을 전달함
    }
}
```

2. gradle 파일의 testInstrumentationRunner 수정
```
defaultConfig {
    applicationId = "com.practice.testing_practice"
    minSdk = 24
    targetSdk = 34
        versionCode = 1
    versionName = "1.0"

    // testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" 
    testInstrumentationRunner = "본인 패키지 주소.HiltTestRunner" 
    vectorDrawables {
        useSupportLibrary = true
    }
}
```

3. TestAppModule 제작
* 테스트 케이스에서만 사용될 객체들을 가지고 있기 위함
* 실제 앱 구현과 동일한 방식으로 한다.
```
@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {
    
    @Provides
    @Named("test_db") // 이미 앱 차원에서 ShoppingItemDatabase를 주입하기 위한 함수가 존재. 이름으로 구분짓기 위해 이것이 필요
    // Singleton으로 annotate하지 않는다 - 각 테스트 케이스 별로 독립된 데이터베이스 필요
    fun provideInMemoryDb(@ApplicationContext context: Context) = 
        Room.inMemoryDatabaseBuilder(context, ShoppingItemDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    
}
```

4. 테스트 클래스의 @RunWith annotation 변경
* 제거

5. @HiltAndroidTest annotation 추가
* 안드로이드 컴포넌트(Activity, Fragment 등)에서 Hilt를 활용하여 주입받고자 할 때 사용하는 annotation과 비슷
* 테스트 클래스에서 이러한 역할을 하는 것이 바로 @HiltAndroidTest
* 의존성을 Test 클래스에 주입하겠다는 의미

6. Field Injection 진행
```
@SmallTest
@HiltAndroidTest
class ShoppingDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject // Field Injection
    @Named("test_db")
    lateinit var database: ShoppingItemDatabase // private로 설정하면 주입 불가능
    lateinit var dao: ShoppingDao // private로 설정하면 주입 불가능
    
    // ...
}
```

7. Hilt 사용을 위한 rule 추가
```
@get:Rule
var hiltRule = HiltAndroidRule(this)
```

8. @Before setup 함수에서 주입을 시작하기 위한 코드 작성
* HiltAndroidRule.inject() : 테스트 클래스로 주입 시작

```
@Before
fun setup() {
    hiltRule.inject()
    dao = database.shoppingDao()
}
```

* Dao Object는 주입받지 않는 이유는?
  * Dao는 데이터베이스 연산을 하기 위한 코드인 만큼 여러 함수들의 사용에 대한 변화를 신경쓸 필요가 없음 