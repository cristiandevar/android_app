package com.example.gestionproductos.ui.precios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionproductos.data.model.Categoria
import com.example.gestionproductos.data.model.Marca
import com.example.gestionproductos.data.model.Producto
import com.example.gestionproductos.data.model.Proveedor
import com.example.gestionproductos.data.repository.ProductoRepository
import kotlinx.coroutines.launch

sealed class EstadoActualizacion {
    object Ninguno : EstadoActualizacion()
    object Aplicando : EstadoActualizacion()
    data class Exito(val cantidad: Int) : EstadoActualizacion()
    data class Error(val mensaje: String) : EstadoActualizacion()
}

class ActualizacionMasivaViewModel(private val repository: ProductoRepository) : ViewModel() {

    private val _categorias = MutableLiveData<List<Categoria>>(emptyList())
    val categorias: LiveData<List<Categoria>> = _categorias

    private val _marcas = MutableLiveData<List<Marca>>(emptyList())
    val marcas: LiveData<List<Marca>> = _marcas

    private val _proveedores = MutableLiveData<List<Proveedor>>(emptyList())
    val proveedores: LiveData<List<Proveedor>> = _proveedores

    // Resultado de "Buscar productos": la previsualización que el usuario
    // puede recortar deseleccionando ítems antes de tocar "Aplicar".
    private val _productosPreview = MutableLiveData<List<Producto>>(emptyList())
    val productosPreview: LiveData<List<Producto>> = _productosPreview

    private val _buscando = MutableLiveData(false)
    val buscando: LiveData<Boolean> = _buscando

    private val _errorBusqueda = MutableLiveData<String?>(null)
    val errorBusqueda: LiveData<String?> = _errorBusqueda

    private val _estado = MutableLiveData<EstadoActualizacion>(EstadoActualizacion.Ninguno)
    val estado: LiveData<EstadoActualizacion> = _estado

    fun cargarCatalogos() {
        viewModelScope.launch {
            _categorias.value = repository.listarCategorias()
            _marcas.value = repository.listarMarcas()
            _proveedores.value = repository.listarProveedores()
        }
    }

    fun buscar(categoriaId: Int?, marcaId: Int?, proveedorId: Int?) {
        _buscando.value = true
        viewModelScope.launch {
            repository.listarProductos(categoriaId, marcaId, proveedorId)
                .onSuccess {
                    _productosPreview.value = it
                    _errorBusqueda.value = null
                }
                .onFailure {
                    _productosPreview.value = emptyList()
                    _errorBusqueda.value = it.message
                    // Se limpia enseguida para que un observer nuevo (por
                    // ejemplo al volver con el botón atrás) no vea este
                    // mismo error otra vez sin una búsqueda nueva.
                    _errorBusqueda.value = null
                }
            _buscando.value = false
        }
    }

    fun aplicar(
        productosIds: List<Int>,
        campo: String,
        operacion: String,
        valor: String
    ) {
        if (productosIds.isEmpty()) {
            _estado.value = EstadoActualizacion.Error("No hay productos seleccionados")
            _estado.value = EstadoActualizacion.Ninguno
            return
        }
        _estado.value = EstadoActualizacion.Aplicando
        viewModelScope.launch {
            repository.actualizarPreciosMasivo(productosIds, campo, operacion, valor)
                .onSuccess {
                    _estado.value = EstadoActualizacion.Exito(it)
                    _estado.value = EstadoActualizacion.Ninguno
                }
                .onFailure {
                    _estado.value = EstadoActualizacion.Error(it.message ?: "Error desconocido")
                    _estado.value = EstadoActualizacion.Ninguno
                }
        }
    }
}
