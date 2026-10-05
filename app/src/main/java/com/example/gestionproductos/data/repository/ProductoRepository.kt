package com.example.gestionproductos.data.repository

import com.example.gestionproductos.data.model.*
import com.example.gestionproductos.data.network.ApiService

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
                Result.failure(Exception("Error del servidor: ${respuesta.code()}"))
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
                Result.failure(Exception("Error del servidor: ${respuesta.code()}"))
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
                Result.failure(Exception("Error del servidor: ${respuesta.code()}"))
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
                Result.failure(Exception("Error del servidor: ${respuesta.code()}"))
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
                Result.failure(Exception("Error del servidor: ${respuesta.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
