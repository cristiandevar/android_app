package com.example.gestionproductos.data.model

// Estos data class reflejan uno a uno los serializers del backend
// (productos_api/serializers.py). Si cambiás un nombre de campo de un
// lado, hay que cambiarlo del otro.

data class Categoria(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    val activo: Boolean = true
)

data class Marca(
    val id: Int,
    val nombre: String,
    val activo: Boolean = true
)

data class Proveedor(
    val id: Int,
    val nombre: String,
    val contacto: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val activo: Boolean = true
)

// NUEVO (Fase 0b): mismo patrón simple que Marca. Se usa en Ventas
// (Fase 2) para registrar con qué se cobró.
data class FormaDePago(
    val id: Int,
    val nombre: String,
    val activo: Boolean = true
)

data class Producto(
    val id: Int = 0,
    val nombre: String,
    val descripcion: String = "",
    val categoria: Int,
    val categoria_nombre: String? = null,
    val marca: Int,
    val marca_nombre: String? = null,
    val proveedor: Int,
    val proveedor_nombre: String? = null,
    val precio_unitario: String,
    val porcentaje_ganancia: String,
    val precio_venta: String? = null,
    val stock: Int,
    // NUEVO (Fase 0c): código de barra, opcional. null cuando el producto
    // no tiene uno cargado (ver Fase 2, se usa para escanear en Venta).
    val codigo_barra: String? = null,
    val activo: Boolean = true
)

data class GrupoProductos(
    val grupo: String,
    val productos: List<Producto>
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class TokenResponse(
    val access: String,
    val refresh: String
)

data class UsuarioActual(
    val username: String,
    val rol: String
)

data class ActualizacionMasivaRequest(
    // Antes se mandaba "filtros" y el backend aplicaba a lo que matcheara en
    // ese momento; ahora se manda la lista final de IDs que el usuario ya
    // vio y confirmó en la previsualización (ver Fase 2).
    val productos_ids: List<Int>,
    val campo: String,
    val operacion: String,
    val valor: String
)

data class ActualizacionMasivaResponse(
    val actualizados: Int
)

data class GenerarListadoRequest(
    // Misma idea que ActualizacionMasivaRequest: la app ya buscó, mostró y
    // dejó deseleccionar productos; acá se manda la lista final confirmada.
    val productos_ids: List<Int>,
    val columnas: List<String>,
    val formato: String
)
