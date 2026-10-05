package com.example.gestionproductos.ui.productos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionproductos.R
import com.example.gestionproductos.data.SessionManager
import com.example.gestionproductos.data.model.Categoria
import com.example.gestionproductos.data.model.Marca
import com.example.gestionproductos.data.model.Proveedor
import com.example.gestionproductos.data.network.RetrofitClient
import com.example.gestionproductos.data.repository.ProductoRepository
import com.example.gestionproductos.databinding.FragmentProductoListBinding
import com.example.gestionproductos.ui.common.ViewModelFactory

class ProductoListFragment : Fragment() {

    private var _binding: FragmentProductoListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProductoListViewModel
    private lateinit var adapter: ProductoAdapter

    private var categorias: List<Categoria> = emptyList()
    private var marcas: List<Marca> = emptyList()
    private var proveedores: List<Proveedor> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())
        val api = RetrofitClient.obtenerInstancia(sessionManager)
        val repository = ProductoRepository(api)
        viewModel = ViewModelProvider(
            this, ViewModelFactory { ProductoListViewModel(repository) }
        )[ProductoListViewModel::class.java]

        adapter = ProductoAdapter { producto ->
            findNavController().navigate(
                R.id.accionAProductoForm,
                Bundle().apply { putInt("productoId", producto.id) }
            )
        }
        binding.recyclerProductos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProductos.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.cargarProductos() }

        binding.fabNuevoProducto.setOnClickListener {
            findNavController().navigate(R.id.accionAProductoForm)
        }

        observarViewModel()
        configurarFiltros()

        viewModel.cargarCatalogos()
        viewModel.cargarProductos()
    }

    private fun observarViewModel() {
        viewModel.productos.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.cargando.observe(viewLifecycleOwner) { binding.swipeRefresh.isRefreshing = it }
        viewModel.error.observe(viewLifecycleOwner) { mensaje ->
            if (mensaje != null) {
                Toast.makeText(requireContext(), "Error: $mensaje", Toast.LENGTH_LONG).show()
            }
        }
        viewModel.categorias.observe(viewLifecycleOwner) {
            categorias = it
            armarSpinnerCategorias()
        }
        viewModel.marcas.observe(viewLifecycleOwner) {
            marcas = it
            armarSpinnerMarcas()
        }
        viewModel.proveedores.observe(viewLifecycleOwner) {
            proveedores = it
            armarSpinnerProveedores()
        }
    }

    private fun configurarFiltros() {
        binding.spinnerCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                viewModel.filtroCategoriaId = if (position == 0) null else categorias[position - 1].id
                viewModel.cargarProductos()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerMarca.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                viewModel.filtroMarcaId = if (position == 0) null else marcas[position - 1].id
                viewModel.cargarProductos()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerProveedor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                viewModel.filtroProveedorId = if (position == 0) null else proveedores[position - 1].id
                viewModel.cargarProductos()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun armarSpinnerCategorias() {
        val nombres = listOf(getString(R.string.filtro_todas)) + categorias.map { it.nombre }
        binding.spinnerCategoria.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres
        )
    }

    private fun armarSpinnerMarcas() {
        val nombres = listOf(getString(R.string.filtro_todas)) + marcas.map { it.nombre }
        binding.spinnerMarca.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres
        )
    }

    private fun armarSpinnerProveedores() {
        val nombres = listOf(getString(R.string.filtro_todos)) + proveedores.map { it.nombre }
        binding.spinnerProveedor.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
