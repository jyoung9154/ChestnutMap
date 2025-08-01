package com.bamtori.chestnutmap.data.network

import android.util.Log
import com.bamtori.chestnutmap.BuildConfig
import com.bamtori.chestnutmap.data.model.GooglePlaceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object ApiHelper {

    /**
     * [Text Search] - 사용자가 입력한 검색어를 바탕으로 주변/특정 위치의 장소 리스트를 검색한다.
     * @param query 예: "카페", "강남 치킨", "피자 레스토랑"
     * @param lat 중심 좌표(선택)
     * @param lng 중심 좌표(선택)
     * @param radius 반경(선택, 미터)
     * @return GooglePlaceInfo 리스트 반환
     */
    suspend fun searchPlaceByText(
        query: String,
        lat: Double? = null,
        lng: Double? = null,
        radius: Int? = null
    ): List<GooglePlaceInfo> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GOOGLE_MAP_KEY
        val location = if (lat != null && lng != null) "$lat,$lng" else null
        val rawJson = NetworkModule.googleApiService.textSearch(query, location, radius, apiKey)
        val root = JSONObject(rawJson)
        val arr = root.optJSONArray("results") ?: return@withContext emptyList()
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                add(parseGooglePlace(obj))
            }
        }
    }

    /**
     * [Nearby Search] - 현재 위치 기준 반경 내 장소(POI) 검색 (예: 내 주변 맛집)
     * @param lat 중심 위도
     * @param lng 중심 경도
     * @param radius 반경(m, 예: 1000 -> 1km)
     * @param keyword 장소 키워드(예: 치킨, cafe)
     * @param type Google Place Type (예: restaurant etc.)
     * @return GooglePlaceInfo 리스트 반환
     */
    suspend fun searchNearbyPlaces(
        lat: Double,
        lng: Double,
        radius: Int = 1000,
        keyword: String? = null,
        type: String? = null
    ): List<GooglePlaceInfo> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GOOGLE_MAP_KEY
        val location = "$lat,$lng"
        val rawJson = NetworkModule.googleApiService.nearbySearch(location, radius, keyword, type, apiKey)
        val root = JSONObject(rawJson)
        val arr = root.optJSONArray("results") ?: return@withContext emptyList()
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                add(parseGooglePlace(obj))
            }
        }
    }

    // 구글 장소 JSON → DTO 파싱 함수
    private fun parseGooglePlace(obj: JSONObject): GooglePlaceInfo {
        val placeId = obj.optString("place_id")
        val name = obj.optString("name")
        val address = obj.optString("vicinity", obj.optString("formatted_address", ""))
        val rating = obj.optDouble("rating", -1.0).takeIf { it >= 0 }
        val types = obj.optJSONArray("types")
        val category = types?.optString(0)
        val photos = obj.optJSONArray("photos")
        val photoUrl = if (photos != null && photos.length() > 0) {
            val photoRef = photos.getJSONObject(0).optString("photo_reference")
            if (photoRef.isNotEmpty()) {
                "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=$photoRef&key=${BuildConfig.GOOGLE_MAP_KEY}"
            } else null
        } else null
        return GooglePlaceInfo(
            name = name,
            address = address,
            rating = rating,
            category = category,
            photoUrl = photoUrl,
            placeId = placeId
        )
    }
}
