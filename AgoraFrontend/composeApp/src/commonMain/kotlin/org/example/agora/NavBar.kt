package org.example.agora

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private data class NavItem(
    val route: String,
    val title: String,
)

private val navItems = listOf(
    NavItem(
        route = "home",
        title = "Home",
    ),
    NavItem(
        route = "notes",
        title = "Notes",
    ),
    NavItem(
        route = "latex",
        title = "LaTeX Editor",
    ),
    NavItem(
        route = "converter",
        title = "Converter",
    ),
    NavItem(
        route = "settings",
        title = "Settings",
    ),
    NavItem(
        route = "notescopy",
        title = "Copy Notes",
    ),
)

@Composable
fun NavBar(
    selectedRoute: String,
    onRouteSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    colorSchema: ColorScheme
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = colorSchema.background, // Colore di sfondo
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Spazio tra gli item
        ) {

            // Un titolo/logo per l'app
            Text(
                text = "Agorà",
                style = MaterialTheme.typography.titleLarge,
                color = colorSchema.primary,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Spaziatore
            Spacer(Modifier.height(16.dp))

            // --- 3. Il corpo della Navbar ---
            // Iteriamo sulla nostra lista di item
            navItems.forEach { item ->
                NavigationRow(
                    item = item,
                    isSelected = (item.route == selectedRoute),
                    onClick = { onRouteSelected(item.route) },
                    colorSchema = colorSchema
                )
            }

            // Questo Spacer "spinge" tutto ciò che segue verso il basso
            Spacer(Modifier.weight(1f))

            // Esempio di footer per la navbar
            Text(
                text = "Profilo Utente",
                color = colorSchema.primary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
            )
        }
    }
}

/**
 * Il Composable per la *singola riga* cliccabile della navbar.
 */
@Composable
private fun NavigationRow(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    colorSchema: ColorScheme
) {
    // --- 4. Logica per lo stile "selezionato" ---
    val backgroundColor = if (isSelected) {
        // Colore primario con trasparenza per lo sfondo selezionato
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        Color.Transparent // Sfondo trasparente se non selezionato
    }

    val contentColor = if (isSelected) {
        colorSchema.primary // Colore primario per icona/testo
    } else {
        colorSchema.primary.copy(alpha = 0.7f) // Colore più "spento"
    }

    // --- 5. Il layout della riga (Icona + Testo) ---
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)) // Angoli arrotondati
            .clickable(onClick = onClick)   // Cliccabile
            .background(backgroundColor)    // Sfondo (che cambia)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(Modifier.width(16.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor // Colore (che cambia)
        )
    }
}