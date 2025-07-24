package com.bamtori.chestnutmap.data.model

import com.google.firebase.Timestamp

/**
 * 지도 데이터를 나타내는 데이터 클래스입니다.
 * 지도의 ID, 이름, 소유자 ID 등을 포함합니다.
 *
 * @property mapId 지도의 고유 ID
 * @property name 지도 이름
 * @property ownerUid 지도 소유자의 UID
 * @property createdAt 지도 생성 타임스탬프
 * @property members 지도에 참여한 멤버들의 UID 목록
 */
data class Map(
    val mapId: String = "",
    val name: String = "",
    val ownerUid: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val members: List<String> = emptyList()
)