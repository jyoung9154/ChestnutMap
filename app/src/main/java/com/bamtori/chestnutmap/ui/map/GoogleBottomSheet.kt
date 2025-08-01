package com.bamtori.chestnutmap.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bamtori.chestnutmap.data.model.GooglePlaceInfo
import com.bamtori.chestnutmap.data.network.ApiHelper
import android.util.Log
import com.google.android.gms.maps.model.LatLng

/**
 * GoogleMap 롱클릭한 위치(latLng)를 기준으로
 * Google Places API Nearby Search를 호출하여 주변 장소 목록을 받아오고,
 * 해당 장소 정보를 바텀시트로 리스트 형식 출력하는 Compose 컴포저블
 *
 * @param latLng      길게 클릭한 위치 좌표 (위도/경도)
 * @param sheetState  바텀시트 상태 (show/hide 제어용)
 * @param onDismiss   바텀시트 닫기 시 호출 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GooglePlaceBottomSheet(
    latLng: LatLng,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    // 장소 목록 상태: 초기값은 빈 리스트
    var placeList by remember { mutableStateOf<List<GooglePlaceInfo>>(emptyList()) }
    // 네트워크 호출 로딩 상태 표시
    var loading by remember { mutableStateOf(false) }
    // 에러 메시지를 표시할 때 사용 (null이면 에러 없음)
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // latLng 좌표가 변경될 때마다 해당 위치 주변 장소 검색 요청 수행
    LaunchedEffect(latLng) {
        loading = true        // 호출 시작 → 로딩 상태 true
        errorMsg = null       // 작업 전 에러 메시지 초기화
        try {
            // ApiHelper의 searchNearbyPlaces 함수는 현재 위치 주변 장소 리스트를 반환
            placeList = ApiHelper.searchNearbyPlaces(latLng.latitude, latLng.longitude)
            if (placeList.isEmpty()) { // 빈 리스트라면 사용자 안내 메시지 세팅
                errorMsg = "가까운 장소 정보를 찾지 못했습니다."
            }
        } catch (e: Exception) {
            // 네트워크 호출 실패, 예외 로그 기록 후 사용자용 에러 메시지 세팅
            Log.e("GooglePlaceBottomSheet", "장소 정보 조회 실패", e)
            errorMsg = "장소 정보를 불러오지 못했습니다."
            placeList = emptyList() // 안전하게 빈 리스트 초기화
        } finally {
            loading = false   // 호출 종료 → 로딩 상태 false
        }
    }

    // Compose Material3 ModalBottomSheet : 바텀시트 UI 렌더링
    ModalBottomSheet(
        onDismissRequest = onDismiss,  // 바텀시트 외부 터치 시 호출 (닫기 로직 연결)
        sheetState = sheetState         // 외부에서 상태 제어 가능
    ) {
        // 바텀시트 콘텐츠를 Column으로 세로 배치, 요소간 8dp 간격 유지
        Column(
            modifier = Modifier
                .padding(16.dp)        // 적당한 패딩 적용
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // UI 상태별로 분기 처리
            when {
                // 1) 로딩 중 UI 구성
                loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp)) // 작은 로딩 인디케이터 표시
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("장소 정보 불러오는 중...") // 사용자 안내 텍스트
                    }
                }

                // 2) 에러 메시지 존재 시 출력
                errorMsg != null -> {
                    Text(
                        text = errorMsg ?: "",
                        color = MaterialTheme.colorScheme.error,  // 빨간색 (Material3 error 색상)
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // 3) 정상적으로 장소 리스트가 있을 때 리스트 표시
                placeList.isNotEmpty() -> {
                    // 리스트 상단에 장소 개수 안내 문구 표시
                    Text(
                        text = "${placeList.size}개의 장소가 검색되었습니다.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // LazyColumn을 이용하여 스크롤 가능한 리스트 UI 구현,
                    // 전체 바텀시트 높이 대비 70%로 제한해 적절한 높이 유지
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight(0.7f)
                    ) {
                        // 리스트 아이템 각각을 GooglePlaceInfo 객체로 매핑하여 렌더링
                        items(placeList, key = { it.placeId ?: it.name ?: it.address ?: "" }) { place ->
                            // 각 아이템 UI 컴포저블 호출, 클릭 시 onPlaceClick 람다 호출 가능
                            PlaceListItem(place = place)
                            Divider() // 아이템 간 구분선 표시
                        }
                    }
                }

                // 4) 나머지 케이스 (빈 리스트 및 초기 상태)
                else -> {
                    Text(
                        text = "장소 정보를 불러올 수 없습니다.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * 한 장소 아이템 UI 렌더링
 *
 * @param place GooglePlaceInfo 데이터
 * @param onClick 클릭 시 동작 (필요시 설정 가능, 기본은 빈 람다)
 */
@Composable
fun PlaceListItem(
    place: GooglePlaceInfo,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }    // 전체 Row 클릭 가능
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 장소 대표 사진이 있으면 왼쪽에 표시 (64dp 크기)
        place.photoUrl?.let { photoUrl ->
            AsyncImage(
                model = photoUrl,
                contentDescription = place.name ?: "장소 사진",
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        // 텍스트 내용 영역 : 이름, 주소, 별점, 카테고리 출력
        Column(
            modifier = Modifier.weight(1f) // 가용 너비 모두 사용
        ) {
            // 장소 이름(없으면 "이름 없음" 표시), 볼드체와 타이틀 크기 적용
            Text(
                text = place.name ?: "이름 없음",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            // 주소가 있을 때만 표시, 보통 작게 표시
            place.address?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // 별점과 카테고리 행
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                // 별점이 0 이상인 경우만 표시
                if (place.rating != null) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "별점",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", place.rating),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 카테고리 표시 (ex: restaurant, cafe)
                place.category?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
