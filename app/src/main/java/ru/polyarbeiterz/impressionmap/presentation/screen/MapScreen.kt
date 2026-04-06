package ru.polyarbeiterz.impressionmap.presentation.screen

import android.content.Context
import android.graphics.PointF
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.launch
import ru.polyarbeiterz.impressionmap.R
import ru.polyarbeiterz.impressionmap.core.logic.filterAndSaveImpressionsWithCoords
import ru.polyarbeiterz.impressionmap.core.utils.toServerDto
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.presentation.model.MapViewModel
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme

@Composable
fun MapComposable(navController: NavController) {
    ImpressionMapTheme {
        MapInteractionScreen(
            navController,
            LocalContext.current,
        )
    }
}


@Composable
fun MapInteractionScreen(
    navController: NavController,
    context: Context,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {

    BackHandler(enabled = true) {
        // Кнопка "назад" пока просто игнорируется, чтобы нельзя было перейти обратно на меню выбора
    }

    val uiState by viewModel.uiState.collectAsState()

    var placemarkMapObject by remember { mutableStateOf<PlacemarkMapObject?>(null) }

    val savedImpressionLocals = remember {
        mutableStateSetOf<ImpressionLocal>()
    }

    DisposableEffect(Unit) {
        onDispose {
            uiState.mapView?.onStop()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.viewModelScope.launch {
            // get all impressions with coords from local database
            viewModel.impressionService.getAll()
                .filterAndSaveImpressionsWithCoords(savedImpressionLocals)

            // get remote impressions and sync with them
            try {
                viewModel.retrofitService.getAllImpressions()
                    .takeIf { it.isSuccessful }
                    .apply {
                        viewModel.synchronizerService.synchronize(
                            savedImpressionLocals.map { it.toServerDto() },
                            this?.body() ?: emptyList()
                        )
                    }
            } catch(e: Exception) {
                Log.e("NETWORK", "Could not sync with remote server")
            }
        }
    }

    LaunchedEffect(savedImpressionLocals.size) {
        // reflect all placemarks
        savedImpressionLocals.forEach { imp ->
            viewModel.createPlacemark(
                Point(imp.latitude!!.toDouble(), imp.longitude!!.toDouble())
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    viewModel.setMapView(this)

                    val map = this.mapWindow.map
                    val mapWindow = this.mapWindow

                    mapWindow.addSizeChangedListener(
                        { _, _, _ -> viewModel.updateFocusInfo() }
                    )
                    viewModel.updateFocusInfo()

                    // set initial position (dirty)
                    viewModel.viewModelScope.launch {
                        val loc = viewModel.locationService.getCurrentLocation()
                        val point = Point(
                            loc?.latitude ?: START_POSITION.target.latitude,
                            loc?.latitude ?: START_POSITION.target.latitude
                        )
                        map.move(
                            CameraPosition(point, 15f, 0f, 0f),
                            START_ANIMATION,
                            null
                        )
                    }
                }
            },
            modifier = modifier,
            update = { view -> }
        )

        MapControls(
            onZoomIn = {
                viewModel.getMap()?.let { map ->
                    val pos = map.cameraPosition
                    map.move(
                        CameraPosition(pos.target, pos.zoom + ZOOM_STEP, pos.azimuth, pos.tilt),
                        SMOOTH_ANIMATION,
                        null
                    )
                }
            },
            onZoomOut = {
                viewModel.getMap()?.let { map ->
                    val pos = map.cameraPosition
                    map.move(
                        CameraPosition(pos.target, pos.zoom - ZOOM_STEP, pos.azimuth, pos.tilt),
                        SMOOTH_ANIMATION,
                        null
                    )
                }
            },
            onCreatePlacemark = {
                // need refactor
                placemarkMapObject = placemarkMapObject.apply {
                    // move and existing placemark
                    placemarkMapObject?.setVisible(true)
                    val focusPoint =
                        viewModel.uiState.value.mapView?.mapWindow?.focusPoint ?: return@apply
                    val point =
                        viewModel.uiState.value.mapView?.mapWindow?.screenToWorld(focusPoint)
                            ?: return@apply
                    placemarkMapObject?.geometry = point
                } ?: viewModel.getMap()!!.mapObjects.addPlacemark().apply {
                    // create placemark if null
                    geometry = viewModel.getMap()!!.cameraPosition.target
                    setIcon(
                        ImageProvider.fromResource(context, R.drawable.marker_down),
                        IconStyle().apply { anchor = PointF(0.5f, 1.0f) })
                    isDraggable = true
                }
            },
            onRejectPlaceMark = {
                placemarkMapObject?.setVisible(false)
            },
            onStartImpCreation = { lat, lon ->
                navController.navigate(
                    "impression_addition/${lat}/${lon}"
                )
            },
            onUpdateImpressions = {
                viewModel.viewModelScope.launch {
                    // get all impressions with coords from local database
                    viewModel.impressionService.getAll()
                        .filterAndSaveImpressionsWithCoords(savedImpressionLocals)

                    // get remote impressions and sync with them
                    try {
                        viewModel.retrofitService.getAllImpressions()
                            .takeIf { it.isSuccessful }
                            .apply {
                                viewModel.synchronizerService.synchronize(
                                    savedImpressionLocals.map { it.toServerDto() },
                                    this?.body() ?: emptyList()
                                )
                            }
                    } catch(e: Exception) {
                        Log.e("NETWORK", "Could not sync with remote server")
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        )
        MainTopBar(navController,
            modifier = modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .padding(horizontal = 8.dp)
                .statusBarsPadding()
        )
        MainBottomBar(
            navController,
            modifier = modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

private const val ZOOM_STEP = 1f

@Composable
fun MapControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onCreatePlacemark: () -> Unit,
    onRejectPlaceMark: () -> Unit,
    onStartImpCreation: (Float, Float) -> Unit,
    onUpdateImpressions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    var selectPointMode by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Zoom Controls - Right side
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = onZoomIn,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = null,
                )
            }

            FloatingActionButton(
                onClick = onZoomOut,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.minus),
                    contentDescription = null,
                )
            }

            if (!selectPointMode) {
                // Usual mode
                FloatingActionButton(
                    onClick = { onCreatePlacemark(); selectPointMode = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.new_box),
                        contentDescription = null,
                    )
                }
                FloatingActionButton(
                    onClick = onUpdateImpressions,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.update),
                        contentDescription = null,
                    )
                }
            } else {
                // Confirm selected point or not
                FloatingActionButton(
                    onClick = {
                        onStartImpCreation(
                            viewModel.getCameraPositionTarget().latitude.toFloat(),
                            viewModel.getCameraPositionTarget().longitude.toFloat()
                        )
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.check),
                        contentDescription = null,
                    )
                }
                FloatingActionButton(
                    onClick = { onRejectPlaceMark(); selectPointMode = false },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cross_bolnisi),
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
fun MainTopBar(
    navController: NavController,
    modifier: Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoadingLocation.collectAsState()

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxSize()
    ) {
        Button(
            onClick = { viewModel.fetchCurrentLocation() },
            enabled = !isLoading
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_settings_24),
                contentDescription = null
            )
        }
        Button(onClick = {}) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.baseline_filter_list_24),
                    contentDescription = null
                )
                Text(text = "Фильтр", modifier = Modifier.padding(horizontal = 12.dp))
                Surface(color = Color.Blue, modifier = Modifier.clip(CircleShape)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    ) {
                        Text(text = "2", modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
        Button(
            onClick = { viewModel.fetchCurrentLocation() },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.baseline_location_marker_24),
                    contentDescription = "Мое местоположение"
                )
            }
        }
    }

}

@Composable
fun MainBottomBar(navController: NavController, modifier: Modifier) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = { navController.navigate("map_screen") })
            ) {
                Text(
                    text = "Карта",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            VerticalDivider(thickness = 1.dp, modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = { navController.navigate("impression_list") })
            ) {
                Text(
                    text = "Список",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

private val START_ANIMATION = Animation(Animation.Type.LINEAR, 1f)
private val SMOOTH_ANIMATION = Animation(Animation.Type.SMOOTH, 0.4f)
private val START_POSITION = CameraPosition(Point(54.707590, 20.508898), 15f, 0f, 0f)

