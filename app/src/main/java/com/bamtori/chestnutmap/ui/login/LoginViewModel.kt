package com.bamtori.chestnutmap.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 로그인 화면의 UI 상태 및 비즈니스 로직을 관리하는 ViewModel입니다.
 * [AuthRepository]를 통해 실제 인증 작업을 수행하고, 로그인 상태를 UI에 노출합니다.
 */
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // 로그인 상태를 나타내는 StateFlow (Initial, Loading, Success, Error)
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    // 사용자의 로그인 여부를 나타내는 StateFlow
    private val _isLoggedIn = MutableStateFlow(authRepository.getCurrentUser() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    /**
     * Google ID 토큰을 사용하여 로그인 프로세스를 시작합니다.
     * @param idToken Google 로그인 시 발급받은 ID 토큰
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading // 로그인 시도 중 상태로 변경
            val user = authRepository.signInWithGoogle(idToken)
            if (user != null) {
                _loginState.value = LoginState.Success // 로그인 성공
                _isLoggedIn.value = true // 로그인 상태 업데이트
            } else {
                _loginState.value = LoginState.Error("Google 로그인에 실패했습니다.") // 로그인 실패
                _isLoggedIn.value = false
            }
        }
    }
}

/**
 * 로그인 프로세스의 다양한 상태를 나타내는 Sealed Class입니다.
 */
sealed class LoginState {
    object Initial : LoginState() // 초기 상태
    object Loading : LoginState() // 로그인 진행 중
    object Success : LoginState() // 로그인 성공
    data class Error(val message: String) : LoginState() // 로그인 실패 (오류 메시지 포함)
}
