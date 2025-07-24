package com.bamtori.chestnutmap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bamtori.chestnutmap.ui.login.AuthRepository
import com.bamtori.chestnutmap.ui.login.LoginScreen
import com.bamtori.chestnutmap.ui.login.LoginViewModel
import com.bamtori.chestnutmap.ui.main.MainScreen

/**
 * 앱의 내비게이션 그래프를 정의하는 Composable 함수입니다.
 * [NavHost]를 사용하여 다양한 화면(스플래시, 로그인, 메인) 간의 전환을 관리합니다.
 * 사용자의 로그인 상태에 따라 초기 화면을 결정합니다.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authRepository = AuthRepository()
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(authRepository))

    // 내비게이션 호스트를 정의하고 시작 지점을 "splash"로 설정합니다.
    NavHost(navController = navController, startDestination = "splash") {
        // 스플래시 화면 정의
        composable("splash") {
            // TODO: 스플래시 화면 UI 구현이 필요합니다.
            // 로그인 상태를 확인하여 로그인 또는 메인 화면으로 이동합니다.
            if (authRepository.getCurrentUser() != null) {
                // 사용자가 로그인되어 있으면 메인 화면으로 이동하고 스플래시 화면을 스택에서 제거합니다.
                navController.navigate("main") { popUpTo("splash") { inclusive = true } }
            } else {
                // 사용자가 로그인되어 있지 않으면 로그인 화면으로 이동하고 스플래시 화면을 스택에서 제거합니다.
                navController.navigate("login") { popUpTo("splash") { inclusive = true } }
            }
        }
        // 로그인 화면 정의
        composable("login") {
            LoginScreen(onLoginSuccess = {
                // 로그인 성공 시 메인 화면으로 이동하고 로그인 화면을 스택에서 제거합니다.
                navController.navigate("main") { popUpTo("login") { inclusive = true } }
            })
        }
        // 메인 화면 정의
        composable("main") {
            MainScreen()
        }
    }
}
