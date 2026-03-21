package ru.polyarbeiterz.impressionmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.polyarbeiterz.impressionmap.ui.theme.ImpressionMapTheme

class ImpressionAdditionActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImpressionMapTheme {
                Scaffold(topBar = {
                    TopBar()
                }, modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ImpressionAdditionMenu(
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpressionAdditionMenu(modifier: Modifier = Modifier) {
    var checkedSaveLocally by remember { mutableStateOf(true) }
    var checkedSendToServer by remember { mutableStateOf(false) }
    var textFieldName by remember { mutableStateOf("") }
    var textFieldDescription by remember { mutableStateOf("") }

    Column(modifier) {
        Text(text = "Имя воспоминания")
        OutlinedTextField(
            value = textFieldName,
            singleLine = true,
            onValueChange = { textFieldName = it },
            placeholder = { Text(text = "Имя") },
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = "Описание воспоминания")
        OutlinedTextField(
            value = textFieldDescription,
            onValueChange = { textFieldDescription = it },
            placeholder = { Text(text = "Описание") },
            modifier = Modifier.fillMaxWidth()
        )
        //Text(text = "Дата и время")
        //TODO() Добавить DatePicker
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Сохранить на устройстве")
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = checkedSaveLocally, onCheckedChange = {
                checkedSaveLocally = it
            })
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ), title = {
            Text(text = "Добавление воспоминания™")
            //TODO() Вместо текста тут должны быть 3 кнопки - сравни с дизайном в Figma
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ImpressionAdditionMenuPreview() {
    ImpressionMapTheme {
        Scaffold(topBar = {
            TopBar()
        }, modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ImpressionAdditionMenu(
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}