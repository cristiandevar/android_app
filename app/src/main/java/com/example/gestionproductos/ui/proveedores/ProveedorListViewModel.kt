package com.example.gestionproductos.ui.proveedores

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionproductos.data.model.Proveedor
import com.example.gestionproductos.data.repository.CatalogoRepository
import com.example.gestionproductos.ui.common.EstadoEliminado
import kotlinx.coroutines.launch

class ProveedorListViewModel(private val repository: CatalogoRepository) : ViewModel() {

    private val _proveedores = MutableLiveData<List<Proveedor>>(emptyList())
    val proveedores: LiveData<List<Proveedor>> = _proveedores

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
            repository.listarProveedores(busqueda)
                .onSuccess {
                    _proveedores.value = it
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

    fun eliminar(proveedor: Proveedor) {
        viewModelScope.launch {
            repository.eliminarProveedor(proveedor.id)
                .onSuccess {
                    _estadoEliminado.value = EstadoEliminado.Exito(proveedor.id)
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
