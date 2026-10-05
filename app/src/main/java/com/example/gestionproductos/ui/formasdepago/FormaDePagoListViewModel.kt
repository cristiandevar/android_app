package com.example.gestionproductos.ui.formasdepago

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionproductos.data.model.FormaDePago
import com.example.gestionproductos.data.repository.CatalogoRepository
import com.example.gestionproductos.ui.common.EstadoEliminado
import kotlinx.coroutines.launch

class FormaDePagoListViewModel(private val repository: CatalogoRepository) : ViewModel() {

    private val _formasDePago = MutableLiveData<List<FormaDePago>>(emptyList())
    val formasDePago: LiveData<List<FormaDePago>> = _formasDePago

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _estadoEliminado = MutableLiveData<EstadoEliminado>(EstadoEliminado.Ninguno)
    val estadoEliminado: LiveData<EstadoEliminado> = _estadoEliminado

    var busqueda: String? = null

    fun cargar() {
        _cargando.value = true
        viewModelScope.launch {
            repository.listarFormasDePago(busqueda)
                .onSuccess {
                    _formasDePago.value = it
                    _error.value = null
                }
                .onFailure {
                    _error.value = it.message
                    // Se limpia enseguida para que un observer nuevo (por
                    // ejemplo al volver con el botón atrás) no vea este
                    // mismo error otra vez sin una acción nueva.
                    _error.value = null
                }
            _cargando.value = false
        }
    }

    fun eliminar(formaDePago: FormaDePago) {
        viewModelScope.launch {
            repository.eliminarFormaDePago(formaDePago.id)
                .onSuccess {
                    _estadoEliminado.value = EstadoEliminado.Exito(formaDePago.id)
                    _estadoEliminado.value = EstadoEliminado.Ninguno
                    cargar()
                }
                .onFailure {
                    _estadoEliminado.value = EstadoEliminado.Error(it.message ?: "Error desconocido")
                    _estadoEliminado.value = EstadoEliminado.Ninguno
                }
        }
    }
}
