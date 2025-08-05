package com.bamtori.chestnutmap.ui.map

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bamtori.chestnutmap.data.map.MapRepository
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import kotlinx.coroutines.launch
import com.google.android.gms.maps.model.CameraPosition as GoogleCameraPosition
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * 지도 화면을 표시하는 메인 Composable 함수입니다.
 * Google 지도, 장소 검색, POI 클릭 및 장소 정보 표시 기능을 포함합니다.
 *
 * @param mapViewModel 지도 관련 데이터와 로직을 처리하는 ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(
            MapRepository()
        )
    )
) {
    // 현재 선택된 지도의 ID를 State로 관리합니다.
    val currentMapId by mapViewModel.currentMapId.collectAsState()

    // 컴포저블이 처음 로드될 때 테스트용 맵 ID를 설정합니다.
    LaunchedEffect(Unit) {
        if (currentMapId == null) {
            mapViewModel.setCurrentMapId("dummy_map_id_for_testing")
        }
    }

    // 현재 컨텍스트를 가져옵니다.
    val context = LocalContext.current

    // 코루틴 스코프를 생성하여 비동기 작업을 관리합니다.
    val scope = rememberCoroutineScope()
    // 바텀시트의 상태를 관리합니다.
    val sheetState = rememberModalBottomSheetState()
    // 지도에서 길게 클릭하여 선택된 위치(위도, 경도)를 저장하는 State.
    var selectedLatLng by remember { mutableStateOf<GoogleLatLng?>(null) }
    // 선택된 장소(POI)의 상세 정보를 저장하는 State.
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    // Google Places API 클라이언트를 초기화합니다.
    val placesClient = remember { Places.createClient(context) }

    // --- 검색 관련 State ---
    // 검색창의 현재 텍스트를 저장하는 State.
    var searchQuery by remember { mutableStateOf("") }
    // 검색창이 활성화 상태인지 여부를 저장하는 State.
    var isSearchActive by remember { mutableStateOf(false) }
    // 장소 검색 자동완성 결과를 저장하는 State.
    var searchResults by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }

    // 지도의 카메라 위치 상태를 관리합니다. (초기 위치: 서울)
    val googleCameraPositionState = com.google.maps.android.compose.rememberCameraPositionState {
        position = GoogleCameraPosition(GoogleLatLng(37.5665, 126.9780), 10f, 0f, 0f)
    }
    // 1.5초후 자동 검색
    LaunchedEffect(searchQuery) {
        snapshotFlow { searchQuery }
            .debounce(1500)
            .filter { it.isNotBlank() }
            .collectLatest { query ->
                doSearch(
                    query = query,
                    googleCameraPositionState = googleCameraPositionState,
                    placesClient = placesClient,
                    onResult = { results -> searchResults = results }
                )
            }
    }

    // 전체 화면을 차지하는 Box 컨테이너.
    Box(modifier = Modifier.fillMaxSize()) {

        // GoogleMap Composable: 지도를 화면에 렌더링합니다.
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = googleCameraPositionState, // 지도 카메라 위치 상태
            // 지도 UI 관련 설정
            uiSettings = MapUiSettings(
                compassEnabled = true,               // 나침반 활성화
                myLocationButtonEnabled = true,    // 내 위치 버튼 활성화
                zoomControlsEnabled = false,         // 확대/축소 컨트롤 비활성화 (제스처로만 가능)
                indoorLevelPickerEnabled = true,   // 실내 지도 레벨 피커 활성화
                tiltGesturesEnabled = true,        // 기울이기 제스처 활성화
                rotationGesturesEnabled = true,    // 회전 제스처 활성화
                scrollGesturesEnabled = true,      // 스크롤 제스처 활성화
                zoomGesturesEnabled = true,        // 줌 제스처 활성화
            ),
            // 지도 속성 설정
            properties = MapProperties(
                isMyLocationEnabled = true // 내 위치 표시 활성화
            ),
            // 지도를 클릭했을 때의 이벤트 리스너
            onMapClick = { latLng ->
                Log.d("MapClick", "Clicked location: ${latLng.latitude}, ${latLng.longitude}")
            },
            // 지도를 길게 클릭했을 때의 이벤트 리스너 (바텀시트 표시)
            onMapLongClick = { latLng ->
                Log.d("MapClick", "Long-clicked location: ${latLng.latitude}, ${latLng.longitude}")
                selectedLatLng = latLng
                scope.launch { sheetState.show() } // 바텀시트 열기
            },
            // POI(관심 장소)를 클릭했을 때의 이벤트 리스너
            onPOIClick = { poi ->
                // 가져올 장소의 상세 정보 필드를 지정합니다.
                val placeFields = listOf(
                    Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHONE_NUMBER,
                    Place.Field.RATING, Place.Field.WEBSITE_URI, Place.Field.TYPES,
                    Place.Field.PHOTO_METADATAS, Place.Field.USER_RATINGS_TOTAL
                )
                val request = FetchPlaceRequest.newInstance(poi.placeId, placeFields)
                placesClient.fetchPlace(request)
                    .addOnSuccessListener { response ->
                        selectedPlace = response.place // 성공 시 선택된 장소 정보 업데이트
                        scope.launch { sheetState.show() } // 바텀시트 열기
                    }
                    .addOnFailureListener { exception ->
                        Log.e("MapScreen", "Place not found.", exception)
                    }
            },
        ) {
            // 지도 위에 마커 등 다른 Composable을 추가할 수 있는 공간입니다.
        }

        // 검색창 Composable
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter) // 화면 상단 중앙에 배치
                .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 8.dp ),
            query = searchQuery, // 검색어 State와 바인딩
            onQueryChange = { searchQuery = it }, // 검색어가 변경될 때마다 State를 업데이트합니다.
            onSearch = { // 키보드에서 검색 액션(엔터 등)을 실행했을 때 API를 호출합니다.
                doSearch(
                    query = searchQuery,
                    googleCameraPositionState = googleCameraPositionState,
                    placesClient = placesClient,
                    onResult = { results -> searchResults = results }
                )
            },
            active = isSearchActive, // 검색창 활성 상태
            onActiveChange = { // 활성 상태가 변경될 때 호출
                isSearchActive = it
                if (!isSearchActive) {
                    searchQuery = ""
                    searchResults = emptyList() // 비활성화되면 검색 결과 초기화
                }
            },
            placeholder = { Text("장소 검색") }, // 검색창에 표시될 안내 텍스트
            shape = RoundedCornerShape(12.dp), // 모서리가 둥근 사각형 모양
            colors = SearchBarDefaults.colors(
                containerColor = Color.White // 배경색을 흰색으로 설정
            ),

            // 앞에 아이콘
            leadingIcon = {
                if (isSearchActive) {
                    IconButton(onClick = {
                        // ① 검색어 초기화
                        searchQuery = ""
                        searchResults = emptyList()
                        // ② 검색창 닫기(비활성화, 즉 지도만 보이게)
                        isSearchActive = false
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                } else {
                    IconButton(onClick = {
                        // ① 검색어 초기화
//                        searchQuery = ""
//                        searchResults = emptyList()
//                        // ② 검색창 닫기(비활성화, 즉 지도만 보이게)
//                        isSearchActive = false
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "검색")
                    }
                }
            },
            // 뒤에 아이콘
            trailingIcon = {
                // query가 있을 때만 X버튼 노출
                if (isSearchActive) {
                    IconButton(onClick = {
                        // ① 검색어 조회
                        doSearch(
                            query = searchQuery,
                            googleCameraPositionState = googleCameraPositionState,
                            placesClient = placesClient,
                            onResult = { results -> searchResults = results }
                        )
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "검색")
                    }
                }
            },
        ) {
            // 검색 결과 목록을 표시하는 LazyColumn
            LazyColumn {
                items(searchResults) { prediction ->
                    ListItem(
                        headlineContent = { Text(prediction.getPrimaryText(null).toString()) }, // 장소 이름
                        supportingContent = { Text(prediction.getSecondaryText(null).toString()) }, // 장소 주소
                        modifier = Modifier.clickable { // 항목 클릭 시 이벤트
                            isSearchActive = false // 검색창 비활성화
                            searchQuery = ""       // 검색어 초기화
                            searchResults = emptyList() // 검색 결과 초기화

                            // 클릭된 장소의 상세 정보(ID, 이름, 좌표)를 요청합니다.
                            val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
                            val request = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)

                            placesClient.fetchPlace(request)
                                .addOnSuccessListener { response ->
                                    val place = response.place
                                    place.latLng?.let { latLng ->
                                        // 해당 장소로 지도를 부드럽게 이동시킵니다.
                                        scope.launch {
                                            googleCameraPositionState.animate(
                                                update = CameraUpdateFactory.newLatLngZoom(latLng, 15f), // 15f 줌 레벨로 이동
                                                durationMs = 1000 // 1초 동안 애니메이션
                                            )
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    if (exception is ApiException && exception.statusCode == 9013) {
                                        // Place ID 만료 안내 및 재검색 권유
                                        Log.d("MapScreen","장소 정보가 만료됐습니다. 다시 검색해 주세요.")
                                    } else {
                                        Log.e("MapScreen", "Place fetch failed", exception)
                                    }
                                }

                        }
                    )
                }
            }
        }


        // --- 바텀시트 표시 로직 ---
        // 1. 지도에서 직접 위치를 선택했을 때
        if (selectedLatLng != null) {
            GooglePlaceBottomSheet(
                latLng = selectedLatLng!!,
                sheetState = sheetState,
                onDismiss = {
                    selectedLatLng = null
                    scope.launch { sheetState.hide() }
                }
            )

        }

        // 2. POI 또는 검색 결과를 통해 장소를 선택했을 때
        if (selectedPlace != null) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    selectedPlace = null
                    scope.launch { sheetState.hide() }
                }
            ) {
                PlaceDetailsBottomSheet(place = selectedPlace, placesClient = placesClient)
            }
        }
    }
}

// ------ (1) doSearch 함수 완성 ------
fun doSearch(
    query: String,
    googleCameraPositionState: CameraPositionState,
    placesClient: PlacesClient,
    onResult: (List<AutocompletePrediction>) -> Unit,
    onError: (Exception) -> Unit = {}
) {
    if (query.isBlank()) {
        onResult(emptyList())
        return
    }
    val bounds = googleCameraPositionState.projection?.visibleRegion?.latLngBounds?.let {
        RectangularBounds.newInstance(it)
    }
    if (bounds == null) {
        onResult(emptyList())
        return
    }
    val request = FindAutocompletePredictionsRequest.builder()
        .setLocationBias(bounds)
        .setQuery(query)
        .build()

    Log.d("MapScreen", "Query: $query")
    placesClient.findAutocompletePredictions(request)
        .addOnSuccessListener { response ->
            onResult(response.autocompletePredictions)
        }
        .addOnFailureListener { exception ->
            Log.e("MapScreen", "Autocomplete prediction failed", exception)
            onError(exception)
        }
}

