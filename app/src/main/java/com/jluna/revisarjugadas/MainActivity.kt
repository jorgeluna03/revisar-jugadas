package com.jluna.revisarjugadas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jluna.revisarjugadas.ui.navigation.AppNavegacion
import com.jluna.revisarjugadas.ui.theme.RevisarJugadasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RevisarJugadasTheme {
                AppNavegacion()
            }
        }
    }
}
