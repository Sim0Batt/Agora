package org.example.agora.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PdfViewer(
    pdfPath: String,
    modifier: Modifier = Modifier
){
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("La visualizzazione PDF non è supportata su Android.")
    }
}