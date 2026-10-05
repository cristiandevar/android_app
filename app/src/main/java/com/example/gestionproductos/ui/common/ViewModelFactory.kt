package com.example.gestionproductos.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory genérica para no repetir una clase de Factory por cada
 * ViewModel que necesita recibir un repositorio en el constructor.
 * Uso: ViewModelProvider(this, ViewModelFactory { MiViewModel(repo) })[MiViewModel::class.java]
 */
class ViewModelFactory<T : ViewModel>(private val creador: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <M : ViewModel> create(modelClass: Class<M>): M {
        return creador() as M
    }
}
