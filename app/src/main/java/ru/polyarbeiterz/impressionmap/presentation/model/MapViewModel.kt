package ru.polyarbeiterz.impressionmap.presentation.model

import android.content.Context
import android.graphics.PointF
import android.location.Location
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.mapkit.Animation
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.polyarbeiterz.impressionmap.R
import ru.polyarbeiterz.impressionmap.core.logic.ImpressionSynchronizer
import ru.polyarbeiterz.impressionmap.data.datastore.PreferencesKeys
import ru.polyarbeiterz.impressionmap.data.datastore.dataStore
import ru.polyarbeiterz.impressionmap.data.dto.ImpressionDto
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.data.service.ImpressionBackendService
import ru.polyarbeiterz.impressionmap.data.service.ImpressionService
import ru.polyarbeiterz.impressionmap.data.service.LocationService
import javax.inject.Inject

data class MapUiState(
    val currentLocation: Location? = null,
    val mapView: MapView? = null,
    val selectedImpressionId: Int? = null
)

data class PlacemarkData(
    val id: Int,
    val point: Point,
    val mapObject: PlacemarkMapObject
)


@HiltViewModel
class MapViewModel @Inject constructor(
    @param:ApplicationContext val context: Context,
    val locationService: LocationService,
    val impressionService: ImpressionService,
    val retrofitService: ImpressionBackendService,
    val synchronizerService: ImpressionSynchronizer,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _isLoadingLocation = MutableStateFlow(false)
    val isLoadingLocation: StateFlow<Boolean> = _isLoadingLocation.asStateFlow()

    private val _placemarks = mutableMapOf<Int, PlacemarkData>()
    val placemarks: Map<Int, PlacemarkData> get() = _placemarks

    private val mapObjectTapListener = MapObjectTapListener { mapObject, point ->
        val id = _placemarks.entries.find { it.value.mapObject == mapObject }?.value?.id
        if (id != null) {
            _uiState.value = _uiState.value.copy(selectedImpressionId = id)
        }
        true
    }


    val allImpressions: StateFlow<List<ImpressionLocal>> = impressionService.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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

    fun createPlacemark(id: Int, point: Point) {
        val placemark = getMap()!!.mapObjects.addPlacemark().apply {
            geometry = point
            setIcon(
                ImageProvider.fromResource(context, R.drawable.marker_down),
                IconStyle().apply {
                    anchor = PointF(0.5f, 1.0f)
                    scale = 1.5f
                })
            isDraggable = true
            addTapListener(mapObjectTapListener)  // Reuse same listener
        }
        _placemarks[id] = PlacemarkData(id, point, placemark)
    }

    fun getPlacemarkScreenPosition(impressionId: Int): Offset? {
        val placemark = _placemarks[impressionId]?.mapObject ?: return null
        val mapWindow = _uiState.value.mapView?.mapWindow ?: return null
        val screenPoint = mapWindow.worldToScreen(placemark.geometry)
        return screenPoint?.let { Offset(it.x, it.y) }
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

    suspend fun synchronize(
        local: Iterable<ImpressionDto>,
        remote: Iterable<ImpressionDto>
    ) {
        if (!shouldSync.first()) return
        synchronizerService.synchronize(local, remote)
    }

    fun deselectImpression() {
        _uiState.value = _uiState.value.copy(selectedImpressionId = null)
    }

    val shouldSync: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            val ip = preferences[PreferencesKeys.SELECTED_HOST_IP] != "127.0.0.1"
            val port = preferences[PreferencesKeys.SELECTED_HOST_PORT] != -1
            ip && port
        }
}