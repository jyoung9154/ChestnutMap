package com.bamtori.chestnutmap.ui.login

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bamtori.chestnutmap.ui.navigation.LoginViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

/**
 * 로그인 화면의 UI를 정의하는 Composable 함수입니다.
 * Google, Kakao, Naver 로그인 버튼을 포함하며, 각 버튼 클릭 시 해당 로그인 흐름을 시작합니다.
 * 로그인 성공 시 메인 화면으로 이동합니다.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(AuthRepository()))
) {
    val context = LocalContext.current
    // Google Sign-In 결과를 처리하기 위한 ActivityResultLauncher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { idToken ->
                loginViewModel.signInWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            // Google 로그인 실패 시 오류 메시지를 토스트로 표시하고 스택 트레이스를 출력합니다.
            e.printStackTrace()
            Toast.makeText(context, "Google 로그인 실패: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    // 로그인 상태 변화를 감지하여 로그인 성공 시 [onLoginSuccess] 콜백을 호출합니다.
    LaunchedEffect(loginViewModel.isLoggedIn.value) {
        if (loginViewModel.isLoggedIn.value) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Chestnut Map", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        // Google 로그인 버튼
        Button(onClick = {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(com.bamtori.chestnutmap.R.string.default_web_client_id)) // Firebase 프로젝트에서 생성된 웹 클라이언트 ID 사용
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }) {
            Image(
                painter = rememberVectorPainter(Icons.Default.AccountCircle),
                contentDescription = "Google Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Google로 로그인")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Kakao 로그인 버튼 (기능 미구현)
        Button(onClick = {
            Toast.makeText(context, "카카오 로그인 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
        }) {
            Image(
                painter = rememberVectorPainter(Icons.Default.ChatBubble),
                contentDescription = "Kakao Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("카카오로 로그인")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Naver 로그인 버튼 (기능 미구현)
        Button(onClick = {
            Toast.makeText(context, "네이버 로그인 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
        }) {
            Image(
                painter = rememberVectorPainter(Icons.Default.Public),
                contentDescription = "Naver Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("네이버로 로그인")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginSuccess = {})
}
