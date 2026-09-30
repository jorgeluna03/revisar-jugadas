package com.jluna.revisarjugadas.data.auth

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await

/** Todo lo relacionado con iniciar y cerrar sesión en Firebase Authentication. */
class AuthRepository(private val auth: FirebaseAuth = Firebase.auth) {

    // Vincular Google a un invitado no dispara el listener de Firebase: con esto forzamos una nueva emisión
    private val refrescar = MutableStateFlow(0)

    /** Emite el usuario actual cada vez que cambia (null = sin sesión). */
    val usuarioActual: Flow<Usuario?> = combine(cambiosDeSesion(), refrescar) { usuario, _ ->
        usuario?.aUsuario()
    }

    suspend fun entrarComoInvitado() {
        auth.signInAnonymously().await()
    }

    /**
     * Inicia sesión con el token de Google. Si el usuario estaba como invitado,
     * vincula la cuenta de Google a ese mismo usuario para no perder sus jugadas.
     */
    suspend fun entrarConGoogle(idToken: String) {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        val actual = auth.currentUser
        if (actual != null && actual.isAnonymous) {
            try {
                actual.linkWithCredential(credencial).await()
                refrescar.value++
                return
            } catch (e: FirebaseAuthUserCollisionException) {
                // Esa cuenta de Google ya existía: entramos directamente con ella
            }
        }
        auth.signInWithCredential(credencial).await()
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    private fun cambiosDeSesion(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private fun FirebaseUser.aUsuario(): Usuario {
        // Después de vincular, el nombre y el email pueden estar solo en los datos del proveedor
        val google = providerData.firstOrNull { it.providerId == GoogleAuthProvider.PROVIDER_ID }
        return Usuario(
            uid = uid,
            nombre = displayName?.takeIf { it.isNotBlank() } ?: google?.displayName,
            email = email?.takeIf { it.isNotBlank() } ?: google?.email,
            esInvitado = isAnonymous,
        )
    }
}
