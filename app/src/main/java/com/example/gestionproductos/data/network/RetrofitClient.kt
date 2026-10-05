package com.example.gestionproductos.data.network

import com.example.gestionproductos.BuildConfig
import com.example.gestionproductos.data.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * BuildConfig.BASE_URL viene de app/build.gradle.kts y cambia solo según
 * la variante (debug = tu PC en la red local, release = el servidor real).
 * Nunca se hardcodea acá.
 */
object RetrofitClient {

    @Volatile
    private var instancia: ApiService? = null

    fun obtenerInstancia(sessionManager: SessionManager): ApiService {
        return instancia ?: synchronized(this) {
            instancia ?: crearInstancia(sessionManager).also { instancia = it }
        }
    }

    private fun crearInstancia(sessionManager: SessionManager): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val cliente = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
