# ChestnutMap

이 프로젝트는 ChestnutMap 안드로이드 애플리케이션입니다.

## 프로젝트 구조

```
/Users/jaeyoung/esquisse/ChestnutMap/
├───.gitignore
├───지도공유플랫폼_PRD.md
├───build.gradle.kts
├───chestnutMap.pdf
├───settings.gradle.kts
├───TASKS.md
├───.git/...
└───app/
    ├───build.gradle.kts
    └───src/
        └───main/
            ├───AndroidManifest.xml
            ├───java/
            │   └───com/
            │       └───bamtori/
            │           └───chestnutmap/
            │               ├───MainActivity.kt
            │               ├───data/
            │               │   ├───map/
            │               │   │   ├───MapRepository.kt
            │               │   │   └───MarkerRepository.kt
            │               │   └───model/
            │               │       ├───Map.kt
            │               │       ├───Marker.kt
            │               │       └───MemoItem.kt
            │               └───ui/
            │                   ├───login/
            │                   │   ├───AuthRepository.kt
            │                   │   ├───LoginScreen.kt
            │                   │   └───LoginViewModel.kt
            │                   ├───main/
            │                   │   ├───MainScreen.kt
            │                   │   └───InviteDialog.kt
            │                   ├───map/
            │                   │   ├───MapScreen.kt
            │                   │   ├───MapViewModel.kt
            │                   │   ├───MapViewModelFactory.kt
            │                   │   └───MarkerEditDialog.kt
            │                   ├───navigation/
            │                   │   ├───AppNavigation.kt
            │                   │   └───LoginViewModelFactory.kt
            │                   ├───calendar/
            │                   │   ├───CalendarScreen.kt
            │                   │   └───CalendarViewModel.kt
            │                   ├───checklist/
            │                   │   └───ChecklistScreen.kt
            │                   ├───memo/
            │                   │   ├───MemoScreen.kt
            │                   │   └───MemoViewModel.kt
            │                   └───settings/
            │                       ├───SettingsScreen.kt
            │                       └───UserPreferencesRepository.kt
            └───res/
                ├───drawable/...
                ├───mipmap-anydpi-v26/...
                ├───mipmap/...
                ├───values/...
                └───xml/...
```

## 주요 파일 및 역할

*   **`MainActivity.kt`**: 앱의 메인 진입점 액티비티입니다. Compose UI를 설정하고 앱의 전반적인 내비게이션을 담당하는 [AppNavigation]을 호출합니다. 네이버 지도 SDK의 클라이언트 ID를 로그에 출력하고 디버그 모드를 활성화하여 상세 로그를 확인할 수 있도록 합니다.

*   **`data/map/MapRepository.kt`**: 지도 관련 데이터를 Firestore와 연동하여 관리하는 Repository 클래스입니다. 지도 생성, 업데이트, 삭제 및 사용자별 지도 목록을 가져오는 기능을 담당합니다.

*   **`data/map/MarkerRepository.kt`**: 지도 마커 관련 데이터를 Firestore와 연동하여 관리하는 Repository 클래스입니다. 마커 생성, 업데이트, 삭제 및 특정 지도에 속한 마커 목록을 가져오는 기능을 담당합니다.

*   **`data/model/Map.kt`**: 지도 데이터를 나타내는 데이터 클래스입니다. 지도의 ID, 이름, 소유자 ID 등을 포함합니다.

*   **`data/model/Marker.kt`**: 지도 마커 데이터를 나타내는 데이터 클래스입니다. 마커의 ID, 소속 지도 ID, 위치(위도, 경도), 제목, 설명 등을 포함합니다.

*   **`data/model/MemoItem.kt`**: 메모 항목 데이터를 나타내는 데이터 클래스입니다. 메모의 ID, 내용, 생성 시간 등을 포함합니다.

*   **`data/preferences/UserPreferencesRepository.kt`**: 사용자 설정(예: 다크 모드 활성화 여부)을 DataStore를 사용하여 저장하고 관리하는 Repository입니다.

*   **`ui/login/AuthRepository.kt`**: Firebase Authentication을 사용하여 사용자 인증(로그인, 로그아웃 등)을 처리하는 Repository입니다. Google 로그인과 같은 소셜 로그인 연동을 담당합니다.

*   **`ui/login/LoginScreen.kt`**: 로그인 화면의 UI를 정의하는 Composable 함수입니다. Google, Kakao, Naver 로그인 버튼을 포함하며, 각 버튼 클릭 시 해당 로그인 흐름을 시작합니다. 로그인 성공 시 메인 화면으로 이동합니다.

*   **`ui/login/LoginViewModel.kt`**: 로그인 화면의 UI 상태 및 비즈니스 로직을 관리하는 ViewModel입니다. [AuthRepository]를 통해 실제 인증 작업을 수행하고, 로그인 상태를 UI에 노출합니다.

*   **`ui/main/MainScreen.kt`**: 앱의 메인 화면 UI를 정의하는 Composable 함수입니다. 하단 내비게이션 바를 포함하며, 각 탭에 따라 다른 화면(지도, 캘린더, 메모, 설정)을 표시합니다.

