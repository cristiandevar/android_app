package com.example.gestionproductos.ui.precios

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionproductos.data.model.Producto
import com.example.gestionproductos.databinding.ItemProductoSeleccionableBinding

/**
 * Lista de previsualización de la actualización masiva: muestra los
 * productos que matchean los filtros elegidos y deja deseleccionar los
 * que el usuario no quiere tocar antes de aplicar el cambio.
 *
 * Mantiene el set de seleccionados acá adentro (todos tildados al
 * cargar la lista) en vez de en el ViewModel, para no complicar el
 * flujo con otra LiveData: el Fragment lo lee recién al tocar "Aplicar".
 */
class ProductoSeleccionableAdapter(
    private val onCambioSeleccion: (seleccionados: Int, total: Int) -> Unit
) : RecyclerView.Adapter<ProductoSeleccionableAdapter.ViewHolder>() {

    private val productos = mutableListOf<Producto>()
    private val seleccionados = mutableSetOf<Int>()

    fun actualizarLista(nuevaLista: List<Producto>) {
        productos.clear()
        productos.addAll(nuevaLista)
        seleccionados.clear()
        seleccionados.addAll(nuevaLista.map { it.id })
        notifyDataSetChanged()
        onCambioSeleccion(seleccionados.size, productos.size)
    }

    fun obtenerSeleccionados(): List<Int> = seleccionados.toList()

    /**
     * Igual que obtenerSeleccionados() pero devuelve los productos
     * completos (no solo el id), en el mismo orden en que se cargaron.
     * Lo usa Crear Listado para pasarle los datos ya confirmados a la
     * vista previa del reporte sin tener que volver a pedirlos al
     * backend.
     */
    fun obtenerProductosSeleccionados(): List<Producto> =
        productos.filter { seleccionados.contains(it.id) }

    inner class ViewHolder(val binding: ItemProductoSeleccionableBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductoSeleccionableBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = productos[position]

        // Se saca el listener antes de fijar el estado para no disparar
        // un "cambio" falso por el reciclado de vistas del RecyclerView.
        holder.binding.checkProducto.setOnCheckedChangeListener(null)
        holder.binding.checkProducto.isChecked = seleccionados.contains(producto.id)
        holder.binding.textNombre.text = producto.nombre
        holder.binding.textDetalle.text =
            "Precio unitario actual: ${producto.precio_unitario}"

        holder.binding.checkProducto.setOnCheckedChangeListener { _, marcado ->
            if (marcado) seleccionados.add(producto.id) else seleccionados.remove(producto.id)
            onCambioSeleccion(seleccionados.size, productos.size)
        }
    }

    override fun getItemCount(): Int = productos.size
}
