package com.jluna.revisarjugadas.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(sesion: SesionViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Revisar Jugadas", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Anotá tus jugadas de Quini 6 y Quiniela y enterate al instante si ganaste.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(Modifier.height(48.dp))

        if (sesion.procesando) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { sesion.entrarConGoogle(context) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Continuar con Google") }

            OutlinedButton(
                onClick = { sesion.entrarComoInvitado() },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("Entrar como invitado") }

            Text(
                "Como invitado tus jugadas quedan solo en este teléfono. " +
                    "Podés vincular tu cuenta de Google después, desde Ajustes.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        sesion.error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        Spacer(Modifier.height(48.dp))
        Text(
            "App no oficial. Verificá siempre tu boleta en una agencia.",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
        )
    }
}