*   **`ui/main/InviteDialog.kt`**: 사용자 초대를 위한 다이얼로그 UI를 정의하는 Composable 함수입니다.

*   **`ui/map/MapScreen.kt`**: 네이버 지도를 표시하고 마커를 관리하는 Composable 함수입니다. 지도 클릭 시 마커 추가 다이얼로그를 띄우고, 기존 마커 클릭 시 수정 다이얼로그를 띄웁니다.

*   **`ui/map/MapViewModel.kt`**: 지도 화면의 UI 상태 및 비즈니스 로직을 관리하는 ViewModel입니다. [MapRepository]와 [MarkerRepository]를 통해 지도 및 마커 데이터를 관리합니다. 현재 선택된 지도 ID와 마커 추가 위치를 상태로 노출합니다.

*   **`ui/map/MapViewModelFactory.kt`**: [MapViewModel]을 생성하기 위한 팩토리 클래스입니다. 의존성 주입(MapRepository, MarkerRepository)을 처리합니다.

*   **`ui/map/MarkerEditDialog.kt`**: 마커 추가 또는 수정을 위한 다이얼로그 UI를 정의하는 Composable 함수입니다. 마커의 제목과 설명을 입력받습니다.

*   **`ui/navigation/AppNavigation.kt`**: 앱의 내비게이션 그래프를 정의하는 Composable 함수입니다. [NavHost]를 사용하여 다양한 화면(스플래시, 로그인, 메인) 간의 전환을 관리합니다. 로그인 상태에 따라 초기 화면을 결정합니다.

*   **`ui/navigation/LoginViewModelFactory.kt`**: [LoginViewModel]을 생성하기 위한 팩토리 클래스입니다. 의존성 주입(AuthRepository)을 처리합니다.

*   **`ui/calendar/CalendarScreen.kt`**: 캘린더 화면의 UI를 정의하는 Composable 함수입니다. 현재는 기본적인 텍스트만 표시됩니다.

*   **`ui/calendar/CalendarViewModel.kt`**: 캘린더 화면의 UI 상태 및 비즈니스 로직을 관리하는 ViewModel입니다. 현재는 기본적인 구조만 정의되어 있습니다.

*   **`ui/checklist/ChecklistScreen.kt`**: 체크리스트 화면의 UI를 정의하는 Composable 함수입니다. 현재는 기본적인 텍스트만 표시됩니다.

*   **`ui/memo/MemoScreen.kt`**: 메모 화면의 UI를 정의하는 Composable 함수입니다. 현재는 기본적인 텍스트만 표시됩니다.

*   **`ui/memo/MemoViewModel.kt`**: 메모 화면의 UI 상태 및 비즈니스 로직을 관리하는 ViewModel입니다. 현재는 기본적인 구조만 정의되어 있습니다.

*   **`ui/settings/SettingsScreen.kt`**: 설정 화면의 UI를 정의하는 Composable 함수입니다. 현재는 기본적인 텍스트만 표시됩니다.

*   **`ui/theme/Color.kt`**: 앱의 색상 팔레트를 정의하는 파일입니다. Material Design 3의 색상 시스템에 따라 기본 색상들을 정의합니다.

*   **`ui/theme/Theme.kt`**: 앱의 Material Design 테마를 정의하는 파일입니다. 다크 모드 지원 및 동적 색상(Android 12 이상)을 설정합니다.

*   **`ui/theme/Type.kt`**: 앱의 타이포그래피(글꼴 스타일)를 정의하는 파일입니다. Material Design 3의 타이포그래피 시스템에 따라 텍스트 스타일을 정의합니다.

## 빌드 및 실행 방법

1.  **Firebase 설정**: `google-services.json` 파일의 `package_name`에 맞춰 프로젝트의 `applicationId`와 `namespace`가 `com.bamtori.chestnutmap`으로 변경되었습니다. `google-services.json` 파일은 `app/` 디렉토리에 위치해야 합니다.

2.  **Gradle Wrapper 실행 권한 부여**: 프로젝트 루트 디렉토리에서 다음 명령어를 실행하여 `gradlew` 파일에 실행 권한을 부여합니다.
    ```bash
    chmod +x gradlew
    ```

3.  **앱 빌드**: 다음 명령어를 실행하여 디버그 버전을 빌드합니다.
    ```bash
    ./gradlew assembleDebug
    ```
    빌드가 성공적으로 완료되면 `app/build/outputs/apk/debug/app-debug.apk` 파일이 생성됩니다.

4.  **Android Studio에서 실행**: Android Studio를 열고 프로젝트를 임포트합니다. 상단 툴바에서 실행할 기기(에뮬레이터 또는 실제 기기)를 선택한 후, 녹색 재생 버튼을 클릭하여 앱을 실행합니다.

    *   **Gradle 동기화 문제 발생 시**: `File` > `Sync Project with Gradle Files`를 클릭하여 Gradle 동기화를 수동으로 진행합니다.

    *   **메모리 부족 오류 발생 시**: `gradle.properties` 파일에 다음 설정을 추가하여 Gradle 데몬의 메모리를 늘릴 수 있습니다.
        ```properties
        org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8
        ```