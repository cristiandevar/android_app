package com.example.gestionproductos.ui.productos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionproductos.data.model.Producto
import com.example.gestionproductos.databinding.ItemProductoBinding

class ProductoAdapter(
    private val onClick: (Producto) -> Unit
) : ListAdapter<Producto, ProductoAdapter.ProductoViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductoViewHolder(private val binding: ItemProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(producto: Producto) {
            binding.textNombre.text = producto.nombre
            binding.textDetalle.text = listOfNotNull(
                producto.marca_nombre,
                producto.proveedor_nombre,
                producto.categoria_nombre
            ).joinToString(" · ")
            binding.textPrecios.text = "Costo: $${producto.precio_unitario}   " +
                "Venta: $${producto.precio_venta ?: "-"}   Stock: ${producto.stock}"
            binding.root.setOnClickListener { onClick(producto) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Producto>() {
        override fun areItemsTheSame(oldItem: Producto, newItem: Producto) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Producto, newItem: Producto) = oldItem == newItem
    }
}
