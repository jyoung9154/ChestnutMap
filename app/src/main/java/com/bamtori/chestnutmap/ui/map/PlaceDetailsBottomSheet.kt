
package com.bamtori.chestnutmap.ui.map

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bamtori.chestnutmap.util.placeTypeKoMap
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient

@Composable
fun PlaceDetailsBottomSheet(place: Place?, placesClient: PlacesClient) {
    if (place == null) {
        // 데이터가 없는 경우 아무것도 표시하지 않거나 로딩 인디케이터를 표시할 수 있습니다.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("장소 정보를 불러올 수 없습니다.")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 상단 드래그 핸들
//        Box(
//            modifier = Modifier
//                .width(40.dp)
//                .height(4.dp)
//                .clip(RoundedCornerShape(2.dp))
//                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
//                .align(Alignment.CenterHorizontally)
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))

        // --- 장소 이름 및 유형 ---
        Text(
            text = place.name ?: "이름 없음",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            // 평점
            Text(
                text = place.rating?.toString() ?: "N/A",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                tint = Color(0xFFFFC700),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(16.dp)
            )
            // 리뷰 개수
            // TODO: Place.Field.USER_RATINGS_TOTAL을 요청하여 실제 리뷰 개수를 표시해야 합니다.
            val reviewsCount = place.userRatingsTotal
            if (reviewsCount != null) {
                Text(
                    text = "($reviewsCount)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            // 장소 유형
//            val placeTypeString = place.types?.firstOrNull()?.toString()
//            // placeTypeKoMap에서 한글 이름을 찾고, 없으면 원래 영문 이름을 그대로 사용합니다.
//            val koreanPlaceType = placeTypeKoMap[placeTypeString] ?: placeTypeString?.replace("_", " ")?.lowercase()?.capitalize()

            val koreanPlaceType = place.types
                ?.filter {
                    it != Place.Type.ESTABLISHMENT && it != Place.Type.POINT_OF_INTEREST
                }
                ?.firstOrNull()
                ?.let { placeTypeKoMap[it.name] }

            if (koreanPlaceType != null) {
                Text(
                    text = " · $koreanPlaceType",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 액션 버튼 ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionButton(icon = Icons.Default.BookmarkBorder, label = "저장")
            ActionButton(icon = Icons.Default.Share, label = "공유")
            ActionButton(icon = Icons.Default.Call, label = "전화")
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        // --- 상세 정보 ---ㅁ
        InfoRow(icon = Icons.Default.LocationOn, text = place.address ?: "주소 정보 없음")
        InfoRow(icon = Icons.Default.Language, text = place.websiteUri?.toString() ?: "웹사이트 없음")
        InfoRow(icon = Icons.Default.Phone, text = place.phoneNumber ?: "전화번호 없음")

        Spacer(modifier = Modifier.height(12.dp))

        // --- 장소 사진 --- (별도 Composable로 분리하여 교체)
        PlacePhoto(place = place, placesClient = placesClient)
    }
}

/**
 * 장소 사진을 비동기적으로 로드하고 표시하는 Composable
 */
@Composable
private fun PlacePhoto(place: Place?, placesClient: PlacesClient?) {
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // place 객체가 변경될 때마다 사진을 다시 로드합니다.
    LaunchedEffect(place) {
        isLoading = true
        imageBitmap = null // 이전 이미지 초기화
        if (place == null || placesClient == null) {
            isLoading = false
            return@LaunchedEffect
        }

        val photoMetadata = place.photoMetadatas?.firstOrNull()
        if (photoMetadata == null) {
            isLoading = false
            return@LaunchedEffect
        }

        val photoRequest = FetchPhotoRequest.builder(photoMetadata).build()
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener { fetchPhotoResponse ->
            imageBitmap = fetchPhotoResponse.bitmap
            isLoading = false
        }.addOnFailureListener {
            isLoading = false
        }
    }
    // 이미지 없고 로딩도 아니면(즉 사진 자체가 없으면) 아예 그리지 않음!
    if (imageBitmap == null && !isLoading) return

    Box(
        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator()
            imageBitmap != null -> {
                Image(
                    bitmap = imageBitmap!!.asImageBitmap(),
                    contentDescription = "${place?.name} 사진",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
//                Icon(
//                    imageVector = Icons.Default.PhotoCamera,
//                    contentDescription = "사진 없음",
//                    tint = Color.Gray,
//                    modifier = Modifier.size(48.dp)
//                )
            }
        }
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        OutlinedIconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(50) // Circle
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Preview를 위한 가짜 데이터
@Preview(showBackground = true)
@Composable
fun PlaceDetailsBottomSheetPreview() {
    // 실제 Place 객체를 만들 수 없으므로, UI 모양만 보기 위한 가짜 Composable을 만듭니다.
    val fakePlaceData = object {
        val name = "경복궁"
        val rating = 4.6
        val reviewsCount = 27819
        val type = "관광 명소"
        val address = "서울 종로구 사직로 161"
        val website = "http://www.royalpalace.go.kr/"
        val phone = "02-3700-3900"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
//        Box(
//            modifier = Modifier
//                .width(40.dp)
//                .height(4.dp)
//                .clip(RoundedCornerShape(2.dp))
//                .background(Color.Gray)
//                .align(Alignment.CenterHorizontally)
//        )
//        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = fakePlaceData.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = fakePlaceData.rating.toString(), style = MaterialTheme.typography.bodyMedium)
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                tint = Color(0xFFFFC700),
                modifier = Modifier.padding(start = 4.dp).size(16.dp)
            )
            Text(
                text = "(${fakePlaceData.reviewsCount})",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 4.dp)
            )
            Text(
                text = " · ${fakePlaceData.type}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
//            ActionButton(icon = Icons.Default.Directions, label = "경로")
            ActionButton(icon = Icons.Default.BookmarkBorder, label = "저장")
            ActionButton(icon = Icons.Default.Share, label = "공유")
            ActionButton(icon = Icons.Default.Call, label = "전화")
        }
        Divider(modifier = Modifier.padding(vertical = 12.dp))
        InfoRow(icon = Icons.Default.LocationOn, text = fakePlaceData.address)
        InfoRow(icon = Icons.Default.Language, text = fakePlaceData.website)
        InfoRow(icon = Icons.Default.Phone, text = fakePlaceData.phone)

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Photo Placeholder",
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
        }

    }
}
