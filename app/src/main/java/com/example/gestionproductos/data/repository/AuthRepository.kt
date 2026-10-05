package com.example.gestionproductos.data.repository

import com.example.gestionproductos.data.SessionManager
import com.example.gestionproductos.data.model.LoginRequest
import com.example.gestionproductos.data.network.ApiService

sealed class ResultadoLogin {
    object Ok : ResultadoLogin()
    data class Error(val mensaje: String) : ResultadoLogin()
}

class AuthRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun login(usuario: String, clave: String): ResultadoLogin {
        return try {
            val respuestaToken = api.login(LoginRequest(usuario, clave))
            if (!respuestaToken.isSuccessful || respuestaToken.body() == null) {
                return ResultadoLogin.Error("Usuario o contraseña incorrectos")
            }
            val tokens = respuestaToken.body()!!

            // Se guarda el token temporalmente para que el interceptor lo
            // use ya en el siguiente pedido (/api/me/).
            sessionManager.guardarSesion(tokens.access, tokens.refresh, usuario, rol = "")

            val respuestaMe = api.usuarioActual()
            if (!respuestaMe.isSuccessful || respuestaMe.body() == null) {
                return ResultadoLogin.Error("No se pudo obtener el usuario logueado")
            }
            val me = respuestaMe.body()!!
            sessionManager.guardarSesion(tokens.access, tokens.refresh, me.username, me.rol)

            ResultadoLogin.Ok
        } catch (e: Exception) {
            ResultadoLogin.Error("No se pudo conectar con el servidor: ${e.message}")
        }
    }

    fun cerrarSesion() = sessionManager.cerrarSesion()
}
