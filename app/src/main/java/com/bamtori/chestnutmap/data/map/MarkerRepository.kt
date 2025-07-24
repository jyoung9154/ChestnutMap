package com.bamtori.chestnutmap.data.map

import com.bamtori.chestnutmap.data.model.Marker
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * 지도 마커 관련 데이터를 Firestore와 연동하여 관리하는 Repository 클래스입니다.
 * 마커 생성, 업데이트, 삭제 및 특정 지도에 속한 마커 목록을 가져오는 기능을 담당합니다.
 */
class MarkerRepository {
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * 특정 지도의 마커 목록을 Firestore에서 가져옵니다.
     * @param mapId 마커를 가져올 지도의 ID
     * @return 마커 목록을 포함하는 Flow
     */
    fun getMarkersForMap(mapId: String): Flow<List<Marker>> = flow {
        // TODO: Firestore에서 특정 지도의 마커 목록을 가져오는 로직을 구현해야 합니다.
        emit(emptyList())
    }

    /**
     * 새로운 마커를 Firestore에 생성합니다.
     * @param marker 생성할 마커 객체
     */
    suspend fun createMarker(marker: Marker) {
        // TODO: Firestore에 새로운 마커를 생성하는 로직을 구현해야 합니다.
    }

    /**
     * 기존 마커를 Firestore에서 업데이트합니다.
     * @param marker 업데이트할 마커 객체
     */
    suspend fun updateMarker(marker: Marker) {
        // TODO: Firestore에서 기존 마커를 업데이트하는 로직을 구현해야 합니다.
    }

    /**
     * 특정 마커를 Firestore에서 삭제합니다.
     * @param markerId 삭제할 마커의 ID
     */
    suspend fun deleteMarker(markerId: String) {
        // TODO: Firestore에서 마커를 삭제하는 로직을 구현해야 합니다.
    }
}