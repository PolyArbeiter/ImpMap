package ru.polyarbeiterz.impressionmap.presentation.screen

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.launch
import ru.polyarbeiterz.impressionmap.R
import ru.polyarbeiterz.impressionmap.data.datastore.UserProfile
import ru.polyarbeiterz.impressionmap.presentation.components.BottomNavBar
import ru.polyarbeiterz.impressionmap.presentation.model.MapViewModel
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme

@Composable
fun MapComposable(navController: NavController) {
    val context = LocalContext.current
    ImpressionMapTheme {
        MapInteractionScreen(navController, context)
    }
}

private var inputListener: InputListener? = null

@Composable
fun MapInteractionScreen(
    navController: NavController,
    context: Context,
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = hiltViewModel()
) {

    var showExitConfirmation by remember { mutableStateOf(false) }

    val uiState by mapViewModel.uiState.collectAsState()

    var placemarkMapObject by remember { mutableStateOf<PlacemarkMapObject?>(null) }

    val impressionsList = mapViewModel.allImpressions.collectAsState().value

    val shouldSync by mapViewModel.shouldSync.collectAsState(initial = null)

    if (shouldSync == null) {
        CircularProgressIndicator()
        return
    }

    val mapViewModel = hiltViewModel<MapViewModel>()
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var locationEnabled by remember { mutableStateOf(locationManager.isLocationEnabled) }
    val locationPermissionLauncher = rememberLocationPermissionLauncher(
        onPermissionGranted = {
            if (locationEnabled)
                mapViewModel.fetchCurrentLocation()
            else
                Toast.makeText(context, "Сервис геолокации должен быть включен", Toast.LENGTH_SHORT)
                    .show()
        },
        onPermissionDenied = {
            Toast.makeText(
                context,
                "Вы должны дать разрешение на точное местоположение для использования этой функции",
                Toast.LENGTH_SHORT
            ).show()
        }
    )

    DisposableEffect(Unit) {
        val locationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    locationEnabled = locationManager.isLocationEnabled
                }
            }
        }

        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        context.registerReceiver(locationReceiver, filter)

        onDispose {
            context.unregisterReceiver(locationReceiver)
        }
    }

    ImpressionMapTheme {
        MapInteractionScreen(navController, context, locationPermissionLauncher)
    }
}


