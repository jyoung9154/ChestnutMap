package com.bamtori.chestnutmap.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bamtori.chestnutmap.ui.login.AuthRepository
import com.bamtori.chestnutmap.ui.login.LoginViewModel

/**
 * [LoginViewModel]을 생성하기 위한 팩토리 클래스입니다.
 * [AuthRepository] 의존성을 주입하여 ViewModel을 초기화합니다.
 */
class LoginViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
