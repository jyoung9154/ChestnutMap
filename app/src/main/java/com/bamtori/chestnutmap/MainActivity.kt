package com.bamtori.chestnutmap

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.bamtori.chestnutmap.ui.navigation.AppNavigation
import com.bamtori.chestnutmap.ui.theme.ChestnutMapTheme
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.NaverMapSdk.AuthFailedException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 네이버 지도 SDK 클라이언트 ID를 안전하게 확인 (실무 권장: NCP_KEY_ID 사용)
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val naverKeyId = appInfo.metaData.getString("com.naver.maps.map.NCP_KEY_ID")
            Log.d("NaverMap", "NCP_KEY_ID from Manifest = $naverKeyId")
        } catch (e: Exception) {
            Log.e("NaverMap", "Failed to get NCP_KEY_ID", e)
        }

        // 2. SDK 초기화 & 인증실패 리스너 등록 (Compose-Activity 생명주기 안전)
        NaverMapSdk.getInstance(this).setOnAuthFailedListener { exception: NaverMapSdk.AuthFailedException ->
            Log.e("NaverMap", "Auth Failed: ${exception.errorCode} - ${exception.message}")

            // 실 앱 환경에서는 사용자 안내 필수!
            runOnUiThread {
                Toast.makeText(
                    this,
                    "네이버 지도 인증 실패: ${exception.message ?: "원인 미상"} (코드: ${exception.errorCode})",
                    Toast.LENGTH_LONG
                ).show()
            }
            // 추가 대응: 에러 전용 화면 노출, 종료 등 비즈니스에 맞게 분기처리
        }

        // Compose 기반 UI 셋업
        setContent {
            ChestnutMapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
