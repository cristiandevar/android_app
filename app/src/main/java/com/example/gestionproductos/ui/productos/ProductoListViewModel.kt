package com.example.gestionproductos.ui.productos

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

class ProductoListViewModel(private val repository: ProductoRepository) : ViewModel() {

    private val _productos = MutableLiveData<List<Producto>>(emptyList())
    val productos: LiveData<List<Producto>> = _productos

    private val _categorias = MutableLiveData<List<Categoria>>(emptyList())
    val categorias: LiveData<List<Categoria>> = _categorias

    private val _marcas = MutableLiveData<List<Marca>>(emptyList())
    val marcas: LiveData<List<Marca>> = _marcas

    private val _proveedores = MutableLiveData<List<Proveedor>>(emptyList())
    val proveedores: LiveData<List<Proveedor>> = _proveedores

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    var filtroCategoriaId: Int? = null
    var filtroMarcaId: Int? = null
    var filtroProveedorId: Int? = null

    fun cargarCatalogos() {
        viewModelScope.launch {
            _categorias.value = repository.listarCategorias()
            _marcas.value = repository.listarMarcas()
            _proveedores.value = repository.listarProveedores()
        }
    }

    fun cargarProductos() {
        _cargando.value = true
        viewModelScope.launch {
            val resultado = repository.listarProductos(
                categoriaId = filtroCategoriaId,
                marcaId = filtroMarcaId,
                proveedorId = filtroProveedorId
            )
            _cargando.value = false
            resultado.onSuccess {
                _productos.value = it
                _error.value = null
            }.onFailure {
                _error.value = it.message
                // Se limpia enseguida para que un observer nuevo (por
                // ejemplo al volver con el botón atrás) no vea este mismo
                // error otra vez sin que hubo una búsqueda nueva.
                _error.value = null
            }
        }
    }
}
