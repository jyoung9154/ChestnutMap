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
        factory = MapViewModelFactory(MapRepository())
    )
) {
    // 현재 지도 선택 ID 및 초기값
    val currentMapId by mapViewModel.currentMapId.collectAsState()
    LaunchedEffect(Unit) {
        if (currentMapId == null) {
            mapViewModel.setCurrentMapId("dummy_map_id_for_testing")
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    // 지도에서 선택된 위치/장소 상태
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    val placesClient = remember { Places.createClient(context) }

    // 검색 관련 state
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }

    // Google Map 상태 (초기 위치 서울)
    val googleCameraPositionState = com.google.maps.android.compose.rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition(
            com.google.android.gms.maps.model.LatLng(37.5665, 126.9780), 10f, 0f, 0f
        )
    }

    // 검색 입력시 자동완성 (debounce)
    LaunchedEffect(searchQuery) {
        snapshotFlow { searchQuery }
            .debounce(1000)
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

    Box(modifier = Modifier.fillMaxSize()) {
        // Google 지도 표시
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = googleCameraPositionState,
            uiSettings = MapUiSettings(
                compassEnabled = true,
                myLocationButtonEnabled = true,
                zoomControlsEnabled = false,
                indoorLevelPickerEnabled = true,
                tiltGesturesEnabled = true,
                rotationGesturesEnabled = true,
                scrollGesturesEnabled = true,
                zoomGesturesEnabled = true,
            ),
            properties = MapProperties(isMyLocationEnabled = true),
            // POI 클릭시 상세 조회 후 바텀시트 띄움
            onPOIClick = { poi ->
                val placeFields = listOf(
                    Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHONE_NUMBER,
                    Place.Field.RATING, Place.Field.WEBSITE_URI, Place.Field.TYPES,
                    Place.Field.PHOTO_METADATAS, Place.Field.USER_RATINGS_TOTAL,Place.Field.LAT_LNG
                )
                val request = FetchPlaceRequest.newInstance(poi.placeId, placeFields)
                placesClient.fetchPlace(request)
                    .addOnSuccessListener { response ->
                        selectedPlace = response.place
                        scope.launch { sheetState.show() }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("MapScreen", "Place not found.", exception)
                    }
            }
        )

        // ------------------ 검색창 ------------------
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(8.dp),
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            // 항상 최신 자동완성(연관검색) fetch + 상세 조회 → 바텀시트
            onSearch = {
                searchWithDetailInfo(
                    searchQuery = searchQuery,
                    googleCameraPositionState = googleCameraPositionState,
                    placesClient = placesClient,
                    scope = scope,
                    onResult = { place ->
                        selectedPlace = place
                        searchQuery = ""
                        searchResults = emptyList()
                        isSearchActive = false
                        scope.launch { sheetState.show() }
                    },
                    moveCamera = { latLng ->
                        scope.launch {
                            googleCameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                                durationMs = 1000
                            )
                        }
                    },
                    onNoResult = {
                        // 토스트, 스낵바 등 안내 코드 (예: "검색 결과 없음")
                    }
                )
            },
            active = isSearchActive,
            onActiveChange = {
                isSearchActive = it
                if (!it) {
                    searchQuery = ""
                    searchResults = emptyList()
                }
            },
            placeholder = { Text("장소 검색") },
            shape = RoundedCornerShape(12.dp),
            colors = SearchBarDefaults.colors(containerColor = Color.White),
            leadingIcon = {
                if (isSearchActive) {
                    IconButton(onClick = {
                        searchQuery = ""
                        searchResults = emptyList()
                        isSearchActive = false
                    }) { Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기") }
                } else {
                    IconButton(onClick = { }) { Icon(Icons.Default.Search, contentDescription = "검색") }
                }
            },
            trailingIcon = {
                if (isSearchActive) {
                    IconButton(onClick = {
                        // 돋보기 아이콘도 항상 상세 fetch로 검색
                        searchWithDetailInfo(
                            searchQuery = searchQuery,
                            googleCameraPositionState = googleCameraPositionState,
                            placesClient = placesClient,
                            scope = scope,
                            onResult = { place ->
                                selectedPlace = place
                                searchQuery = ""
                                searchResults = emptyList()
                                isSearchActive = false
                                scope.launch { sheetState.show() }
                            },
                            moveCamera = { latLng ->
                                scope.launch {
                                    googleCameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                                        durationMs = 1000
                                    )
                                }
                            },
                            onNoResult = {
                                // 없음 안내 처리
                            }
                        )
                    }) { Icon(Icons.Default.Search, contentDescription = "검색") }
                }
            }
        ) {
            // 자동완성(연관검색) 리스트 표시
            LazyColumn {
                items(searchResults) { prediction ->
                    ListItem(
                        headlineContent = { Text(prediction.getPrimaryText(null).toString()) },
                        supportingContent = { Text(prediction.getSecondaryText(null).toString()) },
                        modifier = Modifier.clickable {
                            // 반드시 placeId로 상세 fetch!
                            fetchPlaceAndShowBottomSheet(
                                prediction = prediction,
                                placesClient = placesClient,
                                onSuccess = { place ->
                                    selectedPlace = place
                                    searchQuery = ""
                                    searchResults = emptyList()
                                    isSearchActive = false

                                    scope.launch { sheetState.show() }
                                }
                            )
                        }
                    )
                }
            }
        }

        // 상세정보 바텀시트
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

