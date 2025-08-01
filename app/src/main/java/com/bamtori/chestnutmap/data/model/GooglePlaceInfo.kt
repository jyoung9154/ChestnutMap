package com.bamtori.chestnutmap.data.model

/**
 * 구글 플레이스 API를 통해 얻는 장소 정보 데이터 클래스
 *
 * @property name 매장명
 * @property address 주소 문자열
 * @property rating 1~5 사이 별점 (null일 수 있음)
 * @property category 첫번째 카테고리 타입(ex: restaurant, cafe 등)
 * @property photoUrl 대표 이미지 URL (없으면 null)
 * @property placeId 구글 내부 장소 아이디
 */
data class GooglePlaceInfo(
    val name: String?,
    val address: String?,
    val rating: Double?,
    val category: String?,
    val photoUrl: String?,
    val placeId: String?
)
