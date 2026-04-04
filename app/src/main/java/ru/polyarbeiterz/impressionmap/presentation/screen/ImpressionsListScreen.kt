package ru.polyarbeiterz.impressionmap.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme

@Composable
fun ImpressionListComposable() {
    ImpressionMapTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Greeting2(
                name = "Android",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    ImpressionMapTheme {
        Greeting2("Android")
    }
}