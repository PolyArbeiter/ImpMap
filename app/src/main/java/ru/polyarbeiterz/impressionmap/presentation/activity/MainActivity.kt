package ru.polyarbeiterz.impressionmap.presentation.activity

import android.graphics.PointF
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

import ru.polyarbeiterz.impressionmap.BuildConfig
import ru.polyarbeiterz.impressionmap.R
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme
import ru.polyarbeiterz.impressionmap.data.LocationRepository
import ru.polyarbeiterz.impressionmap.presentation.model.MapViewModel

class MainActivity : ComponentActivity() {
    private lateinit var locationRepository: LocationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        locationRepository = LocationRepository(this)
        // TODO: request for permissions and turn on gps

        val mapViewModel = MapViewModel(locationRepository)
        // TODO: that time DI frameworks exist...

        setContent {
            ImpressionMapTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { MainTopBar(mapViewModel) },
                    bottomBar = { MainBottomBar() }
                ) { innerPadding ->
                    MapInteractionScreen(modifier = Modifier.padding(innerPadding), mapViewModel)
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }
    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

}


@Composable
fun MapInteractionScreen(modifier: Modifier = Modifier, viewModel: MapViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var placemarkMapObject by remember { mutableStateOf<PlacemarkMapObject?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            uiState.mapView?.onStop()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    viewModel.setMapView(this)
                    val map = this.mapWindow.map

                    map.move(START_POSITION, START_ANIMATION, null)

                    placemarkMapObject = map.mapObjects.addPlacemark().apply {
                        geometry = START_POSITION.target
                        setIcon(
                            ImageProvider.fromResource(ctx, R.drawable.baseline_location_marker_24),
                            IconStyle().apply { setAnchor(PointF(0.5f, 1.0f)) }
                        )
                        isDraggable = true
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view -> }
        )

        MapControls(
            onZoomIn = {
                uiState.mapView?.mapWindow?.map?.let { map ->
                    val pos = map.cameraPosition
                    map.move(
                        CameraPosition(pos.target, pos.zoom + ZOOM_STEP, pos.azimuth, pos.tilt),
                        SMOOTH_ANIMATION,
                        null
                    )
                }
            },
            onZoomOut = {
                uiState.mapView?.mapWindow?.map?.let { map ->
                    val pos = map.cameraPosition
                    map.move(
                        CameraPosition(pos.target, pos.zoom - ZOOM_STEP, pos.azimuth, pos.tilt),
                        SMOOTH_ANIMATION,
                        null
                    )
                }
            },
            onFocusGeometry = {

            },
            onFocusPlacemark = {

            },
            onCreatePlacemark = {

            },
            modifier = Modifier.fillMaxSize(),
            viewModel
        )
    }
}
private const val ZOOM_STEP = 1f

@Composable
fun MapControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onFocusGeometry: () -> Unit,
    onFocusPlacemark: () -> Unit,
    onCreatePlacemark: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
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
                Text("+", fontSize = MaterialTheme.typography.headlineSmall.fontSize)
            }

            FloatingActionButton(
                onClick = onZoomOut,
                modifier = Modifier.size(48.dp)
            ) {
                Text("-", fontSize = MaterialTheme.typography.headlineLarge.fontSize)
            }
        }
    }
}

@Composable
fun MainTopBar(viewModel: MapViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoadingLocation.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 25.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_settings_24),
            contentDescription = null
        )
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
fun MainBottomBar() {
    Surface(
        color = Color.LightGray,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .padding(bottom = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .clip(RoundedCornerShape(12.dp))
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


//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    ImpressionMapTheme {
////        MainBottomBar()
//
//        ImpressionMapTheme {
//            Scaffold(
//                modifier = Modifier.fillMaxSize(),
//                topBar = { MainTopBar({}) },
//                bottomBar = { MainBottomBar() }) { innerPadding ->
//                // Won't work in preview - no full init.
//                MapInteractionScreen(modifier = Modifier.padding(innerPadding))
//
//            }
//        }
//    }
//}