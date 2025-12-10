package org.example.agora

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import org.icepdf.ri.common.SwingController
import org.icepdf.ri.common.SwingViewBuilder

@Composable
actual fun PdfViewer(
    pdfPath: String,
    modifier: Modifier = Modifier
) {
    val controller = remember { SwingController() }

    LaunchedEffect(pdfPath) {
        if (pdfPath.isNotEmpty()) {
            try {
                controller.openDocument(pdfPath)
            } catch (e: Exception) {
                println("Errore nell'apertura del PDF: ${e.message}")
            }
        }
    }

    SwingPanel(
        modifier = modifier.fillMaxSize(),
        factory = {
            val builder = SwingViewBuilder(controller)
            val viewerPanel = builder.buildViewerPanel()
            viewerPanel
        },
        update = {
        }
    )
}