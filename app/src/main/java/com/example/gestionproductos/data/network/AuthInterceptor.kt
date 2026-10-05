package com.example.gestionproductos.data.network

import com.example.gestionproductos.data.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Agrega el header "Authorization: Bearer <token>" a cada pedido, siempre
 * que haya una sesión activa. El endpoint de login (/api/token/) no
 * necesita token, pero mandarle un header de más no le molesta a Django.
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val solicitudOriginal = chain.request()
        val token = sessionManager.token()

        val solicitud = if (token != null) {
            solicitudOriginal.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            solicitudOriginal
        }

        return chain.proceed(solicitud)
    }
}
