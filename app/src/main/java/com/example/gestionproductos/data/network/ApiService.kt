package com.example.gestionproductos.data.network

import com.example.gestionproductos.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/token/")
    suspend fun login(@Body datos: LoginRequest): Response<TokenResponse>

    @GET("api/me/")
    suspend fun usuarioActual(): Response<UsuarioActual>

    @GET("api/categorias/")
    suspend fun listarCategorias(@Query("search") search: String? = null): Response<List<Categoria>>

    @GET("api/categorias/{id}/")
    suspend fun obtenerCategoria(@Path("id") id: Int): Response<Categoria>

    @POST("api/categorias/")
    suspend fun crearCategoria(@Body categoria: Categoria): Response<Categoria>

    @PUT("api/categorias/{id}/")
    suspend fun actualizarCategoria(@Path("id") id: Int, @Body categoria: Categoria): Response<Categoria>

    @DELETE("api/categorias/{id}/")
    suspend fun eliminarCategoria(@Path("id") id: Int): Response<Unit>

    @GET("api/marcas/")
    suspend fun listarMarcas(@Query("search") search: String? = null): Response<List<Marca>>

    @GET("api/marcas/{id}/")
    suspend fun obtenerMarca(@Path("id") id: Int): Response<Marca>

    @POST("api/marcas/")
    suspend fun crearMarca(@Body marca: Marca): Response<Marca>

    @PUT("api/marcas/{id}/")
    suspend fun actualizarMarca(@Path("id") id: Int, @Body marca: Marca): Response<Marca>

    @DELETE("api/marcas/{id}/")
    suspend fun eliminarMarca(@Path("id") id: Int): Response<Unit>

    @GET("api/proveedores/")
    suspend fun listarProveedores(@Query("search") search: String? = null): Response<List<Proveedor>>

    @GET("api/proveedores/{id}/")
    suspend fun obtenerProveedor(@Path("id") id: Int): Response<Proveedor>

    @POST("api/proveedores/")
    suspend fun crearProveedor(@Body proveedor: Proveedor): Response<Proveedor>

    @PUT("api/proveedores/{id}/")
    suspend fun actualizarProveedor(@Path("id") id: Int, @Body proveedor: Proveedor): Response<Proveedor>

    @DELETE("api/proveedores/{id}/")
    suspend fun eliminarProveedor(@Path("id") id: Int): Response<Unit>

    // --- Formas de pago (NUEVO, Fase 0b) ---

    @GET("api/formas-de-pago/")
    suspend fun listarFormasDePago(@Query("search") search: String? = null): Response<List<FormaDePago>>

    @GET("api/formas-de-pago/{id}/")
    suspend fun obtenerFormaDePago(@Path("id") id: Int): Response<FormaDePago>

    @POST("api/formas-de-pago/")
    suspend fun crearFormaDePago(@Body formaDePago: FormaDePago): Response<FormaDePago>

    @PUT("api/formas-de-pago/{id}/")
    suspend fun actualizarFormaDePago(@Path("id") id: Int, @Body formaDePago: FormaDePago): Response<FormaDePago>

    @DELETE("api/formas-de-pago/{id}/")
    suspend fun eliminarFormaDePago(@Path("id") id: Int): Response<Unit>

    @GET("api/productos/")
    suspend fun listarProductos(
        @Query("categoria") categoria: Int? = null,
        @Query("marca") marca: Int? = null,
        @Query("proveedor") proveedor: Int? = null,
        @Query("search") search: String? = null
    ): Response<List<Producto>>

    @GET("api/productos/agrupado/")
    suspend fun listarProductosAgrupados(@Query("campo") campo: String): Response<List<GrupoProductos>>

    @GET("api/productos/{id}/")
    suspend fun obtenerProducto(@Path("id") id: Int): Response<Producto>

    @POST("api/productos/")
    suspend fun crearProducto(@Body producto: Producto): Response<Producto>

    @PUT("api/productos/{id}/")
    suspend fun actualizarProducto(@Path("id") id: Int, @Body producto: Producto): Response<Producto>

    // NUEVO (Fase 0c): lo va a usar la pantalla de Venta (Fase 2) para
    // traer el producto apenas se escanea su código. Ya queda la llamada
    // lista, aunque todavía no hay pantalla que la use.
    @GET("api/productos/buscar_por_codigo_barra/")
    suspend fun buscarProductoPorCodigoBarra(@Query("codigo") codigo: String): Response<Producto>

    @POST("api/productos/actualizar_precios_masivo/")
    suspend fun actualizarPreciosMasivo(
        @Body datos: ActualizacionMasivaRequest
    ): Response<ActualizacionMasivaResponse>

    // Devuelve el archivo (PDF o Excel) tal cual lo manda el backend, sin
    // pasar por el conversor de Gson: Retrofit soporta ResponseBody como
    // tipo de retorno para leer bytes crudos.
    @POST("api/productos/generar_listado/")
    suspend fun generarListado(@Body datos: GenerarListadoRequest): Response<ResponseBody>
}
