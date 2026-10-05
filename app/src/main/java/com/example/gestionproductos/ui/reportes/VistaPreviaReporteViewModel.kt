package com.example.gestionproductos.ui.reportes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionproductos.data.model.Producto
import com.example.gestionproductos.data.repository.ProductoRepository
import kotlinx.coroutines.launch

/** Qué hacer con el archivo una vez generado: guardarlo en Descargas o
 *  abrir el selector para compartirlo. Se elige recién al tocar el
 *  botón correspondiente en la vista previa, junto con el formato. */
enum class AccionArchivo { DESCARGAR, COMPARTIR }

sealed class EstadoExportacion {
    object Ninguno : EstadoExportacion()
    object Generando : EstadoExportacion()
    data class Exito(val bytes: ByteArray, val formato: String, val accion: AccionArchivo) : EstadoExportacion()
    data class Error(val mensaje: String) : EstadoExportacion()
}

/**
 * Sostiene los productos y columnas que el usuario ya confirmó en
 * "Generar Listado" (CrearListadoFragment se los pasa acá vía cargar()
 * antes de navegar) y genera el archivo (PDF o Excel, recién elegido en
 * esta pantalla al tocar Descargar o Compartir) llamando al mismo
 * endpoint de siempre.
 *
 * Con alcance de Activity (activityViewModels en ambos Fragments) para
 * que los datos sobrevivan la navegación de una pantalla a la otra sin
 * tener que serializar la lista de productos como argumento de
 * navegación (Producto no es Parcelable y no hace falta agregarlo solo
 * para esto).
 */
class VistaPreviaReporteViewModel(private val repository: ProductoRepository) : ViewModel() {

    private val _productos = MutableLiveData<List<Producto>>(emptyList())
    val productos: LiveData<List<Producto>> = _productos

    private val _columnas = MutableLiveData<List<String>>(emptyList())
    val columnas: LiveData<List<String>> = _columnas

    private val _estadoExportacion = MutableLiveData<EstadoExportacion>(EstadoExportacion.Ninguno)
    val estadoExportacion: LiveData<EstadoExportacion> = _estadoExportacion

    /** Llamado por CrearListadoFragment justo antes de navegar acá. */
    fun cargar(productos: List<Producto>, columnas: List<String>) {
        _productos.value = productos
        _columnas.value = columnas
    }

    fun generarYProcesar(formato: String, accion: AccionArchivo) {
        val productosIds = _productos.value.orEmpty().map { it.id }
        val columnasElegidas = _columnas.value.orEmpty()
        if (productosIds.isEmpty() || columnasElegidas.isEmpty()) {
            _estadoExportacion.value = EstadoExportacion.Error("No hay productos o columnas para generar el reporte")
            _estadoExportacion.value = EstadoExportacion.Ninguno
            return
        }
        _estadoExportacion.value = EstadoExportacion.Generando
        viewModelScope.launch {
            repository.generarListado(productosIds, columnasElegidas, formato)
                .onSuccess {
                    _estadoExportacion.value = EstadoExportacion.Exito(it, formato, accion)
                    // Se limpia enseguida para que un observer nuevo (por
                    // ejemplo si el Fragment recrea su vista) no repita la
                    // misma descarga/compartida sin una acción nueva.
                    _estadoExportacion.value = EstadoExportacion.Ninguno
                }
                .onFailure {
                    _estadoExportacion.value = EstadoExportacion.Error(it.message ?: "Error desconocido")
                    _estadoExportacion.value = EstadoExportacion.Ninguno
                }
        }
    }
}
