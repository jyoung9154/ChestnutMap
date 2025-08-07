package com.bamtori.chestnutmap.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bamtori.chestnutmap.data.model.Event
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * 캘린더 화면의 UI를 정의하는 Composable 함수입니다.
 * "타임트리"와 유사한 월별 캘린더 UI를 표시합니다.
 */
@Composable
fun CalendarScreen(
    calendarViewModel: CalendarViewModel = viewModel()
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val daysOfWeek = remember { daysOfWeek() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )
    val coroutineScope = rememberCoroutineScope()
    val visibleMonth = state.firstVisibleMonth.yearMonth

    val selectedDate by calendarViewModel.selectedDate.collectAsState()
    val events by calendarViewModel.events.collectAsState()
    val eventsForSelectedDate = remember(selectedDate, events) {
        selectedDate?.let { date ->
            events.filter { it.date == date }
        } ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()

    ) {
        // 캘린더 헤더 (월/연도 및 이전/다음 달 버튼)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = {
                coroutineScope.launch {
                    state.animateScrollToMonth(visibleMonth.minusMonths(1))
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowLeft, contentDescription = "이전 달")
            }
            Text(
                text = "${visibleMonth.year}년 ${visibleMonth.month.getDisplayName(TextStyle.FULL, Locale.KOREAN)}",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                coroutineScope.launch {
                    state.animateScrollToMonth(visibleMonth.plusMonths(1))
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = "다음 달")
            }
        }

        // 요일 헤더
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayOfWeek in daysOfWeek) {
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                    color = when (dayOfWeek) {
                        DayOfWeek.SATURDAY -> Color.Blue
                        DayOfWeek.SUNDAY -> Color.Red
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }

        // 월별 캘린더
        Box(modifier = Modifier
//            .weight(20f) // 남는 공간 모두 사용
//            .background(Color.Red)
//            .fillMaxWidth() // 가로로도 꽉!
            ) {
            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    DayCell(
                        day = day,
                        isSelected = selectedDate == day.date,
                        hasEvent = events.any { it.date == day.date }
                    ) {
                        calendarViewModel.onDateSelected(day.date)
                    }
                }
            )
        }


//        Spacer(modifier = Modifier.height(16.dp))

        // 선택된 날짜의 이벤트 목록
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(eventsForSelectedDate) {
                EventItem(event = it)
            }
        }
    }
}

@Composable
fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    hasEvent: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // 정사각형 셀
            .padding(4.dp)
            .background(color = if (isSelected) Color.Gray.copy(alpha = 0.3f) else Color.Transparent)
            .clickable(enabled = day.position == DayPosition.MonthDate, onClick = onClick),

        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = if (day.position == DayPosition.MonthDate) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
            if (hasEvent) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                )
            }
        }
    }
}

@Composable
fun EventItem(event: Event) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(event.color)))
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = event.title)
    }
}