package com.example.gestionproductos.ui.marcas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionproductos.data.model.Marca
import com.example.gestionproductos.data.repository.CatalogoRepository
import com.example.gestionproductos.ui.common.EstadoGuardadoCatalogo
import kotlinx.coroutines.launch

class MarcaFormViewModel(private val repository: CatalogoRepository) : ViewModel() {

    private val _marcaCargada = MutableLiveData<Marca?>(null)
    val marcaCargada: LiveData<Marca?> = _marcaCargada

    private val _estadoGuardado = MutableLiveData<EstadoGuardadoCatalogo>(EstadoGuardadoCatalogo.Ninguno)
    val estadoGuardado: LiveData<EstadoGuardadoCatalogo> = _estadoGuardado

    fun cargarMarca(id: Int) {
        viewModelScope.launch {
            repository.obtenerMarca(id).onSuccess { _marcaCargada.value = it }
        }
    }

    fun guardar(marca: Marca) {
        _estadoGuardado.value = EstadoGuardadoCatalogo.Guardando
        viewModelScope.launch {
            repository.guardarMarca(marca)
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
