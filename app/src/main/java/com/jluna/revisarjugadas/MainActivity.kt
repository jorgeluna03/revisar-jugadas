package com.jluna.revisarjugadas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jluna.revisarjugadas.ui.login.EstadoSesion
import com.jluna.revisarjugadas.ui.login.LoginScreen
import com.jluna.revisarjugadas.ui.login.SesionViewModel
import com.jluna.revisarjugadas.ui.navigation.AppNavegacion
import com.jluna.revisarjugadas.ui.theme.RevisarJugadasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RevisarJugadasTheme {
                Surface(Modifier.fillMaxSize()) {
                    val sesion: SesionViewModel = viewModel()
                    val estado by sesion.estado.collectAsStateWithLifecycle()

                    when (val e = estado) {
                        EstadoSesion.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        EstadoSesion.SinSesion -> LoginScreen(sesion)
                        is EstadoSesion.Activa -> AppNavegacion(e.usuario, sesion)
                    }
                }
            }
        }
    }
}
