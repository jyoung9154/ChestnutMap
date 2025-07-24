package com.bamtori.chestnutmap.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * 사용자 설정(예: 다크 모드 활성화 여부)을 DataStore를 사용하여 저장하고 관리하는 Repository입니다.
 * DataStore는 비동기적이고 트랜잭션적인 방식으로 데이터를 저장하여 데이터 일관성을 보장합니다.
 */
class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        // 여기에 다른 설정 키들을 추가할 수 있습니다.
    }

    /**
     * 다크 모드 활성화 여부를 나타내는 Flow를 반환합니다.
     * 설정이 없으면 기본값으로 false를 반환합니다.
     */
    val darkModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DARK_MODE_ENABLED] ?: false
        }

    /**
     * 다크 모드 활성화 여부를 설정합니다.
     * @param enabled 다크 모드 활성화 여부 (true: 활성화, false: 비활성화)
     */
    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE_ENABLED] = enabled
        }
    }

    // 여기에 다른 설정 관련 메서드들을 추가할 수 있습니다.
}
