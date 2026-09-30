package com.jluna.revisarjugadas.data.notificaciones

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jluna.revisarjugadas.MainActivity
import com.jluna.revisarjugadas.R

/**
 * Con la app cerrada o en segundo plano, Android muestra el aviso solo.
 * Con la app abierta llega acá y lo mostramos nosotros, para que igual se vea.
 */
class MensajesService : FirebaseMessagingService() {

    override fun onMessageReceived(mensaje: RemoteMessage) {
        val aviso = mensaje.notification ?: return
        val permitido = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!permitido) return

        val abrirApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notificacion = NotificationCompat.Builder(this, Notificaciones.CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setColor(ContextCompat.getColor(this, R.color.ic_launcher_background))
            .setContentTitle(aviso.title)
            .setContentText(aviso.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(aviso.body))
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()
        // Un id por tema: un aviso nuevo del mismo juego reemplaza al anterior
        NotificationManagerCompat.from(this).notify(mensaje.from.hashCode(), notificacion)
    }

    override fun onNewToken(token: String) {
        // Las suscripciones a temas se renuevan solas; no guardamos el token
    }
}
