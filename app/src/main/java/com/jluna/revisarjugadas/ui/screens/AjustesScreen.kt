package com.jluna.revisarjugadas.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

    Column(Modifier.fillMaxSize().padding(16.dp)) {
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
