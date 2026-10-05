package com.example.gestionproductos.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionproductos.databinding.ItemCatalogoBinding

/**
 * Adapter genérico para las listas de Categoría/Marca/Proveedor: son
 * estructuralmente iguales (nombre + un subtítulo opcional + botón
 * eliminar), solo cambia de dónde sale cada texto según el tipo T.
 */
class CatalogoAdapter<T : Any>(
    private val obtenerId: (T) -> Int,
    private val obtenerNombre: (T) -> String,
    private val obtenerSubtitulo: (T) -> String?,
    private val onClickItem: (T) -> Unit,
    private val onClickEliminar: (T) -> Unit
) : ListAdapter<T, CatalogoAdapter<T>.ViewHolder>(
    object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T) = obtenerId(oldItem) == obtenerId(newItem)
        override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
    }
) {

    inner class ViewHolder(val binding: ItemCatalogoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCatalogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.textNombre.text = obtenerNombre(item)

        val subtitulo = obtenerSubtitulo(item)
        if (subtitulo.isNullOrBlank()) {
            holder.binding.textSubtitulo.visibility = View.GONE
        } else {
            holder.binding.textSubtitulo.visibility = View.VISIBLE
            holder.binding.textSubtitulo.text = subtitulo
        }

        holder.binding.root.setOnClickListener { onClickItem(item) }
        holder.binding.botonEliminar.setOnClickListener { onClickEliminar(item) }
    }
}
