package com.jluna.revisarjugadas.data.auth

/** Datos del usuario logueado que usa la app (independiente de Firebase). */
data class Usuario(
    val uid: String,
    val nombre: String?,
    val email: String?,
    val esInvitado: Boolean,
)