@Composable
fun MapInteractionScreen(
    navController: NavController,
    context: Context,
    locationPermissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = hiltViewModel()
) {

    var showExitConfirmation by remember { mutableStateOf(false) }

    val uiState by mapViewModel.uiState.collectAsState()

    var placemarkMapObject by remember { mutableStateOf<PlacemarkMapObject?>(null) }

    val impressionsList by mapViewModel.allImpressions.collectAsState()

    // Вместо всего этого, нужно будет загружать иконки пользователей для отображения слева от воспоминаний
    val userProfile by mapViewModel.selectedUserProfile.collectAsState(
        initial = UserProfile(
            null,
            "Имя",
            "Почта"
        )
    )

    var username by remember { mutableStateOf(userProfile.username) }
    var email by remember { mutableStateOf(userProfile.email) }
    var profileImage by remember { mutableStateOf(userProfile.image) }

    LaunchedEffect(userProfile) {
        username = userProfile.username
        email = userProfile.email
        profileImage = userProfile.image
    }

    DisposableEffect(Unit) {
        onDispose {
            uiState.mapView?.onStop()
        }
    }

    LaunchedEffect(Unit) {
        mapViewModel.updateUrl()
    }

    LaunchedEffect(impressionsList.size) {
        impressionsList.forEach { imp ->
            mapViewModel.createPlacemark(
                imp.id,
                Point(imp.latitude!!.toDouble(), imp.longitude!!.toDouble())
            )
        }
    }

    BackHandler(enabled = true) {
        showExitConfirmation = true
    }

    if (showExitConfirmation)
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("Выйти?") },
            text = { Text("Вы точно хотите выйти?") },
            confirmButton = {
                Button(onClick = {
                    (context as? Activity)?.finish()
                }) {
                    Text("Да")
                }
            },
            dismissButton = {
                Button(onClick = { showExitConfirmation = false }) {
                    Text("Нет")
                }
            }
        )

    Box(modifier = modifier.fillMaxSize()) {
        val isDarkTheme = isSystemInDarkTheme()
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapViewModel.setMapView(this)

                    val map = this.mapWindow.map
                    val mapWindow = this.mapWindow

                    if (isDarkTheme) {
                        map.isNightModeEnabled = true
                    }

                    mapWindow.addSizeChangedListener(
                        { _, _, _ -> mapViewModel.updateFocusInfo() }
                    )
                    mapViewModel.updateFocusInfo()

                    if (inputListener == null) {
                        inputListener = object : InputListener {
                            override fun onMapTap(
                                mapWindow: com.yandex.mapkit.map.Map,
                                point: Point
                            ) {
                                mapViewModel.deselectImpression()
                            }

                            override fun onMapLongTap(
                                p0: com.yandex.mapkit.map.Map,
                                point: Point
                            ) {
                            }
                        }
                    }

                    map.addCameraListener { map, cameraPosition, cameraUpdateReason, finished ->
                        placemarkMapObject?.geometry = cameraPosition.target
                    }

                    map.addInputListener(inputListener!!)

                    // set initial position (dirty)
                    mapViewModel.viewModelScope.launch {
                        val loc = mapViewModel.locationService.getCurrentLocation()
                        val point = Point(
                            loc?.latitude ?: START_POSITION.target.latitude,
                            loc?.longitude ?: START_POSITION.target.longitude
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
            onZoomIn = onZoomIn@{
                val now = System.currentTimeMillis()
                if (now - lastZoomTime < ZOOM_DEBOUNCE) return@onZoomIn
                lastZoomTime = now

                val map = mapViewModel.getMap() ?: return@onZoomIn
                val pos = map.cameraPosition
                map.move(
                    CameraPosition(pos.target, pos.zoom + ZOOM_STEP, pos.azimuth, pos.tilt),
                    SMOOTH_ANIMATION,
                    null
                )
            },
            onZoomOut = onZoomOut@{
                val now = System.currentTimeMillis()
                if (now - lastZoomTime < ZOOM_DEBOUNCE) return@onZoomOut
                lastZoomTime = now

                val map = mapViewModel.getMap() ?: return@onZoomOut
                val pos = map.cameraPosition
                map.move(
                    CameraPosition(pos.target, pos.zoom - ZOOM_STEP, pos.azimuth, pos.tilt),
                    SMOOTH_ANIMATION,
                    null
                )
            },
            onCreatePlacemark = {
                // need refactor
                placemarkMapObject = placemarkMapObject.apply {
                    // move and existing placemark
                    placemarkMapObject?.setVisible(true)
                    val focusPoint =
                        mapViewModel.uiState.value.mapView?.mapWindow?.focusPoint ?: return@apply
                    val point =
                        mapViewModel.uiState.value.mapView?.mapWindow?.screenToWorld(focusPoint)
                            ?: return@apply
                    placemarkMapObject?.geometry = point
                } ?: mapViewModel.getMap()!!.mapObjects.addPlacemark().apply {
                    // create placemark if null
                    geometry = mapViewModel.getMap()!!.cameraPosition.target
                    setIcon(
                        ImageProvider.fromResource(context, R.drawable.marker_down),
                        IconStyle().apply {
                            anchor = PointF(0.5f, 1.0f)
                            scale = 2.0f
                        })
                    isDraggable = true
                }
            },
            onRejectPlaceMark = {
                placemarkMapObject?.setVisible(false)
            },
            onStartImpCreation = { lat, lon ->
                navController.navigate(
                    "impression_addition/-1?latitude=${lat}&longitude=${lon}"
                )
            },
            onUpdateImpressions = {
                mapViewModel.viewModelScope.launch {
                    // get remote impressions and sync with them
                    try {
                        mapViewModel.synchronizerService.synchronizeImpressions()
                    } catch (e: Exception) {
                        Log.e("NETWORK", "Could not sync with remote server")
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        )
        MainTopBar(
            navController = navController,
            locationPermissionLauncher = locationPermissionLauncher,
            modifier = modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .padding(horizontal = 8.dp)
        )
        BottomNavBar(
            textLeft = "Карта",
            textRight = "Список",
            onClickLeft = { mapViewModel.deselectImpression() },
            onClickRight = { mapViewModel.deselectImpression(); navController.navigate("impression_list_screen") },
            modifier = modifier
                .align(Alignment.BottomCenter)
        )

        mapViewModel.uiState.collectAsState().value.selectedImpressionId?.let { selectedId ->
            val selectedImpression = impressionsList.find { it.id == selectedId }
            selectedImpression?.let {
                val screenPos = mapViewModel.getPlacemarkScreenPosition(selectedId)
                screenPos?.let { offset ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 80.dp)
                            .navigationBarsPadding()
                            .width(350.dp)
                            .padding(8.dp)
                            .clickable(
                                onClick = {
                                    navController.navigate("impression_addition/${it.id}")
                                    mapViewModel.deselectImpression()
                                },
                            ),
                        elevation = cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (profileImage != null) {
                                val bitmap =
                                    BitmapFactory.decodeByteArray(
                                        profileImage,
                                        0,
                                        profileImage!!.size
                                    )
                                Image(
                                    painter = BitmapPainter(bitmap.asImageBitmap()),
                                    contentDescription = "Фото профиля",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                )
                            }
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(it.title ?: "No title", fontWeight = FontWeight.Bold)
                                Text(it.description ?: "")
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val ZOOM_STEP = 1f
private var lastZoomTime = 0L
private const val ZOOM_DEBOUNCE = 500L

@Composable
fun MapControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onCreatePlacemark: () -> Unit,
    onRejectPlaceMark: () -> Unit,
    onStartImpCreation: (Float, Float) -> Unit,
    onUpdateImpressions: () -> Unit,
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = hiltViewModel()
) {
    var selectPointMode by remember { mutableStateOf(false) }
    val shouldSync by mapViewModel.shouldSync.collectAsState(initial = true)

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
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = null,
                )
            }

            FloatingActionButton(
                onClick = onZoomOut,
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    painter = painterResource(R.drawable.minus),
                    contentDescription = null,
                )
            }
        }

        if (shouldSync)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(end = 16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.update),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .navigationBarsPadding()
                        .clickable(onClick = onUpdateImpressions)
                )
            }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!selectPointMode) {
                // Usual mode
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(onClick = {
                            onCreatePlacemark(); selectPointMode =
                            true; mapViewModel.deselectImpression()
                        })
                )
            } else {
                // Confirm selected point or not
                Icon(
                    painter = painterResource(R.drawable.cross_bolnisi),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(onClick = { onRejectPlaceMark(); selectPointMode = false })

                )
                Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(onClick = {
                            mapViewModel.deselectImpression()
                            onStartImpCreation(
                                mapViewModel.getCameraPositionTarget().latitude.toFloat(),
                                mapViewModel.getCameraPositionTarget().longitude.toFloat()
                            )
                        })
                )
            }
        }
    }
}

