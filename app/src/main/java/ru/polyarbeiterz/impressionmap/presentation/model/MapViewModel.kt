package ru.polyarbeiterz.impressionmap.presentation.model

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.polyarbeiterz.impressionmap.data.LocationRepository

data class MapUiState(
    val currentLocation: Location? = null,
    val mapView: MapView? = null
)

class MapViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _isLoadingLocation = MutableStateFlow(false)
    val isLoadingLocation: StateFlow<Boolean> = _isLoadingLocation.asStateFlow()

    fun setMapView(mapView: MapView?) {
        _uiState.update { it.copy(mapView = mapView) }
    }

    fun updateLocation(currentLocation: Location?) {
        _uiState.update { it.copy(currentLocation = currentLocation) }
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _isLoadingLocation.value = true
            try {
                val location = locationRepository.getCurrentLocation()
                if (location != null) {
                    updateLocation(location)
                    moveMapToLocation(location)
                } else {
                    // TODO: Ui error message
                }
            } finally {
                _isLoadingLocation.value = false
            }
        }
    }

    private fun moveMapToLocation(location: Location) {
        val mapView = _uiState.value.mapView
        if (mapView != null) {
            val point = Point(location.latitude, location.longitude)
            val newPosition = CameraPosition(
                point,
                15f,
                0f,
                0f
            )
            mapView.mapWindow.map.move(
                newPosition,
                Animation(Animation.Type.SMOOTH, 0.5f),
                null
            )
        }
    }
}