package ru.polyarbeiterz.impressionmap.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ru.polyarbeiterz.impressionmap.R
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.presentation.model.ImpressionAdditionModel
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme

@Composable
fun ImpressionAdditionScreen(
    navController: NavController,
    impressionId: Int,
    lat: Float = 0f,
    lon: Float = 0f
) {
    ImpressionMapTheme {
        Scaffold(
            topBar = { AdditionTopBar() },
            modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ImpressionAdditionMenu(
                    navController,
                    modifier = Modifier.padding(8.dp),
                    impressionId = impressionId,
                    lat = lat,
                    lon = lon
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpressionAdditionMenu(
    navController: NavController,
    modifier: Modifier = Modifier,
    lat: Float = 0f,
    lon: Float = 0f,
    impressionId: Int,
    impAdditionModel: ImpressionAdditionModel = hiltViewModel()
) {

    val isNew = impressionId == -1

    val impression = if (!isNew) {
        impAdditionModel.getImpressionById(impressionId).collectAsState(initial = null).value
    } else {
        null
    }

    if (impression == null && !isNew) {
        CircularProgressIndicator()
        return
    }

    var title by rememberSaveable { mutableStateOf(impression?.title ?: "") }
    var description by rememberSaveable { mutableStateOf(impression?.description ?: "") }

    var checkedSaveLocally by remember { mutableStateOf(true) }
    var checkedSendToServer by remember { mutableStateOf(false) }

    Column(modifier) {
        Text(text = "Имя воспоминания")
        OutlinedTextField(
            value = title,
            singleLine = true,
            onValueChange = { title = it },
            placeholder = { Text(text = "Имя") },
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = "Описание воспоминания")
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text(text = "Описание") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Дата и время")

        }
        //TODO() Добавить DatePicker
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Поделиться на сервере")
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = checkedSendToServer, onCheckedChange = {
                checkedSendToServer = it
            })
        }
        LazyVerticalGrid(
            GridCells.Adaptive(64.dp), horizontalArrangement = Arrangement.Absolute.Center
        ) {
            items(3) { i ->
                Box(
                    modifier = Modifier.padding(8.dp)
                )
                Icon(
                    painter = painterResource(R.drawable.icon),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable(
                            enabled = true,
                            onClick = {
                                TODO("Кнопка должна вызывать функцию добавления файлов")
                            })
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_add_24),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { if (isNew) {
                    impAdditionModel.insertImp(
                        ImpressionLocal(
                            latitude = lat,
                            longitude = lon,
                            title = title,
                            description = description,
                            onServer = checkedSendToServer,
                        )
                    )
            } else {
                impAdditionModel.updateImpression(
                    impression!!.copy(
                        id = impressionId,
                        title = title,
                        description = description,
                        onServer = checkedSendToServer,
                    )
                )
            }
                navController.navigate("map_screen")
            }) {
                Text(text="Сохранить")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdditionTopBar() {
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {Text(text = "Редактирование воспоминания")}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ImpressionAdditionMenuPreview() {
    ImpressionMapTheme {
        Scaffold(
            topBar = { AdditionTopBar() },
            modifier = Modifier.fillMaxSize()) { innerPadding ->
                Column(
                    modifier = Modifier.padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
//                    ImpressionAdditionMenu(
//                        modifier = Modifier.padding(8.dp)
//                    )
                }
            }
    }
}