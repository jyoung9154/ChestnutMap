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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * 캘린더 화면의 UI를 정의하는 Composable 함수입니다.
 * "타임트리"와 유사한 월별 캘린더 UI를 표시합니다.
 * 성능 최적화가 적용된 버전입니다.
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

    // 현재 보이는 달 상태 관리 최적화
    var visibleMonth by remember { mutableStateOf(currentMonth) }

    // 달 변경 감지 최적화
    LaunchedEffect(state.firstVisibleMonth.yearMonth) {
        visibleMonth = state.firstVisibleMonth.yearMonth
    }

    val selectedDate by calendarViewModel.selectedDate.collectAsState()
    val events by calendarViewModel.events.collectAsState()

    // 이벤트 맵 미리 생성 (검색 성능 최적화)
    val eventMap = remember(events) {
        events.groupBy { it.date }
    }

    // 화면 높이 계산
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeight = configuration.screenHeightDp.dp

    // WindowInsets는 @Composable이므로 remember 밖에서 호출
    val statusBarHeight = with(density) {
        WindowInsets.statusBars.getTop(density).toDp()
    }
    val navigationBarHeight = with(density) {
        WindowInsets.navigationBars.getBottom(density).toDp()
    }

    // 고정 높이들
    val headerHeight = 56.dp
    val dayOfWeekHeaderHeight = 40.dp
    val fixedElementsHeight = headerHeight + dayOfWeekHeaderHeight + 70.dp

    // 사용 가능한 캘린더 높이 계산
    val availableCalendarHeight = remember(screenHeight, statusBarHeight, navigationBarHeight, fixedElementsHeight) {
        screenHeight - statusBarHeight - navigationBarHeight - fixedElementsHeight
    }

    // 주 개수 계산 최적화 - 현재 달과 앞뒤 몇 달을 미리 계산
    val monthWeeksMap = remember(visibleMonth) {
        val monthsToPreCalculate = mutableMapOf<YearMonth, Int>()

        // 현재 달 기준 앞뒤 3달씩 미리 계산
        for (i in -1..1) {
            val targetMonth = visibleMonth.plusMonths(i.toLong())
            monthsToPreCalculate[targetMonth] = calculateWeeksInMonthOptimized(targetMonth)
        }

        monthsToPreCalculate
    }

    // 현재 달의 주 개수 (미리 계산된 값 사용)
    val weeksInMonth = monthWeeksMap[visibleMonth] ?: calculateWeeksInMonthOptimized(visibleMonth)

    // 행 높이 계산 캐싱
    val rowHeight = remember(availableCalendarHeight, weeksInMonth) {
        availableCalendarHeight / weeksInMonth
    }

    // 다른 달들의 행 높이도 미리 계산
    val monthRowHeightMap = remember(availableCalendarHeight, monthWeeksMap) {
        monthWeeksMap.mapValues { (_, weeks) ->
            availableCalendarHeight / weeks
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 캘린더 헤더 (월/연도 및 이전/다음 달 버튼)
        CalendarHeader(
            visibleMonth = visibleMonth,
            headerHeight = headerHeight,
            onPreviousClick = {
                coroutineScope.launch {
                    state.scrollToMonth(visibleMonth.minusMonths(1)) // animateScrollToMonth 대신 scrollToMonth 사용
                }
            },
            onNextClick = {
                coroutineScope.launch {
                    state.scrollToMonth(visibleMonth.plusMonths(1)) // animateScrollToMonth 대신 scrollToMonth 사용
                }
            }
        )

        // 요일 헤더
        DayOfWeekHeader(
            daysOfWeek = daysOfWeek,
            height = dayOfWeekHeaderHeight
        )

        // 월별 캘린더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(availableCalendarHeight)
        ) {
            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    // 해당 날짜의 달에 맞는 행 높이 사용
                    val monthForDay = YearMonth.from(day.date)
                    val cellHeightForDay = monthRowHeightMap[monthForDay]
                        ?: (availableCalendarHeight / calculateWeeksInMonthOptimized(monthForDay))

                    OptimizedDayCell(
                        day = day,
                        isSelected = selectedDate == day.date,
                        cellHeight = cellHeightForDay,
                        eventMap = eventMap,
                        onClick = { calendarViewModel.onDateSelected(day.date) }
                    )
                }
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    visibleMonth: YearMonth,
    headerHeight: Dp,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowLeft, contentDescription = "이전 달")
        }
        Text(
            text = "${visibleMonth.year}년 ${visibleMonth.month.getDisplayName(TextStyle.FULL, Locale.KOREAN)}",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onNextClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = "다음 달")
        }
    }
}

@Composable
private fun DayOfWeekHeader(
    daysOfWeek: List<DayOfWeek>,
    height: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
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
}

@Composable
private fun OptimizedDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    cellHeight: Dp,
    eventMap: Map<LocalDate, List<Event>>, // 미리 계산된 이벤트 맵
    onClick: () -> Unit
) {
    // 이벤트 존재 여부를 맵에서 빠르게 확인
    val hasEvent = remember(day.date, eventMap) {
        eventMap.containsKey(day.date)
    }

    // MaterialTheme 색상들을 먼저 가져오기
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val errorColor = MaterialTheme.colorScheme.error

    // 색상 계산
    val textColor = when {
        day.position != DayPosition.MonthDate -> Color.Gray
        isSelected -> onPrimaryContainer
        day.date.dayOfWeek == DayOfWeek.SATURDAY -> Color.Blue
        day.date.dayOfWeek == DayOfWeek.SUNDAY -> Color.Red
        else -> onSurface
    }

    val backgroundColor = if (isSelected) {
        primaryContainer.copy(alpha = 0.3f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(cellHeight)
            .background(color = backgroundColor)
            .clickable(enabled = day.position == DayPosition.MonthDate, onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (hasEvent) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(errorColor)
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
private fun calculateWeeksInMonthOptimized(yearMonth: YearMonth): Int {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()

    // WeekFields 객체를 재사용하기 위해 companion object로 이동 가능
    val weekFields = WeekFields.of(DayOfWeek.SUNDAY, 1)

    val firstWeek = firstDayOfMonth.get(weekFields.weekOfMonth())
    val lastWeek = lastDayOfMonth.get(weekFields.weekOfMonth())

    return lastWeek - firstWeek + 1
}