package ru.polyarbeiterz.impressionmap.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar(
    textLeft: String,
    textRight: String,
    onClickLeft: () -> Unit,
    onClickRight: () -> Unit,
    modifier: Modifier
) {
    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .clip(RoundedCornerShape(12.dp))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onClickLeft)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = textLeft,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            VerticalDivider(thickness = 1.dp, modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onClickRight)
            ) {
                Text(
                    text = textRight,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}