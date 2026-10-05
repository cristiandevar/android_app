package com.example.gestionproductos.ui.categorias

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionproductos.data.model.Categoria
import com.example.gestionproductos.data.repository.CatalogoRepository
import com.example.gestionproductos.ui.common.EstadoEliminado
import kotlinx.coroutines.launch

class CategoriaListViewModel(private val repository: CatalogoRepository) : ViewModel() {

    private val _categorias = MutableLiveData<List<Categoria>>(emptyList())
    val categorias: LiveData<List<Categoria>> = _categorias

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
            repository.listarCategorias(busqueda)
                .onSuccess {
                    _categorias.value = it
                    _error.value = null
                }
                .onFailure {
                    _error.value = it.message
                    // Se vuelve a limpiar enseguida (la notificación al
                    // observer activo ya salió con el mensaje) para que un
                    // observer nuevo -por ejemplo al recrear la vista al
                    // volver con el botón atrás- no reciba este mismo error
                    // de nuevo sin que hubo una acción nueva.
                    _error.value = null
                }
            _cargando.value = false
        }
    }

    fun eliminar(categoria: Categoria) {
        viewModelScope.launch {
            repository.eliminarCategoria(categoria.id)
                .onSuccess {
                    _estadoEliminado.value = EstadoEliminado.Exito(categoria.id)
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
