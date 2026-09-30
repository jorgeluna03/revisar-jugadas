package com.jluna.revisarjugadas.ui.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jluna.revisarjugadas.data.auth.AuthRepository
import com.jluna.revisarjugadas.data.auth.GoogleSignIn
import com.jluna.revisarjugadas.data.auth.Usuario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data class Activa(val usuario: Usuario) : EstadoSesion
}

/** Estado de la sesión, compartido por toda la app. */
class SesionViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    val estado: StateFlow<EstadoSesion> = repo.usuarioActual
        .map { if (it == null) EstadoSesion.SinSesion else EstadoSesion.Activa(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoSesion.Cargando)

    var procesando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun entrarComoInvitado() = ejecutar { repo.entrarComoInvitado() }

    /** También sirve para vincular Google a un usuario invitado. */
    fun entrarConGoogle(context: Context) = ejecutar {
        repo.entrarConGoogle(GoogleSignIn.obtenerIdToken(context))
    }

    fun cerrarSesion(context: Context) = ejecutar {
        repo.cerrarSesion()
        GoogleSignIn.limpiar(context)
    }

    private fun ejecutar(accion: suspend () -> Unit) {
        viewModelScope.launch {
            procesando = true
            error = null
            try {
                accion()
            } catch (e: GetCredentialCancellationException) {
                // Llega tanto si el usuario cerró el selector como si Google falló por dentro
                // (por ejemplo al agregar una cuenta nueva en el medio), así que avisamos
                error = "No se completó el inicio de sesión con Google. Probá de nuevo."
            } catch (e: Exception) {
                error = e.message ?: "Ocurrió un error inesperado"
            } finally {
                procesando = false
            }
        }
    }
}
