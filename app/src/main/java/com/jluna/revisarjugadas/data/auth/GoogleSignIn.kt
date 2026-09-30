package com.jluna.revisarjugadas.data.auth

import android.annotation.SuppressLint
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/** Muestra el selector de cuentas de Google (Credential Manager) y devuelve el token. */
object GoogleSignIn {

    /** [context] tiene que ser la Activity, porque se muestra un diálogo. */
    suspend fun obtenerIdToken(context: Context): String {
        val opcion = GetSignInWithGoogleOption.Builder(webClientId(context)).build()
        val pedido = GetCredentialRequest.Builder().addCredentialOption(opcion).build()
        val credencial = CredentialManager.create(context).getCredential(context, pedido).credential

        if (credencial is CustomCredential &&
            credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credencial.data).idToken
        }
        error("Tipo de credencial inesperado: ${credencial.type}")
    }

    /** Olvida la cuenta elegida, así la próxima vez se puede elegir otra. */
    suspend fun limpiar(context: Context) {
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
    }

    /**
     * El plugin de Google Services genera el string `default_web_client_id` a partir de
     * google-services.json, pero solo si Google está habilitado en Firebase Authentication.
     * Se busca por nombre para poder dar un mensaje claro si falta.
     */
    @SuppressLint("DiscouragedApi")
    private fun webClientId(context: Context): String {
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        check(id != 0) {
            "Falta configurar Google en Firebase: habilitalo en Authentication y volvé a descargar google-services.json"
        }
        return context.getString(id)
    }
}
