package com.bamtori.chestnutmap

import android.app.Application
import com.google.android.libraries.places.api.Places
import java.util.Locale
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 앱이 시작될 때 Places SDK를 초기화합니다.
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.GOOGLE_MAP_KEY, Locale.KOREAN)
        }
    }
}

/*
 * =================================================================================================
 *  Android App 실행 순서 (사용자 아이콘 클릭 시)
 * =================================================================================================
 *
 * 1. 앱 프로세스 생성 및 Application 클래스 초기화
 *    - 파일: AndroidManifest.xml
 *    - 내용: 사용자가 앱 아이콘을 탭하면, 안드로이드 시스템은 가장 먼저 Manifest 파일을 읽습니다.
 *           <application> 태그에 android:name=".MainApplication" 으로 등록된 클래스가 있는지 확인합니다.
 *
 * 2. 전역 초기화 실행
 *    - 파일: MainApplication.kt
 *    - 내용: 시스템은 `MainActivity`보다 먼저 `MainApplication`의 인스턴스를 생성하고 `onCreate()` 메서드를 호출합니다.
 *           이곳에 작성된 `Places.initialize()` 코드가 실행되면서, 앱의 다른 어떤 부분보다도 먼저
 *           Google Places SDK가 안전하게 초기화됩니다. 앱 전역에서 사용할 라이브러리를 초기화하기에 가장 이상적인 위치입니다.
 *
 * 3. 시작 액티비티(Activity) 실행
 *    - 파일: AndroidManifest.xml
 *    - 내용: 시스템은 Manifest 파일에서 "LAUNCHER" 인텐트 필터를 가진 액티비티를 찾습니다.
 *           현재 프로젝트에서는 `MainActivity`가 시작 지점으로 지정되어 있습니다.
 *
 * 4. 메인 화면 UI 구성 시작
 *    - 파일: MainActivity.kt
 *    - 내용: 시스템이 `MainActivity`의 `onCreate()` 메서드를 호출합니다.
 *           `onCreate()` 내부의 `setContent { ... }` 블록이 실행되면서, UI 렌더링 제어권이
 *           안드로이드 뷰 시스템에서 Jetpack Compose로 넘어갑니다.
 *
 * 5. 내비게이션 및 화면 라우팅
 *    - 파일: ui/navigation/AppNavigation.kt (추정)
 *    - 내용: `setContent` 블록 내부에서 `AppNavigation` Composable이 호출됩니다.
 *           `AppNavigation`은 `NavHost`를 사용하여 앱의 전체 화면 흐름을 정의하고,
 *           어떤 화면을 가장 먼저 보여줄지(startDestination) 결정합니다.
 *
 * 6. 화면 및 ViewModel 생성
 *    - 파일: ui/map/MapScreen.kt, ui/map/MapViewModel.kt 등
 *    - 내용: 내비게이션 설정에 따라 `MapScreen` Composable 함수가 호출됩니다.
 *           `MapScreen`은 자신의 로직을 처리하기 위해 `MapViewModel`을 필요로 하며,
 *           이 시점에 `MapViewModel`의 인스턴스가 생성됩니다.
 *
 * 7. Places API 사용 (성공)
 *    - 파일: ui/map/MapViewModel.kt
 *    - 내용: `MapViewModel` 내부 코드(예: init 블록)에서 Places API 관련 기능을 호출합니다.
 *           이제는 2단계(`MainApplication.kt`)에서 이미 SDK가 완벽하게 초기화되었기 때문에,
 *           `IllegalStateException` 오류 없이 API를 성공적으로 사용할 수 있습니다.
 *
 */