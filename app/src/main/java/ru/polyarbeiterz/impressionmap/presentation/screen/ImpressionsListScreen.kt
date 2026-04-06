package ru.polyarbeiterz.impressionmap.presentation.screen

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.presentation.ChoiceCard
import ru.polyarbeiterz.impressionmap.presentation.model.ImpressionAdditionModel
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

    Box(modifier = modifier.fillMaxSize()) {
        Column() {
            ListTopBar(
                navController,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .padding(horizontal = 8.dp)
                    .statusBarsPadding()
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
            .navigationBarsPadding()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(
            impressionsList.size,
            key = { index ->
                "${impressionsList[index].latitude}:${impressionsList[index].longitude}:${impressionsList[index].date}"
            }) { index ->
                val el = impressionsList.elementAt(index)
                ChoiceCard(
                    cardName = el.title ?: "Без названия",
                    cardDescription = el.description ?: "Без названия",
                    onClick = { navController.navigate("impression_addition/${el.latitude}/${el.longitude}") },
                    chosen = false,
                    modifier = Modifier.fillMaxWidth()
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
    ) {
        Button( onClick = {} ) {
            Icon(
                painter = painterResource(R.drawable.baseline_settings_24),
                contentDescription = null
            )
        }
        Button(onClick = {}, Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.baseline_filter_list_24),
                    contentDescription = null
                )
                Text(text = "Поиск", modifier = Modifier.padding(horizontal = 12.dp))
                Surface(color = Color.White, modifier = Modifier.clip(CircleShape)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    ) {
                        val i = 1;
                        if (i > 0) {
                            Text(text = i.toString(), modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            }
        }
    }
}