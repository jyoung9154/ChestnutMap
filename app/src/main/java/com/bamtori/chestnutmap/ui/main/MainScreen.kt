package com.bamtori.chestnutmap.ui.main

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesomeMotion
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.bamtori.chestnutmap.ui.map.MapScreen
import com.bamtori.chestnutmap.ui.calendar.CalendarScreen
import com.bamtori.chestnutmap.ui.memo.MemoScreen
import com.bamtori.chestnutmap.ui.settings.SettingsScreen

// 1. 각 탭 정의(Material 아이콘/직접 아이콘 사용 가능, painterResource도 교체 가능)
sealed class Screen(
    val route: String,
    val name: String,
    val icon: ImageVector // 커스텀 벡터나 painterResource 아이콘으로 바꿔도 됨
) {
    object Home : Screen("home", "지도", Icons.Outlined.Map)
    object Calendar : Screen("calendar", "일정", Icons.Outlined.CalendarMonth)
//    object Add : Screen("add", "추가", Icons.Outlined.Add)
    object Notification : Screen("notification", "알림", Icons.Outlined.NotificationsNone) // 알람 아이콘 사용, 필요시 변경
    object Memo : Screen("memo", "메모", Icons.Outlined.AutoAwesomeMotion)
    object Settings : Screen("settings", "설정", Icons.Outlined.Tune)
}

// 2. 하단 네비게이션바 항목 순서. 중앙 Add(FAB)는 한 칸 차지!
val navBarItems = listOf(
    Screen.Home,
    Screen.Calendar,
    Screen.Memo,
    Screen.Notification,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    SetStatusBarIcons(true) // 상태바 컬러 지정

    Scaffold(
        bottomBar = {
            // 3. Row + .weight(1f)로 폭 균등 배치 & 배경색 적용
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF18181C)) // 어두운 배경
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navBarItems.forEach { screen ->
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        // 일반 탭: weight로 칸 균등, 아이콘 tint로 컬러 반영
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.name,
                                    tint = if (selected) Color(153,50,204) else Color(0xFFB0B0C3)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.name,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Color(153,50,204) else Color(0xFFB0B0C3)
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            // colors 인자로 따로 포인트컬러 지정도 가능, 기본 폼은 label에서 해도 충분
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(153,50,204),
                                unselectedIconColor = Color(0xFFB0B0C3),
                                selectedTextColor = Color(153,50,204),
                                unselectedTextColor = Color(0xFFB0B0C3),
                                indicatorColor = Color.Transparent // ← 불필요한 selection bar(하단 흰색 등) 전부 제거!
                            ),
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // innerPadding: bottomBar 높이 + navigationBarsPadding() 만큼 자동 보정됨
        // 4. 실제로 각 탭 화면 표시(지도, 캘린더, 추가, 메모, 설정)
        NavHost(
            navController,
            startDestination = Screen.Home.route,
        ) {
            composable(Screen.Home.route) {Box(modifier = Modifier.padding(bottom = 70.dp).fillMaxSize()) { MapScreen() }}
            composable(Screen.Calendar.route) {Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) { CalendarScreen() }}
            composable(Screen.Memo.route) {  Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) { MemoScreen() }}
            composable(Screen.Notification.route) {  Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {  }}
            composable(Screen.Settings.route) {  Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) { SettingsScreen() }}
        }
    }


}

@Composable
fun SetStatusBarIcons(darkIcons: Boolean = true) {
    val view = LocalView.current
    val activity = view.context as Activity

    SideEffect {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false) // Edge-to-edge 기본세팅
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = darkIcons
    }
}
