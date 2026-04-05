package ru.polyarbeiterz.impressionmap.presentation

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.polyarbeiterz.impressionmap.BuildConfig
import ru.polyarbeiterz.impressionmap.data.entity.Host
import ru.polyarbeiterz.impressionmap.presentation.model.MainActivityModel
import ru.polyarbeiterz.impressionmap.presentation.screen.ImpressionAdditionScreen
import ru.polyarbeiterz.impressionmap.presentation.screen.ImpressionListComposable
import ru.polyarbeiterz.impressionmap.presentation.screen.MapComposable
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)

        setContent {
            AppNavigation(LocalContext.current)
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
fun AppNavigation(context: Context) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "choice_screen"
    ) {
        composable("choice_screen") {
            ChoiceScreen(navController = navController, context = context)
        }

        composable("map_screen") {
            MapComposable(navController)
        }

        composable(
            "impression_addition/{lat}/{lon}",
            arguments = listOf(
                navArgument("lat") { type = NavType.FloatType },
                navArgument("lon") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getFloat("lat") ?: 0.0
            val lon = backStackEntry.arguments?.getFloat("lon") ?: 0.0
            ImpressionAdditionScreen(
                navController,
                lat.toFloat(),
                lon.toFloat()
            )
        }
        composable("impression_list") {
            ImpressionListComposable(navController)
        }
    }
}


@Composable
fun ChoiceScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    context: Context
) {
    ImpressionMapTheme {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = modifier.fillMaxSize()) {
                ChoiceButton(
                    navController,
                    modifier = Modifier.align(Alignment.Center),
                    context = context
                )
            }
        }
    }
}

@Composable
fun ChoiceButton(navController: NavController, modifier: Modifier = Modifier, context: Context) {
    var showModal by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier
            .padding(20.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        ChoiceDialog(navController, showModal, context, onDismissRequest = { showModal = false })

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Режим работы",
                fontWeight = FontWeight.Black,
                fontSize = 30.sp,
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = "Выберите, где будете хранить данные о своих воспоминаниях",
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { showModal = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(14.dp))
            ) {
                Text(text = "Выбрать")
            }
        }
    }
}

