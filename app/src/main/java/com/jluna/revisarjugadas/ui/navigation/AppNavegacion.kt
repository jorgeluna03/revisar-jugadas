package com.jluna.revisarjugadas.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jluna.revisarjugadas.data.auth.Usuario
import com.jluna.revisarjugadas.data.quini6.JugadasRepository
import com.jluna.revisarjugadas.ui.jugadas.EditarJugadaScreen
import com.jluna.revisarjugadas.ui.jugadas.JugadasViewModel
import com.jluna.revisarjugadas.ui.jugadas.MisJugadasScreen
import com.jluna.revisarjugadas.ui.login.SesionViewModel
import com.jluna.revisarjugadas.ui.screens.AjustesScreen
import com.jluna.revisarjugadas.ui.screens.ResultadosScreen

/** Pestañas de la barra inferior. */
enum class Pestania(val ruta: String, val titulo: String, val icono: ImageVector) {
    JUGADAS("jugadas", "Mis jugadas", Icons.AutoMirrored.Filled.List),
    RESULTADOS("resultados", "Resultados", Icons.Filled.Star),
    AJUSTES("ajustes", "Ajustes", Icons.Filled.Settings),
}

private const val RUTA_NUEVA_JUGADA = "jugada/nueva"
private const val RUTA_EDITAR_JUGADA = "jugada/{id}"

@Composable
fun AppNavegacion(usuario: Usuario, sesion: SesionViewModel) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    // Uno por usuario: si cambia la sesión se crea otro con las jugadas del nuevo usuario
    val jugadasVm = viewModel(key = "jugadas-${usuario.uid}") {
        JugadasViewModel(JugadasRepository(usuario.uid))
    }

    Scaffold(
        bottomBar = {
            // La barra solo se muestra en las pestañas principales, no al cargar una jugada
            if (Pestania.entries.any { it.ruta == rutaActual }) {
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
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Pestania.JUGADAS.ruta,
            modifier = Modifier.padding(padding),
        ) {
            composable(Pestania.JUGADAS.ruta) {
                MisJugadasScreen(
                    vm = jugadasVm,
                    onNueva = { navController.navigate(RUTA_NUEVA_JUGADA) },
                    onAbrir = { id -> navController.navigate("jugada/$id") },
                )
            }
            composable(RUTA_NUEVA_JUGADA) {
                EditarJugadaScreen(
                    inicial = null,
                    onGuardar = { jugadasVm.guardar(it); navController.popBackStack() },
                    onBorrar = {},
                    onVolver = { navController.popBackStack() },
                )
            }
            composable(RUTA_EDITAR_JUGADA) { entrada ->
                val jugadas by jugadasVm.jugadas.collectAsStateWithLifecycle()
                val id = entrada.arguments?.getString("id")
                val jugada = id?.let { buscado -> jugadas?.find { it.id == buscado } }
                if (jugada == null) {
                    // Todavía cargando (o la jugada ya no existe)
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    EditarJugadaScreen(
                        inicial = jugada,
                        onGuardar = { jugadasVm.guardar(it); navController.popBackStack() },
                        onBorrar = { jugadasVm.borrar(it); navController.popBackStack() },
                        onVolver = { navController.popBackStack() },
                    )
                }
            }
            composable(Pestania.RESULTADOS.ruta) { ResultadosScreen() }
            composable(Pestania.AJUSTES.ruta) { AjustesScreen(usuario, sesion) }
        }
    }
}
