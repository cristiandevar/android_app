package com.example.gestionproductos.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gestionproductos.data.SessionManager
import com.example.gestionproductos.data.network.RetrofitClient
import com.example.gestionproductos.data.repository.AuthRepository
import com.example.gestionproductos.data.repository.ResultadoLogin
import com.example.gestionproductos.databinding.ActivityLoginBinding
import com.example.gestionproductos.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(applicationContext)
        val api = RetrofitClient.obtenerInstancia(sessionManager)
        authRepository = AuthRepository(api, sessionManager)

        // Si ya había una sesión guardada (token de una vez anterior), se
        // salta directo a la pantalla principal.
        if (sessionManager.haySesionActiva()) {
            irAPantallaPrincipal()
            return
        }

        binding.botonIngresar.setOnClickListener { intentarLogin() }
    }

    private fun intentarLogin() {
        val usuario = binding.editUsuario.text.toString().trim()
        val clave = binding.editClave.text.toString()

        if (usuario.isEmpty() || clave.isEmpty()) {
            mostrarError("Completá usuario y contraseña")
            return
        }

        mostrarCargando(true)
        lifecycleScope.launch {
            when (val resultado = authRepository.login(usuario, clave)) {
                is ResultadoLogin.Ok -> irAPantallaPrincipal()
                is ResultadoLogin.Error -> {
                    mostrarCargando(false)
                    mostrarError(resultado.mensaje)
                }
            }
        }
    }

    private fun irAPantallaPrincipal() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun mostrarError(mensaje: String) {
        binding.textError.text = mensaje
        binding.textError.visibility = View.VISIBLE
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progressLogin.visibility = if (cargando) View.VISIBLE else View.GONE
        binding.botonIngresar.isEnabled = !cargando
    }
}
