package com.example.gestionproductos.ui.formasdepago

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionproductos.data.model.FormaDePago
import com.example.gestionproductos.data.repository.CatalogoRepository
import com.example.gestionproductos.ui.common.EstadoGuardadoCatalogo
import kotlinx.coroutines.launch

class FormaDePagoFormViewModel(private val repository: CatalogoRepository) : ViewModel() {

    private val _formaDePagoCargada = MutableLiveData<FormaDePago?>(null)
    val formaDePagoCargada: LiveData<FormaDePago?> = _formaDePagoCargada

    private val _estadoGuardado = MutableLiveData<EstadoGuardadoCatalogo>(EstadoGuardadoCatalogo.Ninguno)
    val estadoGuardado: LiveData<EstadoGuardadoCatalogo> = _estadoGuardado

    fun cargarFormaDePago(id: Int) {
        viewModelScope.launch {
            repository.obtenerFormaDePago(id).onSuccess { _formaDePagoCargada.value = it }
        }
    }

    fun guardar(formaDePago: FormaDePago) {
        _estadoGuardado.value = EstadoGuardadoCatalogo.Guardando
        viewModelScope.launch {
            repository.guardarFormaDePago(formaDePago)
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
