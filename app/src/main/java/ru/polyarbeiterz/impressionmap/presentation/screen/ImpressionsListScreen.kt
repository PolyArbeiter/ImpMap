package ru.polyarbeiterz.impressionmap.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.presentation.model.ImpressionAdditionModel
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme

@Composable
fun ImpressionListComposable(
    navController: NavController,
    impressionAdditionModel: ImpressionAdditionModel = hiltViewModel()
) {

    ImpressionMapTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            MainBottomBar(
                navController,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 15.dp)
            )
            ImpressionsList()
        }
    }
}

@Composable
fun ImpressionsList(
    impressionAdditionModel: ImpressionAdditionModel = hiltViewModel()
) {
    val allImpressions = remember { mutableStateListOf<ImpressionLocal>() }

    LaunchedEffect(Unit)  {
        impressionAdditionModel.getAllImpressions().forEach { allImpressions.add(it) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(vertical = 30.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Список впечатлений",
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(allImpressions.size) { index ->
            Card(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = allImpressions[index].title ?: "Без названия",
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = allImpressions[index].description ?: "Без названия",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}