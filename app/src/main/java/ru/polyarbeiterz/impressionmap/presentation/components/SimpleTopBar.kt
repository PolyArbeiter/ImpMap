package ru.polyarbeiterz.impressionmap.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SimpleTopBar(
    navController: NavController,
    actionButtonText: String,
    actionButtonOnClick: () -> Unit,
    headerText: String,
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
            text = headerText,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
        )
        TextButton(
            onClick = {
                actionButtonOnClick()
            },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text(actionButtonText)
        }
    }
}