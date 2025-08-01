package com.bamtori.chestnutmap.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    // Text Search (장소 문자열로 검색)
    @GET("maps/api/place/textsearch/json")
    suspend fun textSearch(
        @Query("query") query: String,            // 예) "카페", "송파구 치킨집"
        @Query("location") location: String? = null,  // 지정시 중심위치 편향
        @Query("radius") radius: Int? = null,     // 반경(m)
        @Query("key") key: String
    ): String // Raw JSON으로 받은 뒤 Helper에서 파싱

    // Nearby Search (근방 장소 검색)
    @GET("maps/api/place/nearbysearch/json")
    suspend fun nearbySearch(
        @Query("location") location: String,      // "위도,경도"
        @Query("radius") radius: Int? = null,     // 미터, TextSearch보다 근거리에 집중
        @Query("keyword") keyword: String? = null,// 특정 키워드
        @Query("type") type: String? = null,      // 장소 타입(restaurant, cafe 등)
        @Query("key") key: String
    ): String

    // (예시) 파이어베이스 커스텀 API 등도 추가 가능
}
