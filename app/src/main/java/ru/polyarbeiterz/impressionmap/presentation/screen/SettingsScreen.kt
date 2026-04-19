package ru.polyarbeiterz.impressionmap.presentation.screen

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.polyarbeiterz.impressionmap.R
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
    viewModel: SettingsModel = hiltViewModel()
) {

    var modalToShow by remember { mutableIntStateOf(0) }

    when (modalToShow) {
        1 -> ConnectionSettingsDialogue(navController, onDismissRequest = { modalToShow = 0 })
        2 -> AboutAppDialogue(onDismissRequest = { modalToShow = 0 })
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
                Image(
                    painter = painterResource(R.drawable.icon),
                    contentDescription = "Логотип приложения",
                    modifier = Modifier.size(128.dp)
                )
            }

            Text(
                text = "Имя",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Почта",
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
                    cardName = "О приложении",
                    onClick = { modalToShow = 2 }
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