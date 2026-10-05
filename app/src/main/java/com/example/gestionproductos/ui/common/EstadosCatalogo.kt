package com.example.gestionproductos.ui.common

/**
 * Estados compartidos por las pantallas de Categorías, Marcas y
 * Proveedores (alta/edición y baja). Un solo lugar para los tres, ya que
 * las tres pantallas son estructuralmente iguales.
 */
sealed class EstadoGuardadoCatalogo {
    object Ninguno : EstadoGuardadoCatalogo()
    object Guardando : EstadoGuardadoCatalogo()
    object Exito : EstadoGuardadoCatalogo()
    data class Error(val mensaje: String) : EstadoGuardadoCatalogo()
}

sealed class EstadoEliminado {
    object Ninguno : EstadoEliminado()
    data class Exito(val id: Int) : EstadoEliminado()
    data class Error(val mensaje: String) : EstadoEliminado()
}
