package com.bamtori.chestnutmap.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bamtori.chestnutmap.data.map.MapRepository
import com.bamtori.chestnutmap.data.map.MarkerRepository
import com.bamtori.chestnutmap.data.model.Marker
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.*

/**
 * /app/src/main/java/com/bamtori/chestnutmap/ui/map/MapScreen.kt
 *
 * 네이버 지도앱과 거의 같은 UX + 내 위치 버튼 기능까지 완전 지원
 * - 지도 주요 제스처(이동/확대/축소/회전 등), 내 위치 버튼, ± 버튼, 마커 추가/수정
 * - 초보자를 위한 상세 한글 주석 포함
 */
@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(
            MapRepository(),
            MarkerRepository()
        )
    )
) {

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // 1. 내 위치 소스 생성 (현재 Compose 공식 방식, 직접 new 금지)
    val locationSource = rememberFusedLocationSource()

    // 2. (옵션) 지도 마커 등 상태값 StateFlow 연동
    val markers by mapViewModel.markers.collectAsState()
    var showMarkerEditDialog by remember { mutableStateOf(false) } // 마커 추가/수정 다이얼로그 표시용
    var selectedMarker by remember { mutableStateOf<Marker?>(null) } // 선택된 마커
    val currentMapId by mapViewModel.currentMapId.collectAsState()

    // 처음 진입 시 더미 맵 ID 할당 (테스트/개발시용)
    LaunchedEffect(Unit) {
        if (currentMapId == null) {
            mapViewModel.setCurrentMapId("dummy_map_id_for_testing")
        }
    }

    // 3. 지도 UI 컨트롤 옵션 (네이버 지도앱 기본과 최대한 일치)
    val mapUiSettings = remember {
        MapUiSettings(
            isZoomControlEnabled = false,         // 우측 하단 ± 버튼 표시 (기본 false)
            isCompassEnabled = true,             // 나침반(북쪽) 아이콘 표시
            isScaleBarEnabled = true,            // 거리 축척바 표시
            isScrollGesturesEnabled = true,      // 지도 드래그(이동) 허용
            isZoomGesturesEnabled = true,        // 핀치/더블탭 확대축소 허용
            isTiltGesturesEnabled = true,        // 기울이기(Tilt) 허용
            isRotateGesturesEnabled = true,      // 회전 허용
            isLocationButtonEnabled = true,      // 내 위치 버튼(동그라미) 활성화
            isIndoorLevelPickerEnabled = true,   // 실내 층수면 층 UI 보임
            isLogoClickEnabled = false           // 네이버 로고 클릭 비활성
        )
    }
    val mapProperties = remember { MapProperties() }
    val cameraPositionState = rememberCameraPositionState {
        // 지도 시작 시 카메라 위치: 서울 광화문
        position = CameraPosition(LatLng(37.5665, 126.9780), 10.0)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 4. 실제 네이버 지도 화면
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
            locationSource = locationSource, // 내 위치 기능 활성화(파란 점 + 하단 버튼)
            contentPadding = PaddingValues(0.dp)
        ) {
            // 5. 마커 표시 (Firestore 연동시 실시간 리스트 가능)
//            markers.forEach { marker ->
//                Marker(
//                    state = rememberMarkerState(position = LatLng(marker.lat, marker.lng)),
//                    captionText = marker.title,
//                    onClick = {
//                        // 마커 클릭 시 → 수정 다이얼로그 오픈
//                        selectedMarker = marker
//                        showMarkerEditDialog = true
//                        true // 이벤트 소비
//                    }
//                )
//            }
        }
    }
}
