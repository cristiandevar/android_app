package com.example.gestionproductos.data.repository

import com.example.gestionproductos.data.model.Categoria
import com.example.gestionproductos.data.model.FormaDePago
import com.example.gestionproductos.data.model.Marca
import com.example.gestionproductos.data.model.Proveedor
import com.example.gestionproductos.data.network.ApiService
import com.google.gson.JsonParser
import retrofit2.Response

/**
 * CRUD de los catálogos maestros (Categoría, Marca, Proveedor, Forma de
 * pago). Las pantallas que lo usan están restringidas a Administrador
 * (MainActivity oculta esos ítems del menú para un Operador); el backend
 * además devuelve 403 si alguien llegara igual con otro rol.
 */
class CatalogoRepository(private val api: ApiService) {

    // --- Categorías ---
    suspend fun listarCategorias(busqueda: String? = null): Result<List<Categoria>> =
        envolver { api.listarCategorias(busqueda) }

    suspend fun obtenerCategoria(id: Int): Result<Categoria> =
        envolver { api.obtenerCategoria(id) }

    suspend fun guardarCategoria(categoria: Categoria): Result<Categoria> = envolver {
        if (categoria.id == 0) api.crearCategoria(categoria)
        else api.actualizarCategoria(categoria.id, categoria)
    }

    suspend fun eliminarCategoria(id: Int): Result<Unit> = envolverSinCuerpo { api.eliminarCategoria(id) }

    // --- Marcas ---
    suspend fun listarMarcas(busqueda: String? = null): Result<List<Marca>> =
        envolver { api.listarMarcas(busqueda) }

    suspend fun obtenerMarca(id: Int): Result<Marca> =
        envolver { api.obtenerMarca(id) }

    suspend fun guardarMarca(marca: Marca): Result<Marca> = envolver {
        if (marca.id == 0) api.crearMarca(marca)
        else api.actualizarMarca(marca.id, marca)
    }

    suspend fun eliminarMarca(id: Int): Result<Unit> = envolverSinCuerpo { api.eliminarMarca(id) }

    // --- Proveedores ---
    suspend fun listarProveedores(busqueda: String? = null): Result<List<Proveedor>> =
        envolver { api.listarProveedores(busqueda) }

    suspend fun obtenerProveedor(id: Int): Result<Proveedor> =
        envolver { api.obtenerProveedor(id) }

    suspend fun guardarProveedor(proveedor: Proveedor): Result<Proveedor> = envolver {
        if (proveedor.id == 0) api.crearProveedor(proveedor)
        else api.actualizarProveedor(proveedor.id, proveedor)
    }

    suspend fun eliminarProveedor(id: Int): Result<Unit> = envolverSinCuerpo { api.eliminarProveedor(id) }

    // --- Formas de pago (NUEVO, Fase 0b) ---
    suspend fun listarFormasDePago(busqueda: String? = null): Result<List<FormaDePago>> =
        envolver { api.listarFormasDePago(busqueda) }

    suspend fun obtenerFormaDePago(id: Int): Result<FormaDePago> =
        envolver { api.obtenerFormaDePago(id) }

    suspend fun guardarFormaDePago(formaDePago: FormaDePago): Result<FormaDePago> = envolver {
        if (formaDePago.id == 0) api.crearFormaDePago(formaDePago)
        else api.actualizarFormaDePago(formaDePago.id, formaDePago)
    }

    suspend fun eliminarFormaDePago(id: Int): Result<Unit> = envolverSinCuerpo { api.eliminarFormaDePago(id) }

    // --- Helpers comunes ---

    private suspend fun <T> envolver(llamada: suspend () -> Response<T>): Result<T> {
        return try {
            val respuesta = llamada()
            if (respuesta.isSuccessful && respuesta.body() != null) {
                Result.success(respuesta.body()!!)
            } else {
                Result.failure(Exception(extraerDetalle(respuesta)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun envolverSinCuerpo(llamada: suspend () -> Response<Unit>): Result<Unit> {
        return try {
            val respuesta = llamada()
            if (respuesta.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(extraerDetalle(respuesta)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Etiquetas legibles para los campos que puede devolver un error de
     * validación (DRF los nombra tal cual el campo del serializer).
     */
    private val etiquetasDeCampo = mapOf(
        "nombre" to "Nombre",
        "descripcion" to "Descripción",
        "contacto" to "Contacto",
        "email" to "Email",
        "telefono" to "Teléfono"
    )

    /**
     * El backend manda los errores de dos formas distintas:
     * - Errores de negocio (por ejemplo, al intentar eliminar una
     *   marca/categoría/proveedor con productos con stock asociados) como
     *   JSON {"detalle": "..."}.
     * - Errores de validación del serializer (por ejemplo, un nombre
     *   duplicado al crear o editar) como JSON {"campo": ["mensaje", ...]},
     *   con un campo por cada error.
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
