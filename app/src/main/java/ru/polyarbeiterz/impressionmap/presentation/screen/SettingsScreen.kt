package ru.polyarbeiterz.impressionmap.presentation.screen

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ru.polyarbeiterz.impressionmap.R
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
//    viewModel: SettingsModel = hiltViewModel()
) {
    Column() {
        Box(modifier = modifier
            .fillMaxWidth()) {
            SettingsTopBar(
                navController = navController,
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
            Image(
                painter = painterResource(R.drawable.icon),
                contentDescription = "Логотип приложения",
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
            )
            Text(text = "Имя")
            Text(text = "Почта")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ){
                Text("Одна настройка")
                Text("Одна настройка")
                Text("Одна настройка")
                Text("Одна настройка")
                Text("Одна настройка")
                Text("Одна настройка")
            }
        }
    }
}


@Composable
fun SettingsTopBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Text("Назад")
        }

        Text(
            text = "Настройки",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsInteractionScreen() {
    SettingsComposable(rememberNavController())
}