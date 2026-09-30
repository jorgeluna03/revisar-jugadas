package com.jluna.revisarjugadas.ui.jugadas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jluna.revisarjugadas.data.quini6.comoBolilla
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val locale = Locale.forLanguageTag("es-AR")
private val formatoCorto = DateTimeFormatter.ofPattern("EEE dd/MM", locale)
private val formatoLargo = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale)

/** "Mié. 30/09" */
fun LocalDate.formatoCorto(): String = format(formatoCorto).replaceFirstChar { it.titlecase(locale) }

/** "Miércoles 30 de septiembre" */
fun LocalDate.formatoLargo(): String = format(formatoLargo).replaceFirstChar { it.titlecase(locale) }

/** Un número del Quini 6 dibujado como bolilla. */
@Composable
fun Bolilla(
    numero: Int,
    modifier: Modifier = Modifier,
    resaltada: Boolean = true,
    tamanio: Dp = 36.dp,
) {
    val colores = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(tamanio)
            .clip(CircleShape)
            .background(if (resaltada) colores.primary else colores.surface)
            .border(1.dp, if (resaltada) colores.primary else colores.outline, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            numero.comoBolilla(),
            color = if (resaltada) colores.onPrimary else colores.onSurface,
            fontWeight = if (resaltada) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
