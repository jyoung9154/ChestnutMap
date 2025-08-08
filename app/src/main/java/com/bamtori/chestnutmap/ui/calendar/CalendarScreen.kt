package com.bamtori.chestnutmap.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
import java.time.temporal.WeekFields
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

    // 화면 높이 계산 (시스템 UI 요소들 제외)
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeight = configuration.screenHeightDp.dp

    // 상태바와 네비게이션바 높이 계산
    val statusBarHeight = with(density) {
        WindowInsets.statusBars.getTop(density).toDp()
    }
    val navigationBarHeight = with(density) {
        WindowInsets.navigationBars.getBottom(density).toDp()
    }

    // 헤더와 요일 헤더의 높이 (고정값)
    val headerHeight = 56.dp // 월/연도 헤더
    val dayOfWeekHeaderHeight = 40.dp // 요일 헤더
    val fixedElementsHeight = headerHeight + dayOfWeekHeaderHeight + 70.dp // 커스텀 네비바 높이 추가

    // 캘린더에서 사용할 수 있는 높이 (시스템 UI 요소들과 고정 요소들 제외)
    val availableCalendarHeight = screenHeight - statusBarHeight - navigationBarHeight - fixedElementsHeight

    // 현재 달의 주 개수 계산
    val weeksInMonth = remember(visibleMonth) {
        calculateWeeksInMonth(visibleMonth)
    }

    // 각 행(주)의 높이 계산
    val rowHeight = availableCalendarHeight / weeksInMonth

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 캘린더 헤더 (월/연도 및 이전/다음 달 버튼)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight),
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dayOfWeekHeaderHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

        // 월별 캘린더 - 남은 공간을 모두 차지
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(availableCalendarHeight) // 시스템 UI 제외한 실제 사용 가능한 높이
        ) {
            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    val hasEvent = remember(day.date, events) {
                        events.any { it.date == day.date }
                    }
                    DayCell(
                        day = day,
                        isSelected = selectedDate == day.date,
                        cellHeight = rowHeight, // 계산된 행 높이 전달
                        hasEvent = hasEvent
                    ) {
                        calendarViewModel.onDateSelected(day.date)
                    }
                }
            )
        }
    }
}

@Composable
fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    hasEvent: Boolean,
    cellHeight: Dp, // 높이 파라미터 추가
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(cellHeight) // 전달받은 높이 사용
            .background(
                color = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else
                    Color.Transparent
            )
            .clickable(enabled = day.position == DayPosition.MonthDate, onClick = onClick)
            .padding(4.dp), // 여백 추가
        contentAlignment = Alignment.TopCenter // 텍스트를 위쪽으로 정렬
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // 위쪽 정렬
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = when {
                    day.position != DayPosition.MonthDate -> Color.Gray
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    day.date.dayOfWeek == DayOfWeek.SATURDAY -> Color.Blue
                    day.date.dayOfWeek == DayOfWeek.SUNDAY -> Color.Red
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (hasEvent) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
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

/**
 * 주어진 달의 주 개수를 계산하는 함수
 */
private fun calculateWeeksInMonth(yearMonth: YearMonth): Int {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()

    // 한국 기준으로 일요일을 첫 번째 요일로 설정 (기존 캘린더와 일치)
    val weekFields = WeekFields.of(DayOfWeek.SUNDAY, 1)

    val firstWeek = firstDayOfMonth.get(weekFields.weekOfMonth())
    val lastWeek = lastDayOfMonth.get(weekFields.weekOfMonth())

    return lastWeek - firstWeek + 1
}