package com.bamtori.chestnutmap.ui.memo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 메모 화면의 UI를 정의하는 Composable 함수입니다.
 * 현재는 기본적인 텍스트만 표시됩니다.
 */
@Composable
fun MemoScreen(
    memoViewModel: MemoViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "메모 화면")
        // TODO: Implement memo list and input UI here
    }
}