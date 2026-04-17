package com.example.taller2.data

import android.content.Context

object CredencialesManager {

    private const val PREFS_NAME ="auth"
    private const val KEY_CORREO = "correo"
    private const val KEY_CONTRASEÑA = "contraseña"
    private const val KEY_HUELLA = "huella_activa"

    fun guardarCredenciales(context: Context, correo: String, contraseña: String, huella_activa: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CORREO, correo)
            .putString(KEY_CONTRASEÑA, contraseña)
            .putBoolean(KEY_HUELLA,huella_activa)
            .apply()
    }

    // Elimina todas las credenciales guardadas
    fun limpiarCredenciales(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    // Retorna true si hay credenciales guardadas y la huella esta activa
    fun huellaActiva(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HUELLA, false)

    fun obtenerCorreo(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CORREO, null)

    fun obtenerContrasena(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CONTRASEÑA, null)


}


