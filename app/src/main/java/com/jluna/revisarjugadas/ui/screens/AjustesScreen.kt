package com.jluna.revisarjugadas.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import com.jluna.revisarjugadas.data.notificaciones.Notificaciones
import com.jluna.revisarjugadas.dominio.Avisos
import com.jluna.revisarjugadas.dominio.Jurisdiccion
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jluna.revisarjugadas.data.auth.Usuario
import com.jluna.revisarjugadas.ui.login.SesionViewModel

@Composable
fun AjustesScreen(usuario: Usuario, sesion: SesionViewModel) {
    val context = LocalContext.current
    var confirmarSalida by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Ajustes", style = MaterialTheme.typography.headlineSmall)

        Card(Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Cuenta", style = MaterialTheme.typography.titleMedium)
                if (usuario.esInvitado) {
                    Text("Estás como invitado", modifier = Modifier.padding(top = 4.dp))
                    Text(
                        "Vinculá tu cuenta de Google para no perder tus jugadas si cambiás de teléfono.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Button(
                        onClick = { sesion.entrarConGoogle(context) },
                        enabled = !sesion.procesando,
                        modifier = Modifier.padding(top = 12.dp),
                    ) { Text("Vincular con Google") }
                } else {
                    usuario.nombre?.let { Text(it, modifier = Modifier.padding(top = 4.dp)) }
                    usuario.email?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }

                sesion.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }

                OutlinedButton(
                    onClick = { confirmarSalida = true },
                    enabled = !sesion.procesando,
                    modifier = Modifier.padding(top = 12.dp),
                ) { Text("Cerrar sesión") }
            }
        }

        TarjetaNotificaciones()

        Text(
            "App no oficial. Verificá siempre tu boleta en una agencia.",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 24.dp),
        )
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("Cerrar sesión") },
            text = {
                Text(
                    if (usuario.esInvitado) {
                        "Estás como invitado: si cerrás sesión vas a perder tus jugadas cargadas. ¿Continuar?"
                    } else {
                        "¿Querés cerrar sesión?"
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    sesion.cerrarSesion(context)
                }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun TarjetaNotificaciones() {
    val context = LocalContext.current
    // Copia en memoria de las preferencias, para que los interruptores se actualicen al tocarlos
    val activos = remember {
        mutableStateMapOf<String, Boolean>().apply {
            Notificaciones.temas.forEach { put(it, Notificaciones.activado(context, it)) }
        }
    }
    val opciones = listOf(Avisos.TEMA_QUINI6 to "Quini 6") +
        Jurisdiccion.entries.map { Avisos.temaQuiniela(it) to "Quiniela ${it.nombre}" }

    Card(Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
            Text(
                "Te avisamos cuando se cargan los números de un sorteo. " +
                    "La Quiniela tiene 5 sorteos por día: activá solo las que juegues.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
            )
            opciones.forEach { (tema, nombre) ->
                val activo = activos[tema] == true
                val cambiar = { nuevo: Boolean ->
                    activos[tema] = nuevo
                    Notificaciones.cambiar(context, tema, nuevo)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { cambiar(!activo) }.padding(vertical = 4.dp),
                ) {
                    Text(nombre, modifier = Modifier.weight(1f))
                    Switch(checked = activo, onCheckedChange = cambiar)
                }
            }
        }
    }
}
