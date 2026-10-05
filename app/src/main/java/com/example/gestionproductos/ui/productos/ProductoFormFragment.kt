package com.example.gestionproductos.ui.productos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.gestionproductos.data.SessionManager
import com.example.gestionproductos.data.model.Categoria
import com.example.gestionproductos.data.model.Marca
import com.example.gestionproductos.data.model.Producto
import com.example.gestionproductos.data.model.Proveedor
import com.example.gestionproductos.data.network.RetrofitClient
import com.example.gestionproductos.data.repository.ProductoRepository
import com.example.gestionproductos.databinding.FragmentProductoFormBinding
import com.example.gestionproductos.ui.common.ViewModelFactory

/**
 * Alta y edición de un producto. Por ahora la carga es 100% manual: el
 * botón de escaneo de código de barra (que autocompletaría nombre/marca)
 * queda para cuando retomemos ese tema, tal como se acordó.
 */
class ProductoFormFragment : Fragment() {

    private var _binding: FragmentProductoFormBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProductoFormViewModel

    private var categorias: List<Categoria> = emptyList()
    private var marcas: List<Marca> = emptyList()
    private var proveedores: List<Proveedor> = emptyList()

    private var productoIdEditando: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductoFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productoIdEditando = arguments?.getInt("productoId", -1) ?: -1

        val sessionManager = SessionManager(requireContext())
        val api = RetrofitClient.obtenerInstancia(sessionManager)
        val repository = ProductoRepository(api)
        viewModel = ViewModelProvider(
            this, ViewModelFactory { ProductoFormViewModel(repository) }
        )[ProductoFormViewModel::class.java]

        observarViewModel()
        viewModel.cargarCatalogos()
        if (productoIdEditando != -1) {
            viewModel.cargarProducto(productoIdEditando)
        }

        binding.botonGuardar.setOnClickListener { guardar() }
    }

    private fun observarViewModel() {
        viewModel.categorias.observe(viewLifecycleOwner) {
            categorias = it
            binding.spinnerCategoria.adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, it.map { c -> c.nombre }
            )
        }
        viewModel.marcas.observe(viewLifecycleOwner) {
            marcas = it
            binding.spinnerMarca.adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, it.map { m -> m.nombre }
            )
        }
        viewModel.proveedores.observe(viewLifecycleOwner) {
            proveedores = it
            binding.spinnerProveedor.adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, it.map { p -> p.nombre }
            )
        }
        viewModel.productoCargado.observe(viewLifecycleOwner) { producto ->
            if (producto != null) precargarFormulario(producto)
        }
        viewModel.estadoGuardado.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is EstadoGuardado.Guardando -> binding.botonGuardar.isEnabled = false
                is EstadoGuardado.Exito -> {
                    Toast.makeText(requireContext(), "Producto guardado", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is EstadoGuardado.Error -> {
                    binding.botonGuardar.isEnabled = true
                    Toast.makeText(requireContext(), "Error: ${estado.mensaje}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun precargarFormulario(producto: Producto) {
        binding.editNombre.setText(producto.nombre)
        binding.editPrecioUnitario.setText(producto.precio_unitario)
        binding.editPorcentajeGanancia.setText(producto.porcentaje_ganancia)
        binding.editCantidad.setText(producto.stock.toString())
        categorias.indexOfFirst { it.id == producto.categoria }.takeIf { it >= 0 }
            ?.let { binding.spinnerCategoria.setSelection(it) }
        marcas.indexOfFirst { it.id == producto.marca }.takeIf { it >= 0 }
            ?.let { binding.spinnerMarca.setSelection(it) }
        proveedores.indexOfFirst { it.id == producto.proveedor }.takeIf { it >= 0 }
            ?.let { binding.spinnerProveedor.setSelection(it) }
    }

    private fun guardar() {
        val nombre = binding.editNombre.text.toString().trim()
        val precioUnitario = binding.editPrecioUnitario.text.toString().trim()
        val porcentajeGanancia = binding.editPorcentajeGanancia.text.toString().trim().ifEmpty { "0" }
        val cantidadTexto = binding.editCantidad.text.toString().trim().ifEmpty { "0" }

        if (nombre.isEmpty() || precioUnitario.isEmpty() ||
            categorias.isEmpty() || marcas.isEmpty() || proveedores.isEmpty()
        ) {
            Toast.makeText(requireContext(), "Completá al menos nombre, precio, categoría, marca y proveedor", Toast.LENGTH_LONG).show()
            return
        }

        val categoriaId = categorias[binding.spinnerCategoria.selectedItemPosition].id
        val marcaId = marcas[binding.spinnerMarca.selectedItemPosition].id
        val proveedorId = proveedores[binding.spinnerProveedor.selectedItemPosition].id

        val producto = Producto(
            id = if (productoIdEditando != -1) productoIdEditando else 0,
            nombre = nombre,
            categoria = categoriaId,
            marca = marcaId,
            proveedor = proveedorId,
            precio_unitario = precioUnitario,
            porcentaje_ganancia = porcentajeGanancia,
            stock = cantidadTexto.toIntOrNull() ?: 0
        )
        viewModel.guardar(producto)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
