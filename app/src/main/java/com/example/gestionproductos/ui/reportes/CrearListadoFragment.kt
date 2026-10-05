package com.example.gestionproductos.ui.reportes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import com.example.gestionproductos.databinding.FragmentCrearListadoBinding
import com.example.gestionproductos.ui.common.ViewModelFactory
import com.example.gestionproductos.ui.precios.ProductoSeleccionableAdapter

/**
 * Mismo patrón de "buscar con filtros -> previsualizar -> deseleccionar
 * lo que no se quiera" que Actualización masiva (reutiliza el mismo
 * ProductoSeleccionableAdapter). Acá el paso final ya no genera ni
 * comparte nada directo: pasa los productos y columnas confirmados a
 * VistaPreviaReporteViewModel (alcance de Activity) y navega a la
 * pantalla de vista previa, donde el usuario ve la tabla de productos y
 * recién ahí, al tocar Descargar o Compartir, elige el formato
 * (PDF/Excel) y se genera el archivo de verdad.
 *
 * Disponible para cualquier usuario logueado (no es una pantalla de
 * datos maestros), a diferencia de Actualización masiva y de
 * Marcas/Proveedores/Categorías.
 */
class CrearListadoFragment : Fragment() {

    private var _binding: FragmentCrearListadoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CrearListadoViewModel
    private lateinit var adapterPreview: ProductoSeleccionableAdapter

    private val vistaPreviaViewModel: VistaPreviaReporteViewModel by activityViewModels {
        val sessionManager = SessionManager(requireContext())
        val api = RetrofitClient.obtenerInstancia(sessionManager)
        ViewModelFactory { VistaPreviaReporteViewModel(ProductoRepository(api)) }
    }

    private var categorias: List<Categoria> = emptyList()
    private var marcas: List<Marca> = emptyList()
    private var proveedores: List<Proveedor> = emptyList()

    private var huboBusqueda = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearListadoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())
        val api = RetrofitClient.obtenerInstancia(sessionManager)
        val repository = ProductoRepository(api)
        viewModel = ViewModelProvider(
            this, ViewModelFactory { CrearListadoViewModel(repository) }
        )[CrearListadoViewModel::class.java]

        adapterPreview = ProductoSeleccionableAdapter { seleccionados, total ->
            binding.textEstadoBusqueda.text = when {
                !huboBusqueda -> getString(R.string.mensaje_sin_busqueda)
                total == 0 -> getString(R.string.mensaje_sin_resultados)
                else -> getString(R.string.mensaje_productos_encontrados, total, seleccionados)
            }
        }
        binding.recyclerPreview.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPreview.adapter = adapterPreview

        observarViewModel()
        viewModel.cargarCatalogos()

        binding.botonBuscar.setOnClickListener { buscar() }
        binding.botonGenerarListado.setOnClickListener { continuarAVistaPrevia() }
    }

    private fun observarViewModel() {
        viewModel.categorias.observe(viewLifecycleOwner) {
            categorias = it
            val nombres = listOf(getString(R.string.filtro_todas)) + it.map { c -> c.nombre }
            binding.spinnerFiltroCategoria.adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres
            )
        }
        viewModel.marcas.observe(viewLifecycleOwner) {
            marcas = it
            val nombres = listOf(getString(R.string.filtro_todas)) + it.map { m -> m.nombre }
            binding.spinnerFiltroMarca.adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres
            )
        }
        viewModel.proveedores.observe(viewLifecycleOwner) {
            proveedores = it
            val nombres = listOf(getString(R.string.filtro_todos)) + it.map { p -> p.nombre }
            binding.spinnerFiltroProveedor.adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres
            )
        }

        viewModel.buscando.observe(viewLifecycleOwner) { cargando ->
            binding.progressBuscar.visibility = if (cargando) View.VISIBLE else View.GONE
            binding.botonBuscar.isEnabled = !cargando
        }
        viewModel.productosPreview.observe(viewLifecycleOwner) { productos ->
            adapterPreview.actualizarLista(productos)
        }
        viewModel.errorBusqueda.observe(viewLifecycleOwner) { mensaje ->
            if (mensaje != null) {
                Toast.makeText(requireContext(), "Error al buscar: $mensaje", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun buscar() {
        huboBusqueda = true

        val posCategoria = binding.spinnerFiltroCategoria.selectedItemPosition
        val categoriaId = if (posCategoria <= 0) null else categorias[posCategoria - 1].id

        val posMarca = binding.spinnerFiltroMarca.selectedItemPosition
        val marcaId = if (posMarca <= 0) null else marcas[posMarca - 1].id

        val posProveedor = binding.spinnerFiltroProveedor.selectedItemPosition
        val proveedorId = if (posProveedor <= 0) null else proveedores[posProveedor - 1].id

        viewModel.buscar(categoriaId, marcaId, proveedorId)
    }

    private fun continuarAVistaPrevia() {
        val productosSeleccionados = adapterPreview.obtenerProductosSeleccionados()
        if (productosSeleccionados.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Buscá productos y dejá al menos uno tildado antes de continuar",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val columnas = mutableListOf<String>().apply {
            if (binding.checkColNombre.isChecked) add("nombre")
            if (binding.checkColCategoria.isChecked) add("categoria_nombre")
            if (binding.checkColMarca.isChecked) add("marca_nombre")
            if (binding.checkColProveedor.isChecked) add("proveedor_nombre")
            if (binding.checkColPrecioUnitario.isChecked) add("precio_unitario")
            if (binding.checkColPorcentajeGanancia.isChecked) add("porcentaje_ganancia")
            if (binding.checkColPrecioVenta.isChecked) add("precio_venta")
            if (binding.checkColStock.isChecked) add("stock")
        }
        if (columnas.isEmpty()) {
            Toast.makeText(requireContext(), "Elegí al menos una columna", Toast.LENGTH_LONG).show()
            return
        }

        vistaPreviaViewModel.cargar(productosSeleccionados, columnas)
        findNavController().navigate(R.id.accionAVistaPreviaReporte)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
