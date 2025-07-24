package com.bamtori.chestnutmap.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bamtori.chestnutmap.ui.map.MapScreen
import com.bamtori.chestnutmap.ui.calendar.CalendarScreen
import com.bamtori.chestnutmap.ui.memo.MemoScreen
import com.bamtori.chestnutmap.ui.settings.SettingsScreen

/**
 * 앱의 메인 화면 UI를 정의하는 Composable 함수입니다.
 * 하단 내비게이션 바를 포함하며, 각 탭에 따라 다른 화면(지도, 캘린더, 메모, 설정)을 표시합니다.
 */
sealed class Screen(val route: String, val name: String, val icon: @Composable () -> Unit) {
    object Home : Screen("home", "홈", { Icon(Icons.Default.Home, contentDescription = null) })
    object Calendar : Screen("calendar", "캘린더", { Icon(Icons.Filled.CalendarToday, contentDescription = null) })
    object Memo : Screen("memo", "메모", { Icon(Icons.Filled.Note, contentDescription = null) })
    object Settings : Screen("settings", "설정", { Icon(Icons.Default.Settings, contentDescription = null) })
}

val items = listOf(
    Screen.Home,
    Screen.Calendar,
    Screen.Memo,
    Screen.Settings,
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            // 하단 내비게이션 바 정의
            BottomAppBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.icon() },
                        label = { Text(screen.name) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // 내비게이션 스택 관리 로직
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // 내비게이션 호스트 정의: 각 경로에 해당하는 화면을 표시합니다.
        NavHost(navController, startDestination = Screen.Home.route, Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { MapScreen() }
            composable(Screen.Calendar.route) { CalendarScreen() }
            composable(Screen.Memo.route) { MemoScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}