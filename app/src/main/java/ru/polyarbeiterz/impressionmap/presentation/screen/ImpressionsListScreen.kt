package ru.polyarbeiterz.impressionmap.presentation.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.polyarbeiterz.impressionmap.R
import ru.polyarbeiterz.impressionmap.data.datastore.UserProfile
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.presentation.components.BottomNavBar
import ru.polyarbeiterz.impressionmap.presentation.components.ImpressionListCard
import ru.polyarbeiterz.impressionmap.presentation.model.ImpressionsListModel
import ru.polyarbeiterz.impressionmap.presentation.model.MapViewModel
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme

@Composable
fun ImpressionListComposable(
    navController: NavController,
) {
    ImpressionMapTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ImpressionsListScreen(navController, LocalContext.current)
        }
    }
}

@Composable
fun ImpressionsListScreen(
    navController: NavController,
    context: Context,
    modifier: Modifier = Modifier,
    impressionsListModel: ImpressionsListModel = hiltViewModel()
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column() {
            ListTopBar(
                navController,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .padding(horizontal = 8.dp)
            )
            ImpressionsList(navController, context)
        }
        BottomNavBar(
            textLeft = "Карта",
            textRight = "Список",
            onClickLeft = { navController.navigate("map_screen") },
            onClickRight = {},
            modifier = modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

@Composable
fun ImpressionsList(
    navController: NavController,
    context: Context,
    modifier: Modifier = Modifier,
    impressionsListModel: ImpressionsListModel = hiltViewModel()
) {
    val impressionsList = impressionsListModel.allImpressions.collectAsState().value

    var impressionToDelete by remember { mutableStateOf<ImpressionLocal?>(null) }

    // Вместо всего этого, нужно будет загружать иконки пользователей для отображения слева от воспоминаний
    val userProfile by impressionsListModel.selectedUserProfile.collectAsState(
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


    if (impressionToDelete != null) {
        AlertDialog(
            onDismissRequest = { impressionToDelete = null },
            title = { Text("Удалить воспоминание?") },
            text = { Text("Вы уверены, что хотите удалить \"${impressionToDelete?.title}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        impressionsListModel.deleteImp(impressionToDelete!!.id)
                        impressionToDelete = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        impressionToDelete = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(
            impressionsList.size,
            key = { index ->
                "${impressionsList[index].id}:${impressionsList[index].latitude}:${impressionsList[index].longitude}"
            }) { index ->
            val el = impressionsList.elementAt(index)
            ImpressionListCard(
                cardName = el.title?.takeIf { it.isNotBlank() } ?: "Без названия",
                cardDescription = el.description?.takeIf { it.isNotBlank() } ?: "Без описания",
                onClick = { navController.navigate("impression_addition/${el.id}") },
                onLongPress = { impressionToDelete = el },
                modifier = Modifier.fillMaxWidth(),
                image = if (el.userId == null) {
                    profileImage
                } else {
                    null // TODO() Загружаем из таблицы пользователей
                }

            )
        }
    }
}

@Composable
fun ListTopBar(
    navController: NavController,
    modifier: Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoadingLocation.collectAsState()

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp)
    ) {
        Button(onClick = { navController.navigate("settings_screen") }) {
            Icon(
                painter = painterResource(R.drawable.baseline_settings_24),
                contentDescription = null
            )
        }
        Button(
            onClick = {}, Modifier
                .fillMaxWidth()
                .padding(start = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.baseline_filter_list_24),
                    contentDescription = null
                )
                Text(text = "Поиск", modifier = Modifier.padding(horizontal = 12.dp))
                if (1 == 2) { // Заглушка, потом добавлю функцию поиска и заживём...
                    Surface(color = Color.White, modifier = Modifier.clip(CircleShape)) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        ) {

                        }
                    }
                }
            }
        }
    }
}