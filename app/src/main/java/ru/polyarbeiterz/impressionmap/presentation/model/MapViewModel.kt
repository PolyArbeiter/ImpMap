package ru.polyarbeiterz.impressionmap.presentation.model

import android.content.Context
import android.graphics.PointF
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.mapkit.Animation
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.polyarbeiterz.impressionmap.R
import ru.polyarbeiterz.impressionmap.data.service.ImpressionService
import ru.polyarbeiterz.impressionmap.data.service.LocationService
import javax.inject.Inject

data class MapUiState(
    val currentLocation: Location? = null,
    val mapView: MapView? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val locationService: LocationService,
    val impressionService: ImpressionService
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

    fun updateFocusInfo() {
        _uiState.update { mapUiState ->
            val newMapUiState = mapUiState.copy()
            val mapWindow = newMapUiState.mapView!!.mapWindow
            mapWindow.focusPoint = ScreenPoint(
                mapWindow.width() / 2f,
                mapWindow.height() / 2f,
            )
            newMapUiState
        }
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _isLoadingLocation.value = true
            try {
                val location = locationService.getCurrentLocation()
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

    fun getMap(): com.yandex.mapkit.map.Map? = _uiState.value.mapView?.map

    fun createPlacemark(point: Point) {
        getMap()!!.mapObjects.addPlacemark().apply {
            geometry = point
            setIcon(
                ImageProvider.fromResource(
                    context,
                    R.drawable.marker_down
                ),
                IconStyle().apply { anchor = PointF(0.5f, 1.0f) })
            isDraggable = true
        }
    }

    fun getCameraPositionTarget() = getMap()!!.cameraPosition.target

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