/**
 * (1) 항상 검색어 기준 최신 자동완성 결과를 받아
 * (2) 첫 번째 Prediction의 PlaceId로 FetchPlaceRequest를 호출해서 Place 상세 정보를 얻음
 * (3) 성공시 지도 이동 & 바텀시트 오픈, 실패시 안내
 */
fun searchWithDetailInfo(
    searchQuery: String,
    googleCameraPositionState: CameraPositionState,
    placesClient: PlacesClient,
    scope: CoroutineScope,
    onResult: (Place) -> Unit,
    moveCamera: (com.google.android.gms.maps.model.LatLng) -> Unit,
    onNoResult: (() -> Unit)? = null
) {
    doSearch(
        query = searchQuery,
        googleCameraPositionState = googleCameraPositionState,
        placesClient = placesClient,
        onResult = { predictions ->
            val firstPrediction = predictions.firstOrNull()
            if (firstPrediction != null) {
                // 반드시 placeId로 상세 조회해야 모든 정보 올라옴!
                fetchPlaceAndShowBottomSheet(
                    prediction = firstPrediction,
                    placesClient = placesClient,
                    onSuccess = { place ->
                        place.latLng?.let { moveCamera(it) }
                        onResult(place)
                    }
                )
            } else {
                onNoResult?.invoke()
            }
        }
    )
}

/**
 * 자동완성 Prediction 객체(placeId)로 실제 Place 상세정보를 Fetch해서 결과 콜백
 */
fun fetchPlaceAndShowBottomSheet(
    prediction: AutocompletePrediction,
    placesClient: PlacesClient,
    onSuccess: (Place) -> Unit,
    onError: ((Exception) -> Unit)? = null
) {
    // 필요한 필드를 모두 명시해야 상세 데이터가 올라옴!
    val placeFields = listOf(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.ADDRESS,
        Place.Field.PHONE_NUMBER,
        Place.Field.RATING,
        Place.Field.WEBSITE_URI,
        Place.Field.USER_RATINGS_TOTAL,
        Place.Field.TYPES,
        Place.Field.PHOTO_METADATAS,
        Place.Field.LAT_LNG
    )
    val request = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
    placesClient.fetchPlace(request)
        .addOnSuccessListener { response ->
            val place = response.place
            onSuccess(place)
        }
        .addOnFailureListener { ex ->
            onError?.invoke(ex)
        }
}

/**
 * 기본 자동완성 API 호출(연관검색)
 */
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
    placesClient.findAutocompletePredictions(request)
        .addOnSuccessListener { response -> onResult(response.autocompletePredictions) }
        .addOnFailureListener { exception -> onError(exception) }
}
