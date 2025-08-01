package com.bamtori.chestnutmap.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bamtori.chestnutmap.data.map.MapRepository

/**
 * [MapViewModel]을 생성하기 위한 팩토리 클래스입니다.
 * [MapRepository]와 [MarkerRepository] 의존성을 주입하여 ViewModel을 초기화합니다.
 */
class MapViewModelFactory(private val mapRepository: MapRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(mapRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
