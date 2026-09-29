package com.jluna.revisarjugadas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Verde = Color(0xFF1B5E20)
private val VerdeClaro = Color(0xFF81C784)
private val Dorado = Color(0xFFF9A825)

private val EsquemaClaro = lightColorScheme(
    primary = Verde,
    secondary = Dorado,
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeClaro,
    secondary = Dorado,
)

@Composable
fun RevisarJugadasTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
        content = content,
    )
}
