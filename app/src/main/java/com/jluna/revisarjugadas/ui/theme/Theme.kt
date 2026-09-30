package com.jluna.revisarjugadas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Verde = Color(0xFF1B5E20)
private val VerdeClaro = Color(0xFF81C784)
private val VerdeMuyClaro = Color(0xFFC8E6C9)
private val VerdeOscuro = Color(0xFF0B3D10)
private val Dorado = Color(0xFFF9A825)
private val DoradoClaro = Color(0xFFFFECB3)
private val DoradoOscuro = Color(0xFF5C4100)

// Se definen también los "container" y las superficies: si no, Material usa sus violetas por defecto
private val EsquemaClaro = lightColorScheme(
    primary = Verde,
    onPrimary = Color.White,
    primaryContainer = VerdeMuyClaro,
    onPrimaryContainer = VerdeOscuro,
    secondary = Color(0xFF8D6E00),
    onSecondary = Color.White,
    secondaryContainer = DoradoClaro,
    onSecondaryContainer = DoradoOscuro,
    background = Color(0xFFF8FAF5),
    surface = Color(0xFFF8FAF5),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF2F5EE),
    surfaceContainer = Color(0xFFECF0E8),
    surfaceContainerHigh = Color(0xFFE6EBE2),
    surfaceContainerHighest = Color(0xFFE0E6DC),
    surfaceVariant = Color(0xFFDDE5D9),
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeClaro,
    onPrimary = VerdeOscuro,
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = VerdeMuyClaro,
    secondary = Dorado,
    onSecondary = DoradoOscuro,
    secondaryContainer = DoradoOscuro,
    onSecondaryContainer = DoradoClaro,
    background = Color(0xFF111411),
    surface = Color(0xFF111411),
    surfaceContainerLowest = Color(0xFF0C0F0C),
    surfaceContainerLow = Color(0xFF191D19),
    surfaceContainer = Color(0xFF1D211D),
    surfaceContainerHigh = Color(0xFF272B27),
    surfaceContainerHighest = Color(0xFF323632),
    surfaceVariant = Color(0xFF414941),
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
