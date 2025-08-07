// 프로젝트 루트/settings.gradle.kts

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // 리포지토리를 한 곳에서만 관리하고 프로젝트별 선언 금지
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven( url = "https://jitpack.io")
    }
}

rootProject.name = "ChestnutMap"
include(":app")
