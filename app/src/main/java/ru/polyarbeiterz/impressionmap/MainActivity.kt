package ru.polyarbeiterz.impressionmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImpressionMapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChoiceButton()
                }
            }
        }
    }
}

@Composable
fun ChoiceButton(modifier: Modifier = Modifier) {
    var showModal by remember { mutableStateOf(false) }

    ChoiceModal(state = showModal)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(text = "Выбрать способ работы с приложением ImpMap")
        Button(
            onClick = { showModal = !showModal }
        ) {
            Text(text = "Выбрать")
        }
    }
}

@Composable
fun ChoiceModal(modifier: Modifier = Modifier, state: Boolean) {
    //TODO() Встроить реальную модальную логику,
    // чтобы на экране появилась карточка, фон затухал и т.д.
    if (state) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxSize()
        ) {
            Text(text = "Сервер")
            Text(text = "Локально")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChoiceButton() {
    ImpressionMapTheme() {
        ChoiceButton()
    }
}