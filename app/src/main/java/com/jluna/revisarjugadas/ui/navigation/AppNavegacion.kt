package com.jluna.revisarjugadas.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jluna.revisarjugadas.data.auth.Usuario
import com.jluna.revisarjugadas.ui.login.SesionViewModel
import com.jluna.revisarjugadas.ui.screens.AjustesScreen
import com.jluna.revisarjugadas.ui.screens.MisJugadasScreen
import com.jluna.revisarjugadas.ui.screens.ResultadosScreen

/** Pestañas de la barra inferior. */
enum class Pestania(val ruta: String, val titulo: String, val icono: ImageVector) {
    JUGADAS("jugadas", "Mis jugadas", Icons.AutoMirrored.Filled.List),
    RESULTADOS("resultados", "Resultados", Icons.Filled.Star),
    AJUSTES("ajustes", "Ajustes", Icons.Filled.Settings),
}

@Composable
fun AppNavegacion(usuario: Usuario, sesion: SesionViewModel) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                Pestania.entries.forEach { pestania ->
                    NavigationBarItem(
                        selected = rutaActual == pestania.ruta,
                        onClick = {
                            navController.navigate(pestania.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(pestania.icono, contentDescription = null) },
                        label = { Text(pestania.titulo) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Pestania.JUGADAS.ruta,
            modifier = Modifier.padding(padding),
        ) {
            composable(Pestania.JUGADAS.ruta) { MisJugadasScreen() }
            composable(Pestania.RESULTADOS.ruta) { ResultadosScreen() }
            composable(Pestania.AJUSTES.ruta) { AjustesScreen(usuario, sesion) }
        }
    }
}
