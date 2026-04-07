package ru.polyarbeiterz.impressionmap.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.polyarbeiterz.impressionmap.R
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.presentation.components.SimpleTopBar
import ru.polyarbeiterz.impressionmap.presentation.model.ImpressionAdditionModel
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ImpressionAdditionComposable(
    navController: NavController,
    impressionId: Int,
    lat: Float = 0f,
    lon: Float = 0f
) {
    ImpressionMapTheme {
        ImpressionAdditionScreen(
            navController,
            impressionId = impressionId,
            lat = lat,
            lon = lon,
            modifier = Modifier
                .padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun ImpressionAdditionScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    lat: Float = 0f,
    lon: Float = 0f,
    impressionId: Int,
    impAdditionModel: ImpressionAdditionModel = hiltViewModel()
) {

    val isNew = impressionId == -1

    val impression = if (!isNew) {
        impAdditionModel.getImpById(impressionId).collectAsState(initial = null).value
    } else {
        null
    }

    if (impression == null && !isNew) {
        CircularProgressIndicator()
        return
    }

    var title by rememberSaveable { mutableStateOf(impression?.title) }
    var description by rememberSaveable { mutableStateOf(impression?.description) }
    var selectedDateTime by rememberSaveable {
        mutableLongStateOf(
            impression?.date ?: System.currentTimeMillis()
        )
    }

//    val mediaList = impAdditionModel.getMediaByImpId(impressionId).collectAsState(initial = null).value

    var checkedSendToServer by remember { mutableStateOf(impression?.onServer ?: false) }

    var showDatePicker by remember { mutableStateOf(false) }

    val formattedDate = selectedDateTime.let { millis ->
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
            .format(formatter)
    } ?: "Выберите дату и время"

    if (showDatePicker) {
        DateTimePickerModal(
            initialDateMillis = selectedDateTime,
            onDateSelected = { millis ->
                selectedDateTime = millis ?: System.currentTimeMillis()
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Column() {
        Box() {
            SimpleTopBar(
                navController = navController,
                headerText = "Добавление".takeIf { isNew } ?: "Редактирование",
                actionButtonText = "Сохранить",
                actionButtonOnClick = {
                    if (isNew) {
                        impAdditionModel.insertImp(
                            ImpressionLocal(
                                latitude = lat,
                                longitude = lon,
                                date = selectedDateTime,
                                title = title ?: "Без названия",
                                description = description ?: "Без описания",
                                onServer = checkedSendToServer,
                            )
                        )
                    } else {
                        impAdditionModel.updateImp(
                            impression!!.copy(
                                id = impressionId,
                                date = selectedDateTime,
                                title = title ?: "Без названия",
                                description = description ?: "Без описания",
                                onServer = checkedSendToServer,
                            )
                        )
                    }
                    navController.popBackStack()
                },
                actionButtonEnabled = !(title.isNullOrBlank() || description.isNullOrBlank()),
                modifier = modifier
                    .align(Alignment.TopCenter)
            )
        }

        Column(
            modifier
        ) {

            OutlinedTextField(
                label = { Text(text = "Имя воспоминания") },
                value = title ?: "",
                singleLine = true,
                isError = title.isNullOrBlank(),
                onValueChange = { title = it },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (title.isNullOrBlank()) {
                        Text("Название должно быть заполнено", color = Color.Black)
                    }
                },
            )

            OutlinedTextField(
                label = { Text(text = "Описание воспоминания") },
                value = description ?: "",
                isError = description.isNullOrBlank(),
                onValueChange = { description = it },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (description.isNullOrBlank()) {
                        Text("Описание должно быть заполнено", color = Color.Black)
                    }
                },
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = { showDatePicker = true },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            ) {
                OutlinedTextField(
                    label = { Text(text = "Дата и время") },
                    value = formattedDate,
                    singleLine = true,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Выбрать дату",
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledContainerColor = Color.Transparent,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
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
//            items(mediaList) { index ->
//                Box(
//                    modifier = Modifier.padding(8.dp)
//                )
//                Image(
//                    painter = painterResource(mediaList[index]),
//                    contentDescription = null,
//                    modifier = Modifier.size(64.dp)
//                )
//            }
            item {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable(
                            enabled = true,
                            onClick = {
//                                TODO("Кнопка должна вызывать функцию добавления файлов")
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerModal(
    initialDateMillis: Long? = null,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )

    val initialHour = initialDateMillis?.let { millis ->
        ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(millis),
            ZoneId.systemDefault()
        ).hour
    } ?: 12

    val initialMinute = initialDateMillis?.let { millis ->
        ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(millis),
            ZoneId.systemDefault()
        ).minute
    } ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    val selectedDateTime =
        remember(datePickerState.selectedDateMillis, timePickerState.hour, timePickerState.minute) {
            datePickerState.selectedDateMillis?.let { date ->
                val zdt = ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(date),
                    ZoneId.systemDefault()
                ).withHour(timePickerState.hour)
                    .withMinute(timePickerState.minute)
                    .withSecond(0)
                    .withNano(0)
                zdt.toInstant().toEpochMilli()
            }
        }

    val formattedDate = selectedDateTime?.let { millis ->
        val formatter =
            DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale.getDefault())
        ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(millis),
            ZoneId.systemDefault()
        ).format(formatter)
    } ?: "Выберите дату и время"

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            FilledTonalButton(onClick = {
                onDateSelected(selectedDateTime)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
        ) {
//            Text(
//                text = formattedDate,
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 8.dp),
//                textAlign = TextAlign.Center
//            )

            // Табы для переключения
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Дата") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Время") }
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Контент табов
                when (selectedTab) {
                    0 -> DatePicker(
                        state = datePickerState,
                        title = { Text("Выберите дату", modifier = Modifier.padding(16.dp)) },
                        headline = { Text("Выбранная дата", modifier = Modifier.padding(16.dp)) },
                        modifier = Modifier.fillMaxHeight()
                    )

                    1 -> TimePicker(state = timePickerState)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TestPreview() {
//    ImpressionAdditionTopBar(navController = rememberNavController(), isNew = false, {})
    DateTimePickerModal(null, {}, {})
}