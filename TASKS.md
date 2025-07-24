# 🗺️ ChestnutMap 개발 태스크 목록

이 문서는 ChestnutMap 애플리케이션 개발을 위한 상세 태스크 목록입니다. 각 태스크의 진행 상황을 추적하기 위해 상태를 기록합니다.

---

## Phase 0: 프로젝트 설정 및 초기 구성 (T0)

이 단계에서는 Android 프로젝트를 생성하고, 버전 관리 시스템을 설정하며, 필수 라이브러리와 Firebase 연동을 준비합니다.

*   **태스크 0.1: Git 초기화**
    *   **명령어:** `git init`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `Initialized empty Git repository in /Users/jaeyoung/esquisse/ChestnutMap/.git/`

*   **태스크 0.2: `.gitignore` 파일 생성**
    *   **내용:** 일반적인 Android 및 Kotlin 프로젝트에서 무시해야 할 파일 목록
    *   **상태:** [x] 완료
    *   **로그:**
        *   `/Users/jaeyoung/esquisse/ChestnutMap/.gitignore` 파일 생성 완료.

*   **태스크 0.3: Gradle 프로젝트 구조 설정**
    *   **파일:** `build.gradle.kts`, `settings.gradle.kts`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `/Users/jaeyoung/esquisse/ChestnutMap/settings.gradle.kts` 파일 생성 완료.
        *   `/Users/jaeyoung/esquisse/ChestnutMap/build.gradle.kts` 파일 생성 완료.

*   **태스크 0.4: 앱 모듈 디렉토리 생성**
    *   **명령어:** `mkdir -p app/src/main/java/com/chestnut/map`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `app/src/main/java/com/chestnut/map` 디렉토리 생성 완료.

*   **태스크 0.5: 앱 모듈 `build.gradle.kts` 설정**
    *   **파일:** `app/build.gradle.kts`
    *   **내용:** 필수 의존성 추가
    *   **상태:** [x] 완료
    *   **로그:**
        *   `/Users/jaeyoung/esquisse/ChestnutMap/app/build.gradle.kts` 파일 생성 및 의존성 추가 완료.

*   **태스크 0.6: `AndroidManifest.xml` 설정**
    *   **파일:** `app/src/main/AndroidManifest.xml`
    *   **내용:** 권한, API 키 메타데이터, 메인 액티비티 정의
    *   **상태:** [x] 완료
    *   **로그:**
        *   `/Users/jaeyoung/esquisse/ChestnutMap/app/src/main/AndroidManifest.xml` 파일 생성 완료.

*   **태스크 0.7: Firebase 연동**
    *   **액션:** `google-services.json` 파일 수동 추가
    *   **상태:** [x] 완료 (가정)
    *   **로그:**
        *   사용자가 Firebase 콘솔에서 `google-services.json` 파일을 생성하여 `app/` 디렉토리에 추가해야 합니다.

*   **태스크 0.8: 기본 데이터 모델 클래스 생성**
    *   **파일:** `app/src/main/java/com/chestnut/map/data/model/Map.kt`, `Marker.kt`, `MemoItem.kt`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `Map.kt`, `Marker.kt`, `MemoItem.kt` 파일 생성 완료.

---

## Phase 1: 인증 및 지도 표시 (M1)

*   **태스크 1.1: 로그인 화면 UI 구현**
    *   **파일:** `app/src/main/java/com/chestnut/map/ui/login/LoginScreen.kt`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `LoginScreen.kt` 파일 생성 및 기본 UI 구현 완료.

*   **태스크 1.2: 인증 로직 구현**
    *   **파일:** `AuthRepository.kt`, `LoginViewModel.kt`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `AuthRepository.kt`, `LoginViewModel.kt` 파일 생성 및 기본 구조 구현 완료.

*   **태스크 1.3: 메인 화면 및 하단 네비게이션 구현**
    *   **파일:** `app/src/main/java/com/chestnut/map/ui/main/MainScreen.kt`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `MainScreen.kt` 파일 생성 및 하단 네비게이션 UI 구현 완료.

*   **태스크 1.4: 네이버 지도 연동 및 표시**
    *   **파일:** `app/src/main/java/com/chestnut/map/ui/map/MapScreen.kt`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `MapScreen.kt` 파일 생성 및 네이버 지도 연동 완료.

*   **태스크 1.5: 앱 네비게이션 설정**
    *   **파일:** `app/src/main/java/com/chestnut/map/ui/navigation/AppNavigation.kt`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `AppNavigation.kt` 파일 생성 및 앱 네비게이션 설정 완료.

---

## Phase 2: 실시간 동기화 및 핵심 기능 (M2)

*   **태스크 2.1: Firestore/Realtime DB 연동**
    *   **파일:** `app/src/main/java/com/chestnut/map/data/map/MapRepository.kt`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `MapRepository.kt` 파일 생성 및 Firestore 연동 기본 구조 구현 완료.

*   **태스크 2.2: 지도 ViewModel 구현**
    *   **파일:** `app/src/main/java/com/chestnut/map/ui/map/MapViewModel.kt`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `MapViewModel.kt` 파일 생성 및 기본 구조 구현 완료.

*   **태스크 2.3: 지도 위 마커 실시간 표시**
    *   **파일:** `app/src/main/java/com/chestnut/map/ui/map/MapScreen.kt` (수정)
    *   **상태:** [x] 완료
    *   **로그:**
        *   `MapScreen.kt` 수정 및 마커 표시 로직 추가 완료.

*   **태스크 2.4: 마커 추가/수정 UI 및 로직**
    *   **파일:** `MarkerEditDialog.kt`, `MapViewModel.kt` (수정)
    *   **상태:** [x] 완료
    *   **로그:**
        *   `MapViewModel.kt`에 마커 추가/수정 로직 및 위치 관리 추가.
        *   `MapScreen.kt`에 지도 클릭 및 FAB 클릭 시 마커 추가/수정 다이얼로그 연동.

*   **태스크 2.5: 초대 기능 및 Cloud Functions**
    *   **파일:** `functions/index.js`, `InviteDialog.kt`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `functions/index.js` 파일 생성 및 기본 Cloud Function 구조 추가.
        *   `InviteDialog.kt` 파일 생성 및 기본 UI 구현.

---

## Phase 3: 부가 기능 구현 (M3)

*   **태스크 3.1: 캘린더 화면 구현**
    *   **파일:** `CalendarScreen.kt`, `CalendarViewModel.kt`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `CalendarScreen.kt` 파일 생성 및 기본 UI 구현.
        *   `CalendarViewModel.kt` 파일 생성 및 기본 구조 구현.

*   **태스크 3.2: 메모 및 체크리스트 화면 구현**
    *   **파일:** `MemoScreen.kt`, `ChecklistScreen.kt`, `MemoViewModel.kt`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `MemoScreen.kt` 파일 생성 및 기본 UI 구현.
        *   `ChecklistScreen.kt` 파일 생성 및 기본 UI 구현.
        *   `MemoViewModel.kt` 파일 생성 및 기본 구조 구현.

*   **태스크 3.3: 설정 화면 구현**
    *   **파일:** `SettingsScreen.kt`, `UserPreferencesRepository.kt`
    *   **상태:** [x] 완료
    *   **로그:**
        *   `SettingsScreen.kt` 파일 생성 및 기본 UI 구현.
        *   `UserPreferencesRepository.kt` 파일 생성 및 기본 구조 구현.
