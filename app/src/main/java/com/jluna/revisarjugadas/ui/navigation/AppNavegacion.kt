package com.jluna.revisarjugadas.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jluna.revisarjugadas.data.auth.Usuario
import com.jluna.revisarjugadas.data.jugadas.Jugada
import com.jluna.revisarjugadas.data.jugadas.JugadaQuini6
import com.jluna.revisarjugadas.data.jugadas.JugadaQuiniela
import com.jluna.revisarjugadas.data.jugadas.JugadasRepository
import com.jluna.revisarjugadas.data.notificaciones.Notificaciones
import com.jluna.revisarjugadas.ui.jugadas.EditarQuini6Screen
import com.jluna.revisarjugadas.ui.jugadas.EditarQuinielaScreen
import com.jluna.revisarjugadas.ui.jugadas.JugadasViewModel
import com.jluna.revisarjugadas.ui.jugadas.MisJugadasScreen
import com.jluna.revisarjugadas.ui.jugadas.TipoJuego
import com.jluna.revisarjugadas.ui.login.SesionViewModel
import com.jluna.revisarjugadas.ui.resultados.ResultadosScreen
import com.jluna.revisarjugadas.ui.screens.AjustesScreen

/** Pestañas de la barra inferior. */
enum class Pestania(val ruta: String, val titulo: String, val icono: ImageVector) {
    JUGADAS("jugadas", "Mis jugadas", Icons.AutoMirrored.Filled.List),
    RESULTADOS("resultados", "Resultados", Icons.Filled.Star),
    AJUSTES("ajustes", "Ajustes", Icons.Filled.Settings),
}

private const val RUTA_NUEVA_JUGADA = "jugada/nueva/{tipo}"
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

    // Al entrar: aplicar las suscripciones a los avisos y pedir permiso para mostrarlos (Android 13+)
    val context = LocalContext.current
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        Notificaciones.sincronizar(context)
        val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !tienePermiso) {
            pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
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
                    onNueva = { tipo -> navController.navigate("jugada/nueva/${tipo.name}") },
                    onAbrir = { id -> navController.navigate("jugada/$id") },
                )
            }
            val guardar: (Jugada) -> Unit = { jugadasVm.guardar(it); navController.popBackStack() }
            val borrar: (String) -> Unit = { jugadasVm.borrar(it); navController.popBackStack() }
            val volver: () -> Unit = { navController.popBackStack() }

            composable(RUTA_NUEVA_JUGADA) { entrada ->
                when (entrada.arguments?.getString("tipo")) {
                    TipoJuego.QUINIELA.name -> EditarQuinielaScreen(null, guardar, borrar, volver)
                    else -> EditarQuini6Screen(null, guardar, borrar, volver)
                }
            }
            composable(RUTA_EDITAR_JUGADA) { entrada ->
                val jugadas by jugadasVm.jugadas.collectAsStateWithLifecycle()
                val id = entrada.arguments?.getString("id")
                when (val jugada = id?.let { buscado -> jugadas?.find { it.id == buscado } }) {
                    is JugadaQuini6 -> EditarQuini6Screen(jugada, guardar, borrar, volver)
                    is JugadaQuiniela -> EditarQuinielaScreen(jugada, guardar, borrar, volver)
                    // Todavía cargando (o la jugada ya no existe)
                    null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
            }
            composable(Pestania.RESULTADOS.ruta) { ResultadosScreen() }
            composable(Pestania.AJUSTES.ruta) { AjustesScreen(usuario, sesion) }
        }
    }
}
