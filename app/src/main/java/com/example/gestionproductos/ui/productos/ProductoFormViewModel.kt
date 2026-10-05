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

sealed class EstadoGuardado {
    object Ninguno : EstadoGuardado()
    object Guardando : EstadoGuardado()
    object Exito : EstadoGuardado()
    data class Error(val mensaje: String) : EstadoGuardado()
}

class ProductoFormViewModel(private val repository: ProductoRepository) : ViewModel() {

    private val _categorias = MutableLiveData<List<Categoria>>(emptyList())
    val categorias: LiveData<List<Categoria>> = _categorias

    private val _marcas = MutableLiveData<List<Marca>>(emptyList())
    val marcas: LiveData<List<Marca>> = _marcas

    private val _proveedores = MutableLiveData<List<Proveedor>>(emptyList())
    val proveedores: LiveData<List<Proveedor>> = _proveedores

    private val _productoCargado = MutableLiveData<Producto?>(null)
    val productoCargado: LiveData<Producto?> = _productoCargado

    private val _estadoGuardado = MutableLiveData<EstadoGuardado>(EstadoGuardado.Ninguno)
    val estadoGuardado: LiveData<EstadoGuardado> = _estadoGuardado

    fun cargarCatalogos() {
        viewModelScope.launch {
            _categorias.value = repository.listarCategorias()
            _marcas.value = repository.listarMarcas()
            _proveedores.value = repository.listarProveedores()
        }
    }

    fun cargarProducto(id: Int) {
        viewModelScope.launch {
            repository.obtenerProducto(id).onSuccess { _productoCargado.value = it }
        }
    }

    fun guardar(producto: Producto) {
        _estadoGuardado.value = EstadoGuardado.Guardando
        viewModelScope.launch {
            repository.guardarProducto(producto)
                .onSuccess {
                    _estadoGuardado.value = EstadoGuardado.Exito
                    _estadoGuardado.value = EstadoGuardado.Ninguno
                }
                .onFailure {
                    _estadoGuardado.value = EstadoGuardado.Error(it.message ?: "Error desconocido")
                    _estadoGuardado.value = EstadoGuardado.Ninguno
                }
        }
    }
}
