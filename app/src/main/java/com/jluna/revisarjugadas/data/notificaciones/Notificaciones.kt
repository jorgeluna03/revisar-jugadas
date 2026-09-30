package com.jluna.revisarjugadas.data.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.edit
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.jluna.revisarjugadas.dominio.Avisos
import com.jluna.revisarjugadas.dominio.Jurisdiccion

/**
 * Avisos de resultados por Firebase Cloud Messaging.
 * Cada juego/jurisdicción es un "tema" ([Avisos]); la herramienta admin envía el aviso al tema
 * al cargar un resultado, y la app se suscribe a los temas que el usuario tiene activados.
 */
object Notificaciones {
    const val CANAL = "resultados"

    val temas: List<String> = listOf(Avisos.TEMA_QUINI6) + Jurisdiccion.entries.map(Avisos::temaQuiniela)

    /** Por defecto solo el Quini 6: la Quiniela tiene 5 sorteos por día y serían demasiados avisos. */
    fun activado(context: Context, tema: String): Boolean =
        preferencias(context).getBoolean(tema, tema == Avisos.TEMA_QUINI6)

    fun cambiar(context: Context, tema: String, activo: Boolean) {
        preferencias(context).edit { putBoolean(tema, activo) }
        aplicar(tema, activo)
    }

    /** Vuelve a aplicar las suscripciones guardadas (por si se reinstaló la app o cambió el token). */
    fun sincronizar(context: Context) {
        temas.forEach { aplicar(it, activado(context, it)) }
    }

    fun crearCanal(context: Context) {
        val canal = NotificationChannel(CANAL, "Resultados de sorteos", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Aviso cuando se cargan los números de un sorteo"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    private fun aplicar(tema: String, activo: Boolean) {
        if (activo) Firebase.messaging.subscribeToTopic(tema) else Firebase.messaging.unsubscribeFromTopic(tema)
    }

    private fun preferencias(context: Context) = context.getSharedPreferences("notificaciones", Context.MODE_PRIVATE)
}
