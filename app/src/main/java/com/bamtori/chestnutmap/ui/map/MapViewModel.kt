package com.bamtori.chestnutmap.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bamtori.chestnutmap.data.map.MapRepository
import com.bamtori.chestnutmap.data.map.MarkerRepository
import com.bamtori.chestnutmap.data.model.Map
import com.bamtori.chestnutmap.data.model.Marker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 지도 화면의 UI 상태 및 비즈니스 로직을 관리하는 ViewModel입니다.
 * [MapRepository]와 [MarkerRepository]를 통해 지도 및 마커 데이터를 관리합니다.
 * 현재 선택된 지도 ID와 마커 추가 위치를 상태로 노출합니다.
 */
class MapViewModel(private val mapRepository: MapRepository, private val markerRepository: MarkerRepository) : ViewModel() {

    // 앱에 존재하는 지도 목록을 나타내는 StateFlow
    private val _maps = MutableStateFlow<List<Map>>(emptyList())
    val maps: StateFlow<List<Map>> = _maps

    // 현재 선택된 지도에 표시될 마커 목록을 나타내는 StateFlow
    private val _markers = MutableStateFlow<List<Marker>>(emptyList())
    val markers: StateFlow<List<Marker>> = _markers

    // 현재 사용자가 보고 있는 지도의 ID를 나타내는 StateFlow
    private val _currentMapId = MutableStateFlow<String?>(null)
    val currentMapId: StateFlow<String?> = _currentMapId

    // 새로운 마커를 추가할 때의 위도/경도 위치를 나타내는 StateFlow
    private val _markerAddLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val markerAddLocation: StateFlow<Pair<Double, Double>?> = _markerAddLocation

    init {
        // TODO: 현재 사용자를 위한 지도 목록을 가져오는 로직을 구현해야 합니다.
        // TODO: 현재 선택된 지도를 위한 마커 목록을 가져오는 로직을 구현해야 합니다.
    }

    /**
     * 현재 보고 있는 지도의 ID를 설정하고, 해당 지도의 마커를 가져옵니다.
     * @param mapId 설정할 지도의 ID
     */
    fun setCurrentMapId(mapId: String) {
        _currentMapId.value = mapId
        fetchMarkersForMap(mapId)
    }

    /**
     * 새로운 마커를 추가할 위치(위도, 경도)를 설정합니다.
     * @param lat 위도
     * @param lng 경도
     */
    fun setMarkerAddLocation(lat: Double, lng: Double) {
        _markerAddLocation.value = Pair(lat, lng)
    }

    /**
     * 마커 추가 위치를 초기화합니다.
     */
    fun clearMarkerAddLocation() {
        _markerAddLocation.value = null
    }

    /**
     * 새로운 지도를 생성합니다.
     * @param map 생성할 지도 객체
     */
    fun createMap(map: Map) {
        viewModelScope.launch {
            mapRepository.createMap(map)
        }
    }

    /**
     * 기존 지도를 업데이트합니다.
     * @param map 업데이트할 지도 객체
     */
    fun updateMap(map: Map) {
        viewModelScope.launch {
            mapRepository.updateMap(map)
        }
    }

    /**
     * 특정 지도를 삭제합니다.
     * @param mapId 삭제할 지도의 ID
     */
    fun deleteMap(mapId: String) {
        viewModelScope.launch {
            mapRepository.deleteMap(mapId)
        }
    }

    /**
     * 특정 지도의 마커 목록을 가져옵니다.
     * @param mapId 마커를 가져올 지도의 ID
     */
    fun fetchMarkersForMap(mapId: String) {
        viewModelScope.launch {
            markerRepository.getMarkersForMap(mapId).collectLatest {
                _markers.value = it
            }
        }
    }

    /**
     * 마커를 저장합니다. 새로운 마커인 경우 생성하고, 기존 마커인 경우 업데이트합니다.
     * @param marker 저장할 마커 객체
     */
    fun saveMarker(marker: Marker) {
        viewModelScope.launch {
            if (marker.markerId.isEmpty()) { // 새로운 마커인 경우
                val newMarker = marker.copy(
                    markerId = java.util.UUID.randomUUID().toString(),
                    mapId = _currentMapId.value ?: "", // 현재 맵 ID 사용
                    lat = _markerAddLocation.value?.first ?: 0.0,
                    lng = _markerAddLocation.value?.second ?: 0.0
                )
                markerRepository.createMarker(newMarker)
            } else { // 기존 마커를 수정하는 경우
                markerRepository.updateMarker(marker)
            }
            clearMarkerAddLocation() // 마커 추가 위치 초기화
        }
    }

    /**
     * 특정 마커를 삭제합니다.
     * @param markerId 삭제할 마커의 ID
     */
    fun deleteMarker(markerId: String) {
        viewModelScope.launch {
            markerRepository.deleteMarker(markerId)
        }
    }
}
