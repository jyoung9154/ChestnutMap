package com.bamtori.chestnutmap.ui.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Firebase Authentication을 사용하여 사용자 인증(로그인, 로그아웃 등)을 처리하는 Repository입니다.
 * Google 로그인과 같은 소셜 로그인 연동을 담당합니다.
 */
class AuthRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * 현재 로그인된 Firebase 사용자 정보를 반환합니다.
     * @return 현재 로그인된 FirebaseUser 객체 또는 로그인되어 있지 않으면 null
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Google ID 토큰을 사용하여 Firebase에 로그인합니다.
     * @param idToken Google 로그인 시 발급받은 ID 토큰
     * @return 로그인 성공 시 FirebaseUser 객체, 실패 시 null
     */
    suspend fun signInWithGoogle(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return try {
            firebaseAuth.signInWithCredential(credential).await().user
        } catch (e: Exception) {
            e.printStackTrace() // 오류 발생 시 스택 트레이스를 출력하여 디버깅에 도움을 줍니다.
            null
        }
    }

    /**
     * 현재 사용자를 로그아웃합니다.
     */
    fun signOut() {
        firebaseAuth.signOut()
    }
}
