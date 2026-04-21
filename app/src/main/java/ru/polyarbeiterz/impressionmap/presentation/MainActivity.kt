package ru.polyarbeiterz.impressionmap.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import ru.polyarbeiterz.impressionmap.R
import ru.polyarbeiterz.impressionmap.data.datastore.UserCredentials
import ru.polyarbeiterz.impressionmap.data.entity.Host
import ru.polyarbeiterz.impressionmap.presentation.components.EntityCard
import ru.polyarbeiterz.impressionmap.presentation.model.MainActivityModel
import ru.polyarbeiterz.impressionmap.presentation.screen.ImpressionAdditionComposable
import ru.polyarbeiterz.impressionmap.presentation.screen.ImpressionListComposable
import ru.polyarbeiterz.impressionmap.presentation.screen.MapComposable
import ru.polyarbeiterz.impressionmap.presentation.screen.SettingsComposable
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ImpressionMap)
        enableEdgeToEdge()
        try {
            MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        } catch (e: AssertionError) {
            Log.i("MAPKIT", "Trying to set API Key after MapKit was already initialized")
        }

        MapKitFactory.initialize(this)
        setContent {
            ImpressionMapTheme {
                AppNavigation(LocalContext.current)
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
fun AppNavigation(context: Context) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "choice_screen"
    ) {
        composable("choice_screen") {
            ChoiceScreenComposable(navController = navController, context = context)
        }

        composable("map_screen") {
            MapComposable(navController)
        }

        composable(
            "impression_addition/{impressionId}?latitude={latitude}&longitude={longitude}",
            arguments = listOf(
                navArgument("impressionId") { type = NavType.IntType },
                navArgument("latitude") { type = NavType.FloatType; defaultValue = 0f },
                navArgument("longitude") { type = NavType.FloatType; defaultValue = 0f }
            )
        ) { backStackEntry ->
            val impressionId = backStackEntry.arguments?.getInt("impressionId") ?: -1
            val defaultLat = backStackEntry.arguments?.getFloat("latitude") ?: 0f
            val defaultLon = backStackEntry.arguments?.getFloat("longitude") ?: 0f
            ImpressionAdditionComposable(
                navController = navController,
                impressionId = impressionId,
                defaultLat,
                defaultLon
            )
        }
        composable("impression_list_screen") {
            ImpressionListComposable(navController)
        }
        composable("settings_screen") {
            SettingsComposable(navController)
        }
    }
}


@Composable
fun ChoiceScreenComposable(
    navController: NavController,
    modifier: Modifier = Modifier,
    context: Context,
    mainActivityModel: MainActivityModel = hiltViewModel()
) {

    val selectedHost by mainActivityModel.selectedHost.collectAsState(
        initial = Host(
            name = "loading",
            ip = "",
            port = -2
        )
    )

    LaunchedEffect(selectedHost) {
        if (selectedHost?.name != "loading" && selectedHost?.port != -2 && selectedHost != null) {
            navController.navigate("map_screen") {
                popUpTo("choice_screen") { inclusive = true }
            }
        }
    }

    ImpressionMapTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (selectedHost?.name == "loading") {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (selectedHost?.port == -2 || selectedHost == null) {
                ChoiceButtonScreen(
                    navController,
                    modifier = Modifier.align(Alignment.Center),
                    context = context
                )
            }
        }
    }
}

@Composable
fun ChoiceButtonScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    context: Context
) {
    var showModal by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier
            .padding(20.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (showModal)
            ChoiceDialog(navController, onDismissRequest = { showModal = false })

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
                text = "Выберите, где будете хранить данные о своих впечатлениях",
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { showModal = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(14.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "Выбрать")
            }
        }
    }
}

