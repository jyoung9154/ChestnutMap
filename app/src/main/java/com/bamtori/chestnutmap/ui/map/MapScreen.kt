package com.bamtori.chestnutmap.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bamtori.chestnutmap.data.map.MapRepository
import com.bamtori.chestnutmap.data.map.MarkerRepository
import com.bamtori.chestnutmap.data.model.Marker
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.Marker as NaverMarker // Alias for Naver Map's Marker
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Overlay.OnClickListener // Import OnClickListener
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.CameraPosition

/**
 * 네이버 지도를 표시하고 마커를 관리하는 Composable 함수입니다.
 * 지도 클릭 시 마커 추가 다이얼로그를 띄우고, 기존 마커 클릭 시 수정 다이얼로그를 띄웁니다.
 */
@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(MapRepository(), MarkerRepository())
    )
) {
    // MapViewModel에서 마커 목록, 마커 편집 다이얼로그 표시 여부, 선택된 마커, 현재 지도 ID 등의 상태를 관찰합니다.
    val markers by mapViewModel.markers.collectAsState()
    var showMarkerEditDialog by remember { mutableStateOf(false) }
    var selectedMarker by remember { mutableStateOf<Marker?>(null) }
    val currentMapId by mapViewModel.currentMapId.collectAsState()

    // 임시: 현재 지도 ID가 설정되지 않은 경우, 테스트를 위해 더미 ID를 설정합니다.
    // 실제 앱에서는 내비게이션을 통해 전달되거나 사용자 기본 지도를 로드하는 방식으로 관리되어야 합니다.
    LaunchedEffect(Unit) {
        if (currentMapId == null) {
            mapViewModel.setCurrentMapId("dummy_map_id_for_testing")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 네이버 지도 컴포저블
        val cameraPositionState = rememberCameraPositionState {
            position = com.naver.maps.map.CameraPosition(LatLng(37.5665, 126.9780), 10.0) // 서울
        }

        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            // 지도 클릭 시 마커 추가 위치를 설정하고 마커 편집 다이얼로그를 띄웁니다.
            onMapClick = { _, latLng ->
                mapViewModel.setMarkerAddLocation(latLng.latitude, latLng.longitude)
                selectedMarker = null // 새로운 마커 추가 모드
                showMarkerEditDialog = true
            }
        ) {
            // ViewModel에서 관찰하는 마커 목록을 지도에 표시합니다.
            markers.forEach { marker ->
                NaverMarker( // 네이버 지도 마커 컴포저블
                    state = com.naver.maps.map.compose.rememberMarkerState(position = LatLng(marker.lat, marker.lng)),
                    captionText = marker.title,
                    // 마커 클릭 시 해당 마커를 선택하고 마커 편집 다이얼로그를 띄웁니다.
                    onClick = { // Remove explicit type for 'it'
                        selectedMarker = marker
                        showMarkerEditDialog = true
                        true
                    }
                )
            }
        }

        // 마커 추가 Floating Action Button
        FloatingActionButton(
            onClick = {
                mapViewModel.clearMarkerAddLocation() // 이전 클릭 위치 초기화
                selectedMarker = null // 새로운 마커 추가 모드
                showMarkerEditDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "마커 추가")
        }

        // 마커 편집/추가 다이얼로그 표시
        if (showMarkerEditDialog) {
            MarkerEditDialog(
                marker = selectedMarker,
                onDismiss = { showMarkerEditDialog = false }, // 다이얼로그 닫기
                onConfirm = { marker -> // 마커 저장 (추가 또는 수정)
                    mapViewModel.saveMarker(marker)
                    showMarkerEditDialog = false
                }
            )
        }
    }
}
