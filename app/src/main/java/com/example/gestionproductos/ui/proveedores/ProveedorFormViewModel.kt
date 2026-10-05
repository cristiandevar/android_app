package com.example.gestionproductos.ui.proveedores

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionproductos.data.model.Proveedor
import com.example.gestionproductos.data.repository.CatalogoRepository
import com.example.gestionproductos.ui.common.EstadoGuardadoCatalogo
import kotlinx.coroutines.launch

class ProveedorFormViewModel(private val repository: CatalogoRepository) : ViewModel() {

    private val _proveedorCargado = MutableLiveData<Proveedor?>(null)
    val proveedorCargado: LiveData<Proveedor?> = _proveedorCargado

    private val _estadoGuardado = MutableLiveData<EstadoGuardadoCatalogo>(EstadoGuardadoCatalogo.Ninguno)
    val estadoGuardado: LiveData<EstadoGuardadoCatalogo> = _estadoGuardado

    fun cargarProveedor(id: Int) {
        viewModelScope.launch {
            repository.obtenerProveedor(id).onSuccess { _proveedorCargado.value = it }
        }
    }

    fun guardar(proveedor: Proveedor) {
        _estadoGuardado.value = EstadoGuardadoCatalogo.Guardando
        viewModelScope.launch {
            repository.guardarProveedor(proveedor)
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
