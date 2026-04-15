package ru.polyarbeiterz.impressionmap.presentation.screen

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.polyarbeiterz.impressionmap.R
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.data.entity.MediaType
import ru.polyarbeiterz.impressionmap.presentation.components.SimpleTopBar
import ru.polyarbeiterz.impressionmap.presentation.model.ImpressionAdditionModel
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme
import java.io.File
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
            navController = navController,
            modifier = Modifier
                .padding(horizontal = 8.dp),
            context = LocalContext.current,
            lat = lat,
            lon = lon,
            impressionId = impressionId
        )
    }
}

@Composable
fun ImpressionAdditionScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    context: Context,
    lat: Float = 0f,
    lon: Float = 0f,
    impressionId: Int,
    impAdditionModel: ImpressionAdditionModel = hiltViewModel()
) {

    val isNew = impressionId == -1

    var currentImpressionId by remember { mutableIntStateOf(impressionId) }

    val impression = if (!isNew) {
        impAdditionModel.getImpById(currentImpressionId).collectAsState(initial = null).value
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

    var checkedSendToServer by remember { mutableStateOf(impression?.onServer ?: false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<ByteArray?>(null) }

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

    LaunchedEffect(Unit) {
        if (isNew) {
            val imp =
                ImpressionLocal(
                    latitude = lat,
                    longitude = lon,
                    date = selectedDateTime,
                    title = title ?: "Без названия",
                    description = description ?: "Без описания",
                    onServer = checkedSendToServer,
                )
            currentImpressionId = impAdditionModel.insertImp(imp).toInt()
        }
    }

    var confirmed by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            if (isNew && currentImpressionId != -1 && !confirmed) {
                impAdditionModel.deleteImp(currentImpressionId)
            }
        }
    }


    Column() {
        Box() {
            SimpleTopBar(
                navController = navController,
                headerText = "Добавление".takeIf { isNew } ?: "Редактирование",
                actionButtonText = "Сохранить",
                actionButtonOnClick = {
                    confirmed = true
                    impAdditionModel.updateImp(
                        ImpressionLocal(
                            id = currentImpressionId,
                            latitude = lat,
                            longitude = lon,
                            date = selectedDateTime,
                            title = title ?: "Без названия",
                            description = description ?: "Без описания",
                            onServer = checkedSendToServer,
                        )
                    )
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
        MediaGrid(
            context = context,
            impressionId = currentImpressionId,
            onImageTap = { selectedImage = it })
    }
    if (selectedImage != null) {
        FullScreenImageOverlay(
            imageData = selectedImage!!,
            onDismiss = { selectedImage = null }
        )
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

@Composable
fun MediaGrid(
    modifier: Modifier = Modifier,
    onImageTap: (ByteArray) -> Unit,
    context: Context,
    impressionId: Int,
    impAdditionModel: ImpressionAdditionModel = hiltViewModel()
) {

    var showMediaDialog by remember { mutableStateOf(false) }
    val mediaList = impAdditionModel.getMediaByImpId(impressionId)
        .collectAsState(initial = emptyList()).value

    val cameraUri = remember {
        val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            uris.forEach { uri ->
                impAdditionModel.addMediaToImpression(
                    uri,
                    MediaType.IMAGE,
                    impressionId = impressionId
                )
            }
            showMediaDialog = false
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                impAdditionModel.addMediaToImpression(
                    cameraUri,
                    MediaType.IMAGE,
                    impressionId
                )
            }
            showMediaDialog = false
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                cameraLauncher.launch(cameraUri)
            }
        }
    )

    if (showMediaDialog) {
        AlertDialog(
            onDismissRequest = { showMediaDialog = false },
            title = { Text("Выберите изображение") },
            confirmButton = {
                Button(onClick = { galleryLauncher.launch("image/*") }) {
                    Text("Галерея")
                }
            },
            dismissButton = {
                Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Камера")
                }
            }
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(mediaList.size) { index ->
            val mediaItem = mediaList[index]
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray)
                    .clickable { onImageTap(mediaItem.fileData) }
            ) {
                if (mediaItem.mediaType == MediaType.IMAGE) {
                    Image(
                        bitmap = BitmapFactory.decodeByteArray(
                            mediaItem.fileData,
                            0,
                            mediaItem.fileData.size
                        ).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray)
                    .clickable { showMediaDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_add_24),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun FullScreenImageOverlay(imageData: ByteArray, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = BitmapFactory.decodeByteArray(
                imageData,
                0,
                imageData.size
            ).asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentScale = ContentScale.Fit
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TestPreview() {
//    ImpressionAdditionTopBar(navController = rememberNavController(), isNew = false, {})
    DateTimePickerModal(null, {}, {})
}