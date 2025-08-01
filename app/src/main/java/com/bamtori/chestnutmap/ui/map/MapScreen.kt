package com.bamtori.chestnutmap.ui.map

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bamtori.chestnutmap.data.map.MapRepository
import com.google.maps.android.compose.GoogleMap
import kotlinx.coroutines.launch
import com.google.android.gms.maps.model.CameraPosition as GoogleCameraPosition
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapProperties


/**
 * /app/src/main/java/com/bamtori/chestnutmap/ui/map/MapScreen.kt
 *
 * 네이버 지도앱과 거의 같은 UX + 내 위치 버튼 기능까지 완전 지원
 * - 지도 주요 제스처(이동/확대/축소/회전 등), 내 위치 버튼, ± 버튼, 마커 추가/수정
 * - 초보자를 위한 상세 한글 주석 포함
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
    val currentMapId by mapViewModel.currentMapId.collectAsState()

    // 처음 진입 시 더미 맵 ID 할당 (테스트/개발시용)
    LaunchedEffect(Unit) {
        if (currentMapId == null) {
            mapViewModel.setCurrentMapId("dummy_map_id_for_testing")
        }
    }

    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var selectedLatLng by remember { mutableStateOf<GoogleLatLng?>(null) }
    var selectedPoi by remember { mutableStateOf<PointOfInterest?>(null) }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    val placesClient = remember { Places.createClient(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        //구글 지도
        val googleCameraPositionState = com.google.maps.android.compose.rememberCameraPositionState() {
            position = GoogleCameraPosition(GoogleLatLng(37.5665, 126.9780), 10f, 0f, 0f)
        }
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
            properties = MapProperties(
                isMyLocationEnabled = true
            ),
            onMapClick = { latLng ->
                Log.d("MapClick", "Clicked location: ${latLng.latitude}, ${latLng.longitude}")
            },
            onMapLongClick = { latLng ->
                Log.d("MapClick", "Clicked location: ${latLng.latitude}, ${latLng.longitude}")
                selectedLatLng = GoogleLatLng(latLng.latitude, latLng.longitude)
                scope.launch { sheetState.show() }
            },
            onPOIClick = { poi ->
                // Place.Field: 원하는 상세 필드 추가
                val placeFields = listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.PHONE_NUMBER,
                    Place.Field.RATING,
                    Place.Field.WEBSITE_URI,
                    Place.Field.TYPES,
                    Place.Field.PHOTO_METADATAS,
                    Place.Field.USER_RATINGS_TOTAL
                )
                val request = FetchPlaceRequest.newInstance(poi.placeId, placeFields)
                placesClient.fetchPlace(request)
                    .addOnSuccessListener { response ->
                        selectedPlace = response.place
                        // 필요시 바텀시트 자동 show
                        scope.launch { sheetState.show() }
                    }
                    .addOnFailureListener { exception ->  Log.e("MapScreen", "Place not found.", exception) }
            },
        ) {
            // 필요시 마커 등 추가
        }


        // ② 바텀시트 표출
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

        if (selectedPlace != null) {
//            PlaceDetailsBottomSheet(place = selectedPlace)
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    selectedPlace = null
                    scope.launch { sheetState.hide() }
                }
            ) {
//                Log.d("MapScreen", "selectedPlace: $selectedPlace")
                PlaceDetailsBottomSheet(place = selectedPlace, placesClient = placesClient)
            }
        }
    }
}

