package com.bamtori.chestnutmap.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bamtori.chestnutmap.data.model.Marker

/**
 * 마커 추가 또는 수정을 위한 다이얼로그 UI를 정의하는 Composable 함수입니다.
 * 마커의 제목과 설명을 입력받습니다.
 *
 * @param marker 편집할 기존 마커 객체 (새로운 마커 추가 시 null)
 * @param onDismiss 다이얼로그가 닫힐 때 호출되는 콜백
 * @param onConfirm 마커 저장 (추가 또는 수정) 시 호출되는 콜백. 변경된 마커 객체를 반환합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkerEditDialog(
    marker: Marker? = null,
    onDismiss: () -> Unit,
    onConfirm: (Marker) -> Unit
) {
    var title by remember { mutableStateOf(marker?.title ?: "") }
    var description by remember { mutableStateOf(marker?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (marker == null) "마커 추가" else "마커 수정") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("설명") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newMarker = marker?.copy(title = title, description = description) ?: Marker(
                    markerId = java.util.UUID.randomUUID().toString(),
                    mapId = "", // TODO: 실제 mapId로 대체
                    lat = 0.0, // TODO: 실제 위도/경도로 대체
                    lng = 0.0,
                    title = title,
                    description = description
                )
                onConfirm(newMarker)
            }) {
                Text(if (marker == null) "추가" else "수정")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