@Composable
fun ChoiceDialog(
    navController: NavController,
    onDismissRequest: () -> Unit,
    mainActivityModel: MainActivityModel = hiltViewModel()
) {
    val serverList by mainActivityModel.allHosts.collectAsState()
    val selectedHost by mainActivityModel.selectedHost.collectAsState(
        initial = Host(
            name = "local_mode",
            ip = "127.0.0.1",
            port = -1
        )
    )
    val selectedUserCredentials by mainActivityModel.selectedUserCredentials.collectAsState(
        initial = UserCredentials(
            username = "",
            password = ""
        )
    )
    var showAdditionModal by remember { mutableStateOf(false) }
    var hostToDelete by remember { mutableStateOf<Host?>(null) }
    var showConnectionError by remember { mutableStateOf(false) }
    var showCredentialsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedUserCredentials) {
        mainActivityModel.urlManager.updateBasic(
            selectedUserCredentials
        )
    }

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

    if (showConnectionError) {
        AlertDialog(
            onDismissRequest = { showConnectionError = false },
            title = { Text("Сервер недоступен") },
            text = { Text("Не удалось подключиться к выбранному серверу. Проверьте адрес и порт.") },
            confirmButton = {
                Button(
                    onClick = { showConnectionError = false }
                ) {
                    Text("ОК")
                }
            }
        )
    }

    if (showCredentialsDialog)
        CredentialsDialog(
            showModal = showCredentialsDialog,
            onDismiss = {
                showCredentialsDialog = false
            },
            onConfirm = { username, password ->
                mainActivityModel.viewModelScope.launch {
                    val cred = UserCredentials(username, password)
                    mainActivityModel.selectUserCredentials(cred)

                    val secondTry = mainActivityModel.checkServerConnection(
                        mainActivityModel.urlManager.baseUrl.value,
                        UserCredentials(username, password)
                    )

                    if (!secondTry) {
                        mainActivityModel.selectLocalMode()
                        showConnectionError = true
                    }

                    showCredentialsDialog = false
                }
            }
        )

    if (showAdditionModal)
        ServerAdditionDialog(
            onDismissRequest = { showAdditionModal = false },
            onAddition = { server: Host, userCredentials: UserCredentials ->
                mainActivityModel.viewModelScope.launch {
                    // try to select and login
                    mainActivityModel.selectHost(server)
                    mainActivityModel.urlManager.updateUrl(
                        "http://${server.ip}:${server.port}"
                    )
                    mainActivityModel.selectUserCredentials(userCredentials)

                    val couldAuth = mainActivityModel.loginRequest(userCredentials)

                    if (couldAuth) {
                        mainActivityModel.insertHost(server)
                    } else {
                        mainActivityModel.selectLocalMode()
                        showConnectionError = true
                    }
                }
                showAdditionModal = false
            },
            isValidIp = mainActivityModel::isValidIp,
            isValidPort = mainActivityModel::isValidPort
        )

    Dialog({ onDismissRequest() }) {
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.height(500.dp)
                ) {
                    item(key = "local_mode") {
                        EntityCard(
                            cardName = "Ваше устройство",
                            cardDescription = "Данные сохранены локально",
                            onClick = { mainActivityModel.selectLocalMode() },
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
                        EntityCard(
                            cardName = el.name ?: "Нет имени",
                            cardDescription = "IP: " + el.ip + ", порт: " + el.port,
                            onClick = {
                                mainActivityModel.viewModelScope.launch {
                                    val url = "http://${el.ip}:${el.port}"
                                    val isReachable = if (el.ip == "127.0.0.1") {
                                        true
                                    } else {
                                        val url = "http://${el.ip}:${el.port}"
                                        // use existing credentials from datastore
                                        mainActivityModel.checkServerConnection(
                                            url, selectedUserCredentials
                                        )
                                    }
                                    mainActivityModel.selectHost(el)
                                    mainActivityModel.urlManager.updateUrl(url)
                                    if (!isReachable) {
                                        showCredentialsDialog = true
                                    }
                                }
                            },
                            onLongPress = { hostToDelete = el },
                            chosen = selectedHost?.ip == el.ip && selectedHost?.port == el.port
                        )
                    }
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showAdditionModal = true }
                ) {
                    Text(text = "Добавить сервер")
                }
            }
        }
    }
}

@Composable
fun ServerAdditionDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onAddition: (server: Host, userCredentials: UserCredentials) -> Unit,
    isValidIp: (String) -> Boolean,
    isValidPort: (Int) -> Boolean
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                    OutlinedTextField(
                        value = username,
                        label = { Text(text = "Имя пользователя") },
                        onValueChange = { username = it },
                        isError = username.isEmpty(),
                        supportingText = {
                            if (username.isEmpty()) {
                                Text("Поле не может быть пустым", color = Color.Red)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        label = { Text(text = "Пароль") },
                        onValueChange = { password = it },
                        isError = password.isEmpty(),
                        supportingText = {
                            if (password.isEmpty()) {
                                Text("Поле не может быть пустым", color = Color.Red)
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
                            ),
                            UserCredentials(username, password)
                        )
                        name = ""
                        address = ""
                        port = ""
                    },
                    enabled = (address.isNotEmpty() && isValidIp(address)) &&
                            (port.isNotEmpty() && isValidPort(port.toIntOrNull() ?: 0)) &&
                            (username.isNotEmpty() && password.isNotEmpty()),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(text = "Добавить") }
                Button(
                    onClick = { onDismissRequest() }, modifier = Modifier.fillMaxWidth()
                ) { Text(text = "Отменить") }
            }
        }
    }
}

@Composable
fun CredentialsDialog(
    showModal: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (username: String, password: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isUsernameError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    if (showModal) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Введите учетные данные") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            isUsernameError = it.isEmpty()
                        },
                        label = { Text("Имя пользователя") },
                        isError = isUsernameError,
                        supportingText = {
                            if (isUsernameError) {
                                Text("Поле не может быть пустым", color = Color.Red)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            isPasswordError = it.isEmpty()
                        },
                        label = { Text("Пароль") },
                        isError = isPasswordError,
                        supportingText = {
                            if (isPasswordError) {
                                Text("Поле не может быть пустым", color = Color.Red)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            username.isEmpty() -> isUsernameError = true
                            password.isEmpty() -> isPasswordError = true
                            else -> {
                                onConfirm(username, password)
                                username = ""
                                password = ""
                            }
                        }
                    }
                ) {
                    Text("Подключиться")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        )
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