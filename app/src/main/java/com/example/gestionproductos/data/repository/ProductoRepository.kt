package com.example.gestionproductos.data.repository

import com.example.gestionproductos.data.model.*
import com.example.gestionproductos.data.network.ApiService
import com.google.gson.JsonParser
import retrofit2.Response

/**
 * Encapsula las llamadas de red relacionadas a productos y sus catálogos
 * (categoría/marca/proveedor), para que los ViewModel no hablen
 * directamente con Retrofit.
 */
class ProductoRepository(private val api: ApiService) {

    suspend fun listarCategorias(): List<Categoria> =
        api.listarCategorias().body().orEmpty()

    suspend fun listarMarcas(): List<Marca> =
        api.listarMarcas().body().orEmpty()

    suspend fun listarProveedores(): List<Proveedor> =
        api.listarProveedores().body().orEmpty()

    suspend fun listarProductos(
        categoriaId: Int? = null,
        marcaId: Int? = null,
        proveedorId: Int? = null
    ): Result<List<Producto>> {
        return try {
            val respuesta = api.listarProductos(categoria = categoriaId, marca = marcaId, proveedor = proveedorId)
            if (respuesta.isSuccessful) {
                Result.success(respuesta.body().orEmpty())
            } else {
                Result.failure(Exception(extraerDetalle(respuesta)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerProducto(id: Int): Result<Producto> {
        return try {
            val respuesta = api.obtenerProducto(id)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                Result.success(respuesta.body()!!)
            } else {
                Result.failure(Exception(extraerDetalle(respuesta)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun guardarProducto(producto: Producto): Result<Producto> {
        return try {
            val respuesta = if (producto.id == 0) {
                api.crearProducto(producto)
            } else {
                api.actualizarProducto(producto.id, producto)
            }
            if (respuesta.isSuccessful && respuesta.body() != null) {
                Result.success(respuesta.body()!!)
            } else {
                // Antes acá solo se mostraba "Error del servidor: 400" sin
                // leer el cuerpo de la respuesta, así que un 400 por
                // código de barra repetido (unique=True en el backend) se
                // veía como un error genérico. extraerDetalle() lee el
                // mensaje real que manda el serializer (mismo mecanismo
                // que ya usa CatalogoRepository).
                Result.failure(Exception(extraerDetalle(respuesta)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarPreciosMasivo(
        productosIds: List<Int>,
        campo: String,
        operacion: String,
        valor: String
    ): Result<Int> {
        return try {
            val respuesta = api.actualizarPreciosMasivo(
                ActualizacionMasivaRequest(productosIds, campo, operacion, valor)
            )
            if (respuesta.isSuccessful && respuesta.body() != null) {
                Result.success(respuesta.body()!!.actualizados)
            } else {
                Result.failure(Exception(extraerDetalle(respuesta)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generarListado(
        productosIds: List<Int>,
        columnas: List<String>,
        formato: String
    ): Result<ByteArray> {
        return try {
            val respuesta = api.generarListado(GenerarListadoRequest(productosIds, columnas, formato))
            if (respuesta.isSuccessful && respuesta.body() != null) {
                Result.success(respuesta.body()!!.bytes())
            } else {
                Result.failure(Exception(extraerDetalle(respuesta)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Helper común (mismo mecanismo que CatalogoRepository) ---

    /**
     * Etiquetas legibles para los campos que puede devolver un error de
     * validación (DRF los nombra tal cual el campo del serializer).
     */
    private val etiquetasDeCampo = mapOf(
        "nombre" to "Nombre",
        "descripcion" to "Descripción",
        "categoria" to "Categoría",
        "marca" to "Marca",
        "proveedor" to "Proveedor",
        "precio_unitario" to "Precio unitario",
        "porcentaje_ganancia" to "Porcentaje de ganancia",
        "stock" to "Cantidad",
        "codigo_barra" to "Código de barra"
    )

    /**
     * El backend manda los errores de dos formas distintas:
     * - Errores de negocio como JSON {"detalle": "..."}.
     * - Errores de validación del serializer (por ejemplo, un código de
     *   barra duplicado al crear o editar un producto) como JSON
     *   {"campo": ["mensaje", ...]}, con un campo por cada error.
     * Si no matchea ninguna de las dos formas, se cae a un mensaje
     * genérico con el código HTTP.
     */
    private fun <T> extraerDetalle(respuesta: Response<T>): String {
        return try {
            val texto = respuesta.errorBody()?.string()
            if (texto.isNullOrBlank()) return "Error del servidor: ${respuesta.code()}"

            val json = JsonParser.parseString(texto).asJsonObject
            val detalle = json.get("detalle")?.takeIf { it.isJsonPrimitive }?.asString
            if (detalle != null) return detalle

            val mensajesPorCampo = json.entrySet().mapNotNull { (campo, valor) ->
                val mensaje = when {
                    valor.isJsonArray -> valor.asJsonArray.joinToString(" ") { it.asString }
                    valor.isJsonPrimitive -> valor.asString
                    else -> null
                }
                mensaje?.let { "${etiquetasDeCampo[campo] ?: campo}: $it" }
            }
            mensajesPorCampo.takeIf { it.isNotEmpty() }?.joinToString("\n")
                ?: "Error del servidor: ${respuesta.code()}"
        } catch (e: Exception) {
            "Error del servidor: ${respuesta.code()}"
        }
    }
}