@Composable
fun ChoiceDialog(
    navController: NavController,
    showModal: Boolean,
    context: Context,
    onDismissRequest: () -> Unit,
    mainActivityModel: MainActivityModel = hiltViewModel()
) {
    val serverList by mainActivityModel.allHosts.collectAsState()
    val selectedHost by mainActivityModel.selectedHost.collectAsState(initial = null)
    var showAdditionModal by remember { mutableStateOf(false) }
    var hostToDelete by remember { mutableStateOf<Host?>(null) }

    var locationPermissionGranted by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLocationPermissionLauncher(
        onPermissionGranted = {
            locationPermissionGranted = true
            Log.d("Permission", "Геолокация разрешена")
            navController.navigate("map_screen")
        },
        onPermissionDenied = {
            locationPermissionGranted = false
            Log.d("Permission", "Геолокация запрещена")
            navController.navigate("map_screen")
        }
    )

    if (hostToDelete != null) {
        AlertDialog(
            onDismissRequest = { hostToDelete = null },
            title = { Text("Удалить сервер?") },
            text = { Text("Вы уверены, что хотите удалить \"${hostToDelete?.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        mainActivityModel.deleteHost(hostToDelete!!)
                        hostToDelete = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        hostToDelete = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    ServerAdditionDialog(
        showModal = showAdditionModal,
        onDismissRequest = { showAdditionModal = false },
        onAddition = { server: Host ->
            // add and update serverList
            mainActivityModel.viewModelScope.launch {
                mainActivityModel.insertHost(server)
            }
            showAdditionModal = false
        },
        isValidIp = mainActivityModel::isValidIp,
        isValidPort = mainActivityModel::isValidPort
    )

    if (showModal) Dialog({ onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                LazyVerticalGrid(columns = GridCells.Fixed(1), modifier = Modifier.height(500.dp)) {
                    item(key = "local_mode") {
                        ChoiceCard(
                            cardName = "Ваше устройство",
                            cardDescription = "Данные сохранены локально",
                            onClick = { mainActivityModel.selectLocalMode() },
                            onLongPress = {},
                            chosen = selectedHost?.ip == "127.0.0.1" && selectedHost?.port == -1
                        )
                    }
                    items(
                        serverList.size,
                        key = { index ->
                            "${serverList[index].ip}:${serverList[index].port}"
                        }
                    ) { index ->
                        val el = serverList.elementAt(index)
                        ChoiceCard(
                            cardName = el.name ?: "Нет имени",
                            cardDescription = "IP: " + el.ip + ", порт: " + el.port,
                            onClick = { mainActivityModel.selectHost(el) },
                            onLongPress = { hostToDelete = el },
                            chosen = selectedHost?.ip == el.ip && selectedHost?.port == el.port
                        )
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(), onClick = { showAdditionModal = true }) {
                    Text(text = "Добавить сервер")
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedHost != null,
                    onClick = {
                        if (selectedHost?.ip != "127.0.0.1" && selectedHost?.port != -1)
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                        else
                            navController.navigate("map_screen")
                    }) { Text(text = "Продолжить") }
            }
        }
    }
}

@Composable
fun ChoiceCard(
    modifier: Modifier = Modifier,
    cardName: String,
    cardDescription: String,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {},
    chosen: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(shape = RoundedCornerShape(16.dp))
            .indication(
                interactionSource = interactionSource,
                indication = ripple()
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        Surface(
            color = if (chosen) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = cardName,
                        textAlign = TextAlign.Left,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = cardDescription, textAlign = TextAlign.Left
                    )
                }
                if (chosen) Text(
                    text = "✓", modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
fun ServerAdditionDialog(
    showModal: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onAddition: (server: Host) -> Unit,
    isValidIp: (String) -> Boolean,
    isValidPort: (Int) -> Boolean
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }

    if (showModal) {
        Dialog({ onDismissRequest() }) {
            Card(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Добавить новый сервер",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Column(
                        horizontalAlignment = Alignment.Start, modifier = Modifier.padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            label = { Text(text = "Имя сервера") },
                            onValueChange = { name = it },
                            supportingText = {
                                if (name.isNotEmpty() && name.length in 10..20) {
                                    Text("Хорошее название", color = Color.White)
                                } else if (name.isNotEmpty() && name.length > 20) {
                                    Text("Хватит", color = Color.White)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = address,
                            label = { Text(text = "IP-адрес") },
                            onValueChange = { address = it },
                            isError = address.isNotEmpty() && !isValidIp(address),
                            supportingText = {
                                if (address.isNotEmpty() && !isValidIp(address)) {
                                    Text("Неверный формат IP", color = Color.Red)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = port,
                            label = { Text(text = "Порт") },
                            onValueChange = { port = it },
                            isError = port.isNotEmpty() && !isValidPort(port.toIntOrNull() ?: 0),
                            supportingText = {
                                if (port.isNotEmpty() && !isValidPort(port.toIntOrNull() ?: 0)) {
                                    Text("Неверный номер порта", color = Color.Red)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Button(
                        onClick = {
                            onAddition(
                                Host(
                                    name = name, ip = address, port = port.toInt()
                                )
                            )
                            name = ""
                            address = ""
                            port = ""
                        },
                        enabled = (address.isNotEmpty() && isValidIp(address)) && (port.isNotEmpty() && isValidPort(
                            port.toIntOrNull() ?: 0
                        )),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(text = "Добавить") }
                    Button(
                        onClick = { onDismissRequest() }, modifier = Modifier.fillMaxWidth()
                    ) { Text(text = "Отменить") }
                }
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
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

@Preview(showBackground = true)
@Composable
fun PreviewChoiceButton() {
    ImpressionMapTheme {
//        ChoiceScreen(context = LocalContext.current)
//        ChoiceDialog(showModal = true, context = LocalContext.current, onDismissRequest = {})
//        ServerAdditionDialog(showModal = true, onDismissRequest = {})
    }
}