package ru.polyarbeiterz.impressionmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImpressionMapTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { MainTopBar() },
                    bottomBar = { MainBottomBar() }) { innerPadding ->
                    Map(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Map(modifier: Modifier = Modifier) {
    Surface(
        color = Color.Black,
        modifier = modifier
    ) { }
}

@Composable
fun MainTopBar() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_settings_24), contentDescription = null
        )
        Button(onClick = {}) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.baseline_filter_list_24),
                    contentDescription = null
                )
                Text(text = "Фильтр", modifier = Modifier.padding(horizontal = 12.dp))
                Surface(color = Color.Blue, modifier = Modifier.clip(CircleShape)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    ) {
                        Text(text = "2", modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
        Icon(
            painter = painterResource(R.drawable.baseline_location_marker_24),
            contentDescription = null
        )
    }
}

@Composable
fun MainBottomBar() {
    Surface(
        color = Color.LightGray,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = "Карта",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            VerticalDivider(thickness = 1.dp, modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = "Список",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ImpressionMapTheme {
//        MainBottomBar()

        ImpressionMapTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { MainTopBar() },
                bottomBar = { MainBottomBar() }) { innerPadding ->
                Map(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}