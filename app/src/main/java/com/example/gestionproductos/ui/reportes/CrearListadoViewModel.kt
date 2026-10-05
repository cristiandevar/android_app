package com.example.gestionproductos.ui.reportes

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

/**
 * Se encarga solo de la búsqueda con filtros y de cargar los catálogos
 * para los spinners. La generación del archivo (PDF/Excel) y su
 * descarga/compartida ahora viven en VistaPreviaReporteViewModel: una
 * vez que el usuario confirma acá qué productos y columnas incluir,
 * pasa a la pantalla de vista previa, donde recién se elige el formato.
 */
class CrearListadoViewModel(private val repository: ProductoRepository) : ViewModel() {

    private val _categorias = MutableLiveData<List<Categoria>>(emptyList())
    val categorias: LiveData<List<Categoria>> = _categorias

    private val _marcas = MutableLiveData<List<Marca>>(emptyList())
    val marcas: LiveData<List<Marca>> = _marcas

    private val _proveedores = MutableLiveData<List<Proveedor>>(emptyList())
    val proveedores: LiveData<List<Proveedor>> = _proveedores

    private val _productosPreview = MutableLiveData<List<Producto>>(emptyList())
    val productosPreview: LiveData<List<Producto>> = _productosPreview

    private val _buscando = MutableLiveData(false)
    val buscando: LiveData<Boolean> = _buscando

    private val _errorBusqueda = MutableLiveData<String?>(null)
    val errorBusqueda: LiveData<String?> = _errorBusqueda

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
}
