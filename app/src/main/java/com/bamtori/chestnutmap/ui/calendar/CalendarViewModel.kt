package com.bamtori.chestnutmap.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bamtori.chestnutmap.data.model.Event
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 캘린더 화면의 UI 상태 및 비즈니스 로직을 관리하는 ViewModel입니다.
 * Firestore에서 일정 데이터를 가져오고, 캘린더 UI 상태를 관리합니다.
 */
class CalendarViewModel : ViewModel() {

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadEvents()
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    private fun loadEvents() {
        viewModelScope.launch {
            try {
                val result = firestore.collection("events").get().await()
                val eventList = result.documents.mapNotNull {
                    val event = it.toObject(Event::class.java)
                    val timestamp = it.getTimestamp("date")
                    timestamp?.let {
                        event?.copy(date = toLocalDate(it))
                    }
                }
                _events.value = eventList
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun toLocalDate(timestamp: Timestamp): LocalDate {
        return Instant.ofEpochSecond(timestamp.seconds).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}