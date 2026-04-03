package ru.polyarbeiterz.impressionmap.presentation.activity

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.polyarbeiterz.impressionmap.BuildConfig
import ru.polyarbeiterz.impressionmap.R
import ru.polyarbeiterz.impressionmap.data.entity.Impression
import ru.polyarbeiterz.impressionmap.presentation.model.MapViewModel
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)

        setContent {
            ImpressionMapTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { MainTopBar() },
                    bottomBar = { MainBottomBar() }
                ) { innerPadding ->
                    MapInteractionScreen(
                        LocalContext.current,
                        modifier = Modifier.padding(innerPadding)
                    )
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
fun MapInteractionScreen(
    context: Context, modifier:
    Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    var placemarkMapObject by remember { mutableStateOf<PlacemarkMapObject?>(null) }

    var savedImpressions by remember {
        mutableStateOf<MutableSet<Impression>>(mutableSetOf())
    }

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
                    val mapWindow = this.mapWindow

                    mapWindow.addSizeChangedListener(
                         {_, _, _ -> viewModel.updateFocusInfo()}
                    )
                    viewModel.updateFocusInfo()

                    map.move(START_POSITION, START_ANIMATION, null)

                    var got: List<Impression>
                    // read all placemarks
                    viewModel.viewModelScope.launch {
                        got = viewModel.impressionService.getAll()
//                            .filter {
//                                    imp -> imp.longitude != null &&
//                                    imp.latitude != null &&
//                                    !savedImpressions.contains(imp)
//                            }
//                            .forEach { imp ->  savedImpressions.add(imp) }
                    }

                    // reflect on page
                    savedImpressions.forEach { imp ->
                        viewModel.createPlacemark(
                            Point(imp.latitude!!, imp.longitude!!)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
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
                placemarkMapObject = placemarkMapObject.apply {
                    placemarkMapObject?.setVisible(true)
                    val focusPoint = viewModel.uiState.value.mapView?.mapWindow?.focusPoint ?: return@apply
                    val point = viewModel.uiState.value.mapView?.mapWindow?.screenToWorld(focusPoint) ?: return@apply
                    placemarkMapObject?.geometry = point
                } ?:viewModel.getMap()!!.mapObjects.addPlacemark().apply {
                    geometry = viewModel.getMap()!!.cameraPosition.target
                    setIcon(
                        ImageProvider.fromResource(context, R.drawable.ic_dollar_pin),
                        IconStyle().apply { anchor = PointF(0.5f, 1.0f) })
                    isDraggable = true
                }
            },
            onRejectPlaceMark = {
              placemarkMapObject?.setVisible(false)
            },
            onStartImpCreation = {
                context.startActivity(
                    Intent(
                        context, ImpressionAdditionActivity::class.java
                    )
                )
                // read all placemarks
                viewModel.viewModelScope.launch {
                    viewModel.impressionService.getAll()
                        .filter {
                                imp -> imp.longitude != null &&
                                imp.latitude != null &&
                                !savedImpressions.contains(imp)
                        }
                        .forEach { imp ->  savedImpressions.add(imp) }
                }

                // reflect on page
                savedImpressions.forEach { imp ->
                    viewModel.createPlacemark(
                        Point(imp.latitude!!, imp.longitude!!)
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
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
    onStartImpCreation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectPointMode by remember {mutableStateOf(false)}

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
            } else {
                // Confirm selected point or not
                FloatingActionButton(
                    onClick = { onStartImpCreation() },
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
fun MainTopBar(viewModel: MapViewModel = hiltViewModel()) {
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