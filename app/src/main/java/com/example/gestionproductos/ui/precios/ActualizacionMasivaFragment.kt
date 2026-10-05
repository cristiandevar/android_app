package com.example.gestionproductos.ui.precios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionproductos.data.SessionManager
import com.example.gestionproductos.data.model.Categoria
import com.example.gestionproductos.data.model.Marca
import com.example.gestionproductos.data.model.Proveedor
import com.example.gestionproductos.data.network.RetrofitClient
import com.example.gestionproductos.data.repository.ProductoRepository
import com.example.gestionproductos.databinding.FragmentActualizacionMasivaBinding
import com.example.gestionproductos.ui.common.ViewModelFactory

/**
 * Pantalla restringida a Administrador (MainActivity ya oculta el ítem del
 * menú para un Operador). El backend además rechaza esta acción con 403
 * si alguien llegara igual con otro rol.
 *
 * Flujo (Fase 2): elegir filtros -> "Buscar productos" trae la
 * previsualización -> el usuario puede destildar productos que no quiere
 * tocar -> "Aplicar" manda solo los IDs que quedaron tildados.
 */
class ActualizacionMasivaFragment : Fragment() {

    private var _binding: FragmentActualizacionMasivaBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ActualizacionMasivaViewModel
    private lateinit var adapterPreview: ProductoSeleccionableAdapter

    private var categorias: List<Categoria> = emptyList()
    private var marcas: List<Marca> = emptyList()
    private var proveedores: List<Proveedor> = emptyList()

    // Se pone en true recién cuando el usuario toca "Buscar productos", para
    // no mostrar "0 encontrados" antes de que haya buscado nada (el
    // observer de productosPreview se dispara solo con registrarse, con la
    // lista vacía inicial).
    private var huboBusqueda = false

    private val campos = listOf("precio_unitario" to "Precio unitario (costo)", "porcentaje_ganancia" to "% de ganancia")
    private val operaciones = listOf(
        "porcentaje" to "Sumar/restar %",
        "monto_fijo" to "Sumar/restar monto fijo",
        "valor_nuevo" to "Reemplazar por un valor"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActualizacionMasivaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())
        val api = RetrofitClient.obtenerInstancia(sessionManager)
        val repository = ProductoRepository(api)
        viewModel = ViewModelProvider(
            this, ViewModelFactory { ActualizacionMasivaViewModel(repository) }
        )[ActualizacionMasivaViewModel::class.java]

        binding.spinnerCampo.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, campos.map { it.second }
        )
        binding.spinnerOperacion.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, operaciones.map { it.second }
        )

        adapterPreview = ProductoSeleccionableAdapter { seleccionados, total ->
            binding.textEstadoBusqueda.text = when {
                !huboBusqueda -> getString(com.example.gestionproductos.R.string.mensaje_sin_busqueda)
                total == 0 -> getString(com.example.gestionproductos.R.string.mensaje_sin_resultados)
                else -> getString(
                    com.example.gestionproductos.R.string.mensaje_productos_encontrados, total, seleccionados
                )
            }
        }
        binding.recyclerPreview.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPreview.adapter = adapterPreview

        observarViewModel()
        viewModel.cargarCatalogos()

        binding.botonBuscar.setOnClickListener { buscar() }
        binding.botonAplicar.setOnClickListener { aplicar() }
    }

    private fun observarViewModel() {
        viewModel.categorias.observe(viewLifecycleOwner) {
            categorias = it
            val nombres = listOf("Todas") + it.map { c -> c.nombre }
            binding.spinnerFiltroCategoria.adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres
            )
        }
        viewModel.marcas.observe(viewLifecycleOwner) {
            marcas = it
            val nombres = listOf("Todas") + it.map { m -> m.nombre }
            binding.spinnerFiltroMarca.adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres
            )
        }
        viewModel.proveedores.observe(viewLifecycleOwner) {
            proveedores = it
            val nombres = listOf("Todos") + it.map { p -> p.nombre }
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

        viewModel.estado.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is EstadoActualizacion.Aplicando -> {
                    binding.progressAplicar.visibility = View.VISIBLE
                    binding.botonAplicar.isEnabled = false
                }
                is EstadoActualizacion.Exito -> {
                    binding.progressAplicar.visibility = View.GONE
                    binding.botonAplicar.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        getString(com.example.gestionproductos.R.string.mensaje_productos_actualizados, estado.cantidad),
                        Toast.LENGTH_LONG
                    ).show()
                    // Se vuelve a buscar para que la previsualización refleje
                    // los precios ya actualizados, en vez de dejar los viejos
                    // en pantalla como si nada hubiera cambiado.
                    buscar()
                }
                is EstadoActualizacion.Error -> {
                    binding.progressAplicar.visibility = View.GONE
                    binding.botonAplicar.isEnabled = true
                    Toast.makeText(requireContext(), "Error: ${estado.mensaje}", Toast.LENGTH_LONG).show()
                }
                else -> {}
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

    private fun aplicar() {
        val valor = binding.editValor.text.toString().trim()
        if (valor.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresá un valor", Toast.LENGTH_SHORT).show()
            return
        }

        val productosIds = adapterPreview.obtenerSeleccionados()
        if (productosIds.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Buscá productos y dejá al menos uno tildado antes de aplicar",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val (campo, campoLabel) = campos[binding.spinnerCampo.selectedItemPosition]
        val (operacion, operacionLabel) = operaciones[binding.spinnerOperacion.selectedItemPosition]

        // Es una operación que afecta precios en varios productos a la vez
        // y no se puede deshacer con un botón "atrás": se confirma antes de
        // mandarla, mostrando en criollo qué se va a hacer y a cuántos
        // productos.
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar actualización masiva")
            .setMessage(
                "Se va a aplicar \"$operacionLabel\" (valor $valor) sobre " +
                    "\"$campoLabel\" a ${productosIds.size} producto(s) seleccionados.\n\n" +
                    "¿Confirmás?"
            )
            .setPositiveButton("Aplicar") { _, _ ->
                viewModel.aplicar(productosIds, campo, operacion, valor)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
