package ru.polyarbeiterz.impressionmap.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import ru.polyarbeiterz.impressionmap.BuildConfig
import ru.polyarbeiterz.impressionmap.presentation.screen.ImpressionAdditionScreen
import ru.polyarbeiterz.impressionmap.presentation.screen.MapComposable
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme

class Server(val name: String, val ip: String, val port: String)
//TODO() Необходимо добавить санитизацию/парсинг IP-адреса и порта
// И вывод тоста об ошибке, если что-то не подходит
// Добавить в конструктор парсинг того и другого. Может быть добавить отдельный класс для адреса

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

        composable("impression_addition") {
            ImpressionAdditionScreen(navController)
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
                ChoiceButton(navController, modifier = Modifier.align(Alignment.Center), context = context)
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
fun ChoiceDialog(navController: NavController, showModal: Boolean, context: Context, onDismissRequest: () -> Unit) {
    var serverList by remember { mutableStateOf(listOf<Server>()) }
    var showAdditionModal by remember { mutableStateOf(false) }

    ServerAdditionDialog(
        showModal = showAdditionModal,
        onDismissRequest = { showAdditionModal = false },
        onAddition = { server: Server ->
            serverList = serverList + server
            showAdditionModal = false
        })

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
                    item {
                        ChoiceCard(
                            cardName = "Ваше устройство",
                            cardDescription = "Данные сохранены локально",
                            onClick = {},
                            chosen = true
                        )
                    }
                    items(serverList) { server ->
                        ChoiceCard(
                            cardName = server.name,
                            cardDescription = "IP: " + server.ip + ", порт: " + server.port,
                            onClick = {},
                            //TODO() При нажатии на сервер, все остальные должны перестать быть активными
                            // Ввести систему выбора текущего сервера для работы наверное
                            chosen = false
                        )
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(), onClick = { showAdditionModal = true }) {
                    Text(text = "Добавить сервер")
                }
                Button(
                    modifier = Modifier.fillMaxWidth(), onClick = {
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
    chosen: Boolean
) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
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
    onAddition: (server: Server) -> Unit
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
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = address,
                            label = { Text(text = "IP-адрес") },
                            onValueChange = { address = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = port,
                            label = { Text(text = "Порт") },
                            onValueChange = { port = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Button(
                        onClick = {
                            onAddition(
                                Server(
                                    name = name, ip = address, port = port
                                )
                            )
                            name = ""
                            address = ""
                            port = ""
                        }, modifier = Modifier.fillMaxWidth()
                    ) { Text(text = "Добавить") }
                    Button(
                        onClick = { onDismissRequest() }, modifier = Modifier.fillMaxWidth()
                    ) { Text(text = "Отменить") }
                }
            }
        }
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