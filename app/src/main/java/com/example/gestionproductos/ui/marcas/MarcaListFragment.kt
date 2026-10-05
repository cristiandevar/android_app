package com.example.gestionproductos.ui.marcas

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionproductos.R
import com.example.gestionproductos.data.SessionManager
import com.example.gestionproductos.data.model.Marca
import com.example.gestionproductos.data.network.RetrofitClient
import com.example.gestionproductos.data.repository.CatalogoRepository
import com.example.gestionproductos.databinding.FragmentCatalogoListaBinding
import com.example.gestionproductos.ui.common.CatalogoAdapter
import com.example.gestionproductos.ui.common.EstadoEliminado
import com.example.gestionproductos.ui.common.ViewModelFactory

/** Pantalla restringida a Administrador (ver MainActivity y el backend). */
class MarcaListFragment : Fragment() {

    private var _binding: FragmentCatalogoListaBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MarcaListViewModel
    private lateinit var adapter: CatalogoAdapter<Marca>

    private val manejadorBusqueda = Handler(Looper.getMainLooper())
    private var tareaBusqueda: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogoListaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())
        val api = RetrofitClient.obtenerInstancia(sessionManager)
        val repository = CatalogoRepository(api)
        viewModel = ViewModelProvider(
            this, ViewModelFactory { MarcaListViewModel(repository) }
        )[MarcaListViewModel::class.java]

        adapter = CatalogoAdapter(
            obtenerId = { it.id },
            obtenerNombre = { it.nombre },
            obtenerSubtitulo = { null },
            onClickItem = { marca ->
                findNavController().navigate(
                    R.id.accionAMarcaForm,
                    Bundle().apply { putInt("marcaId", marca.id) }
                )
            },
            onClickEliminar = { marca -> confirmarEliminar(marca) }
        )
        binding.recyclerLista.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLista.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.cargar() }
        binding.fabAgregar.setOnClickListener {
            findNavController().navigate(R.id.accionAMarcaForm)
        }

        binding.editBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tareaBusqueda?.let { manejadorBusqueda.removeCallbacks(it) }
                val nuevaTarea = Runnable {
                    viewModel.busqueda = s?.toString()?.trim()?.ifEmpty { null }
                    viewModel.cargar()
                }
                tareaBusqueda = nuevaTarea
                manejadorBusqueda.postDelayed(nuevaTarea, 350)
            }
        })

        observarViewModel()
        viewModel.cargar()
    }

    private fun observarViewModel() {
        viewModel.marcas.observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)
            binding.textVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.cargando.observe(viewLifecycleOwner) { binding.swipeRefresh.isRefreshing = it }
        viewModel.error.observe(viewLifecycleOwner) { mensaje ->
            if (mensaje != null) {
                Toast.makeText(requireContext(), "Error: $mensaje", Toast.LENGTH_LONG).show()
            }
        }
        viewModel.estadoEliminado.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is EstadoEliminado.Exito -> {
                    Toast.makeText(
                        requireContext(), getString(R.string.mensaje_elemento_eliminado), Toast.LENGTH_SHORT
                    ).show()
                }
                is EstadoEliminado.Error -> {
                    Toast.makeText(requireContext(), "Error: ${estado.mensaje}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun confirmarEliminar(marca: Marca) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.boton_eliminar))
            .setMessage(getString(R.string.mensaje_confirmar_eliminar, marca.nombre))
            .setPositiveButton(R.string.boton_eliminar) { _, _ -> viewModel.eliminar(marca) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tareaBusqueda?.let { manejadorBusqueda.removeCallbacks(it) }
        _binding = null
    }
}
