package com.example.gestionproductos.ui.categorias

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionproductos.data.model.Categoria
import com.example.gestionproductos.data.repository.CatalogoRepository
import com.example.gestionproductos.ui.common.EstadoGuardadoCatalogo
import kotlinx.coroutines.launch

class CategoriaFormViewModel(private val repository: CatalogoRepository) : ViewModel() {

    private val _categoriaCargada = MutableLiveData<Categoria?>(null)
    val categoriaCargada: LiveData<Categoria?> = _categoriaCargada

    private val _estadoGuardado = MutableLiveData<EstadoGuardadoCatalogo>(EstadoGuardadoCatalogo.Ninguno)
    val estadoGuardado: LiveData<EstadoGuardadoCatalogo> = _estadoGuardado

    fun cargarCategoria(id: Int) {
        viewModelScope.launch {
            repository.obtenerCategoria(id).onSuccess { _categoriaCargada.value = it }
        }
    }

    fun guardar(categoria: Categoria) {
        _estadoGuardado.value = EstadoGuardadoCatalogo.Guardando
        viewModelScope.launch {
            repository.guardarCategoria(categoria)
                .onSuccess {
                    _estadoGuardado.value = EstadoGuardadoCatalogo.Exito
                    _estadoGuardado.value = EstadoGuardadoCatalogo.Ninguno
                }
                .onFailure {
                    _estadoGuardado.value = EstadoGuardadoCatalogo.Error(it.message ?: "Error desconocido")
                    _estadoGuardado.value = EstadoGuardadoCatalogo.Ninguno
                }
        }
    }
}