@Composable
fun MainTopBar(
    navController: NavController,
    locationPermissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    modifier: Modifier,
    mapViewModel: MapViewModel = hiltViewModel()
) {
    val isLoading by mapViewModel.isLoadingLocation.collectAsState()

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp)
    ) {
        Button(
            onClick = { mapViewModel.deselectImpression(); navController.navigate("settings_screen") },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_settings_24),
                contentDescription = null
            )
        }
        Button(
            onClick = { mapViewModel.deselectImpression() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.baseline_filter_list_24),
                    contentDescription = null
                )
                Text(text = "Фильтр", modifier = Modifier.padding(horizontal = 12.dp))
                if (1 == 2) { // Заглушка, потом добавлю функцию фильтрации и заживём...
                    Surface(color = Color.White, modifier = Modifier.clip(CircleShape)) {
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
        }
        Button(
            onClick = {
                mapViewModel.deselectImpression()
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
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
fun rememberLocationPermissionLauncher(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) =
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted =
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

private val START_ANIMATION = Animation(Animation.Type.LINEAR, 1f)
private val SMOOTH_ANIMATION = Animation(Animation.Type.SMOOTH, 0.4f)
private val START_POSITION = CameraPosition(Point(54.707590, 20.508898), 15f, 0f, 0f)

