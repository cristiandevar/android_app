package com.example.gestionproductos.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Guarda el token JWT y el rol del usuario logueado en un almacenamiento
 * cifrado del dispositivo (no en SharedPreferences comunes), para que si
 * alguien accede al almacenamiento del celular no encuentre el token en
 * texto plano.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            "sesion_segura",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun guardarSesion(access: String, refresh: String, username: String, rol: String) {
        prefs.edit()
            .putString(KEY_ACCESS, access)
            .putString(KEY_REFRESH, refresh)
            .putString(KEY_USERNAME, username)
            .putString(KEY_ROL, rol)
            .apply()
    }

    fun token(): String? = prefs.getString(KEY_ACCESS, null)
    fun refreshToken(): String? = prefs.getString(KEY_REFRESH, null)
    fun username(): String? = prefs.getString(KEY_USERNAME, null)
    fun rol(): String? = prefs.getString(KEY_ROL, null)
    fun esAdministrador(): Boolean = rol() == "Administrador"
    fun haySesionActiva(): Boolean = token() != null

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROL = "rol"
    }
}
