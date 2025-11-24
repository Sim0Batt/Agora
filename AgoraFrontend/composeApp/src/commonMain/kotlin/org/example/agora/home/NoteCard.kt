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
import org.example.agora.LocalNavigator

@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    title: String,
    length: Int,
    savedPath: String,
    colorPalette: ColorScheme,
    noteId: Int,
    type: String
) {
    val navigator = LocalNavigator.current
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        colors = CardColors(
            if(type == "University") Color(43, 74, 117) else colorPalette.secondary,
            colorPalette.primary,
            if(type == "University") Color(43, 74, 117) else colorPalette.secondary.copy(alpha = 0.3f),
            colorPalette.primary
        ),
        modifier = Modifier.size(width = 350.dp, height = 200.dp),
        onClick = {
            navigator.navigate("editNote", noteId = noteId)
        },
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
            text = "Length: $length",
            color = colorPalette.onSurface,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
        )

        Text(
            text = savedPath,
            color = colorPalette.onSurface,
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
        )
    }
}