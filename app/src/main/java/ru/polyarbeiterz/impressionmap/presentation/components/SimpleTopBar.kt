package ru.polyarbeiterz.impressionmap.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SimpleTopBar(
    navController: NavController,
    headerText: String,
    modifier: Modifier = Modifier,
    actionButtonText: String = "",
    actionButtonOnClick: () -> Unit = {},
    actionButtonEnabled: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Назад")
        }
        Text(
            text = headerText,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        TextButton(
            onClick = { actionButtonOnClick() },
            enabled = actionButtonEnabled,
            modifier = Modifier.align(Alignment.TopEnd),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(actionButtonText)
        }
    }
}