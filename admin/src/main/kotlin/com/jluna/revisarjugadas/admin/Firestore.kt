package com.jluna.revisarjugadas.admin

import com.github.ajalt.clikt.core.CliktError
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.FieldValue
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.jluna.revisarjugadas.dominio.Avisos
import java.io.File
import java.time.LocalDate

/**
 * Acceso a Firebase con permisos de administrador: Firestore (ignora las reglas de seguridad)
 * y Cloud Messaging para enviar los avisos.
 */
class Firestore(rutaCredenciales: String?) {

    private val app by lazy {
        val archivo = listOfNotNull(
            rutaCredenciales,
            System.getenv("GOOGLE_APPLICATION_CREDENTIALS"),
            "admin/credenciales.json",
        ).map(::File).firstOrNull { it.isFile }
            ?: throw CliktError(
                "No encuentro las credenciales. Descargá la clave de la cuenta de servicio desde Firebase " +
                    "(Configuración del proyecto → Cuentas de servicio) y guardala como admin/credenciales.json",
            )
        val opciones = FirebaseOptions.builder()
            .setCredentials(archivo.inputStream().use(GoogleCredentials::fromStream))
            .build()
        FirebaseApp.initializeApp(opciones)
    }

    private val db by lazy { FirestoreClient.getFirestore(app) }

    private val sorteos get() = db.collection("sorteos")

    fun leer(id: String): Map<String, Any?>? = sorteos.document(id).get().get().data

    fun guardar(id: String, datos: Map<String, Any?>) {
        val conFecha = datos + ("publicado" to FieldValue.serverTimestamp())
        sorteos.document(id).set(conFecha).get()
    }

    fun sorteosDelDia(fecha: LocalDate): List<Map<String, Any?>> =
        sorteos.whereEqualTo("fecha", fecha.toString()).get().get().documents.map { it.data }

    /** Envía el aviso a todos los suscriptos al tema. Devuelve el id del mensaje. */
    fun enviar(aviso: Avisos.Aviso): String {
        val mensaje = Message.builder()
            .setTopic(aviso.tema)
            .setNotification(Notification.builder().setTitle(aviso.titulo).setBody(aviso.texto).build())
            // Prioridad alta para que llegue enseguida aunque el teléfono esté en reposo
            .setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).build())
            .build()
        return FirebaseMessaging.getInstance(app).send(mensaje)
    }
}
