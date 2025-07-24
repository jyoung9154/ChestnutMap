package com.bamtori.chestnutmap.data.model

import com.google.firebase.Timestamp

/**
 * 메모 항목 데이터를 나타내는 데이터 클래스입니다.
 * 메모의 ID, 내용, 생성 시간 등을 포함합니다.
 *
 * @property itemId 아이템의 고유 ID
 * @property mapId 아이템이 속한 지도의 ID
 * @property type 아이템 타입 (MEMO, PLAN, CHECKLIST)
 * @property content 내용
 * @property status 완료 여부 (체크리스트용)
 * @property dueDate 마감일 또는 일정 날짜
 */
data class MemoItem(
    val itemId: String = "",
    val mapId: String = "",
    val type: ItemType = ItemType.MEMO,
    val content: String = "",
    val status: Boolean = false,
    val dueDate: Timestamp? = null
)

/**
 * 메모, 일정, 체크리스트 등 다양한 아이템의 타입을 정의하는 열거형 클래스
 */
enum class ItemType {
    MEMO, PLAN, CHECKLIST
}