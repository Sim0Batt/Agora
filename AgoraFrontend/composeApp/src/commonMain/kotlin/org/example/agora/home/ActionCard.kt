package org.example.agora.home

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    onClick: () -> Unit,
    colorPalette: ColorScheme
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        colors = CardColors(
            colorPalette.secondary,
            colorPalette.primary,
            colorPalette.primary.copy(alpha = 0.3f),
            colorPalette.primary
        ),
        modifier = Modifier.size(width = 350.dp, height = 200.dp),
        onClick = onClick,
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = colorPalette.onSurface,
            fontSize = 35.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 30.dp),
            textAlign = TextAlign.Center,
        )

        Text(
            text = text,
            color = colorPalette.onSurface,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
        )
    }
}