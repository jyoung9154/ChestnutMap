package com.bamtori.chestnutmap.data.model

import com.google.firebase.Timestamp

/**
 * 지도 마커 데이터를 나타내는 데이터 클래스입니다.
 * 마커의 ID, 소속 지도 ID, 위치(위도, 경도), 제목, 설명 등을 포함합니다.
 *
 * @property markerId 마커의 고유 ID
 * @property mapId 마커가 속한 지도의 ID
 * @property lat 위도
 * @property lng 경도
 * @property title 마커 제목
 * @property description 마커 설명
 * @property color 마커 색상 (Hex 코드 등)
 * @property photos 마커에 첨부된 사진 URL 목록
 * @property favorites 이 마커를 즐겨찾기한 사용자 UID 목록
 * @property updatedAt 마지막 업데이트 타임스탬프
 */
data class Marker(
    val markerId: String = "",
    val mapId: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val title: String = "",
    val description: String = "",
    val color: String = "#FF0000", // 기본값: 빨간색
    val photos: List<String> = emptyList(),
    val favorites: List<String> = emptyList(),
    val updatedAt: Timestamp = Timestamp.now()
)