package ru.polyarbeiterz.impressionmap.presentation.screen

import android.R.attr.bitmap
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.polyarbeiterz.impressionmap.R
import ru.polyarbeiterz.impressionmap.core.utils.compressImageToByteArray
import ru.polyarbeiterz.impressionmap.data.datastore.UserCredentials
import ru.polyarbeiterz.impressionmap.data.datastore.UserProfile
import ru.polyarbeiterz.impressionmap.data.entity.Host
import ru.polyarbeiterz.impressionmap.data.entity.MediaLocal
import ru.polyarbeiterz.impressionmap.presentation.ChoiceDialog
import ru.polyarbeiterz.impressionmap.presentation.components.NonEntityCard
import ru.polyarbeiterz.impressionmap.presentation.components.SimpleTopBar
import ru.polyarbeiterz.impressionmap.presentation.model.SettingsModel
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme

@Composable
fun SettingsComposable(navController: NavController) {
    ImpressionMapTheme {
        SettingsInteractionScreen(
            navController, LocalContext.current
        )
    }
}

@Composable
fun SettingsInteractionScreen(
    navController: NavController,
    context: Context,
    modifier: Modifier = Modifier,
    settingsModel: SettingsModel = hiltViewModel()
) {

    val userProfile by settingsModel.selectedUserProfile.collectAsState(
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

    var modalToShow by remember { mutableIntStateOf(0) }

    when (modalToShow) {
        1 -> ConnectionSettingsDialogue(navController, onDismissRequest = { modalToShow = 0 })
        2 -> ProfileSettingsDialogue(
            context = context, onDismissRequest = { modalToShow = 0 },
            username = username, email = email, profileImage = profileImage
        )

        3 -> AboutAppDialogue(onDismissRequest = { modalToShow = 0 })
    }

    Column(Modifier.background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = modifier
                .fillMaxWidth()
        ) {
            SimpleTopBar(
                navController = navController,
                headerText = "Настройки",
                modifier = modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .padding(horizontal = 8.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (profileImage != null) {
                    val bitmap =
                        BitmapFactory.decodeByteArray(profileImage, 0, profileImage!!.size)
                    Image(
                        painter = BitmapPainter(bitmap.asImageBitmap()),
                        contentDescription = "Фото профиля",
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.icon),
                        contentDescription = "Фото профиля",
                        modifier = Modifier
                            .size(128.dp)
                    )
                }
            }

            Text(
                text = username,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = email,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Thin
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                NonEntityCard(
                    cardName = "Настройки подключения",
                    onClick = { modalToShow = 1 }
                )
                HorizontalDivider()
                NonEntityCard(
                    cardName = "Настройки профиля",
                    onClick = { modalToShow = 2 }
                )
                HorizontalDivider()
                NonEntityCard(
                    cardName = "О приложении",
                    onClick = { modalToShow = 3 }
                )

            }
        }
    }
}

@Composable
fun ConnectionSettingsDialogue(
    navController: NavController,
    onDismissRequest: () -> Unit
) {
    ChoiceDialog(
        navController = navController,
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun ProfileSettingsDialogue(
    context: Context,
    onDismissRequest: () -> Unit,
    settingsModel: SettingsModel = hiltViewModel(),
    profileImage: ByteArray?,
    username: String,
    email: String
) {

    var editUsername by remember { mutableStateOf(username) }
    var editEmail by remember { mutableStateOf(email) }
    var editProfileImage by remember { mutableStateOf(profileImage) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { it ->
                val inputStream = context.contentResolver.openInputStream(it)
                val byteArray = inputStream?.readBytes()
                byteArray?.let {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    editProfileImage = compressImageToByteArray(
                        bitmap,
                        quality = 70,
                        maxWidth = 256,
                        maxHeight = 256
                    )
                }
            }
        }
    )

    Dialog({ onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (editProfileImage != null) {
                        val bitmap =
                            BitmapFactory.decodeByteArray(
                                editProfileImage,
                                0,
                                editProfileImage!!.size
                            )
                        Image(
                            painter = BitmapPainter(bitmap.asImageBitmap()),
                            contentDescription = "Фото профиля",
                            modifier = Modifier
                                .size(128.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.icon),
                            contentDescription = "Фото профиля",
                            modifier = Modifier
                                .size(128.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = editUsername,
                    label = { Text(text = "Имя") },
                    onValueChange = { editUsername = it },
                    isError = editUsername.isBlank(),
                    supportingText = {
                        if (editUsername.isBlank()) {
                            Text("Заполните поле имени", color = Color.Red)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editEmail,
                    label = { Text(text = "Почта") },
                    onValueChange = { editEmail = it },
                    isError = editEmail.isBlank(),
                    supportingText = {
                        if (editEmail.isBlank()) {
                            Text("Заполните поле почты", color = Color.Red)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    enabled = editUsername.isNotBlank() && editEmail.isNotBlank(),
                    onClick = {
                        settingsModel.saveUserProfile(editUsername, editEmail, editProfileImage)
                        onDismissRequest()
                    }
                ) { Text(text = "Сохранить") }
            }
        }
    }
}

@Composable
fun AboutAppDialogue(
    onDismissRequest: () -> Unit
) {

    Dialog({ onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ImpressionMap",
                    lineHeight = 1.5.em,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Приложение ImpressionMap позволяет сохранять впечатления от прогулок и путешествий в виде геометок на карте.\n\n" +
                            "Данные можно хранить как локально, так и на любом доступном сервере.",
                    lineHeight = 1.5.em,
                )
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Column() {
                            Text(text = "Авторы", fontWeight = FontWeight.Bold)
                            Text(
                                text = "- Шаров Матвей Андреевич\n" +
                                        "- Лутчак Андрей Алексеевич\n" +
                                        "- Нестеренко Сергей Андреевич", fontSize = 1.5.em
                            )
                        }
                        Image(
                            painter = painterResource(R.drawable.polytech_logo),
                            contentDescription = "Логотип Политеха",
                        )
                    }
                }
                Text(
                    text = "“Разработка мобильных приложений”\n" +
                            "СПбПУ, 2026", textAlign = TextAlign.Center, lineHeight = 1.5.em
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsInteractionScreen() {
    AboutAppDialogue({})
}