package com.example.gestionproductos.ui.categorias

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.gestionproductos.R
import com.example.gestionproductos.data.SessionManager
import com.example.gestionproductos.data.model.Categoria
import com.example.gestionproductos.data.network.RetrofitClient
import com.example.gestionproductos.data.repository.CatalogoRepository
import com.example.gestionproductos.databinding.FragmentCategoriaFormBinding
import com.example.gestionproductos.ui.common.EstadoGuardadoCatalogo
import com.example.gestionproductos.ui.common.ViewModelFactory

class CategoriaFormFragment : Fragment() {

    private var _binding: FragmentCategoriaFormBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CategoriaFormViewModel
    private var categoriaIdEditando: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriaFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoriaIdEditando = arguments?.getInt("categoriaId", -1) ?: -1

        val sessionManager = SessionManager(requireContext())
        val api = RetrofitClient.obtenerInstancia(sessionManager)
        val repository = CatalogoRepository(api)
        viewModel = ViewModelProvider(
            this, ViewModelFactory { CategoriaFormViewModel(repository) }
        )[CategoriaFormViewModel::class.java]

        observarViewModel()
        if (categoriaIdEditando != -1) {
            viewModel.cargarCategoria(categoriaIdEditando)
        }

        binding.botonGuardar.setOnClickListener { guardar() }
    }

    private fun observarViewModel() {
        viewModel.categoriaCargada.observe(viewLifecycleOwner) { categoria ->
            if (categoria != null) {
                binding.editNombre.setText(categoria.nombre)
                binding.editDescripcion.setText(categoria.descripcion)
            }
        }
        viewModel.estadoGuardado.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is EstadoGuardadoCatalogo.Guardando -> binding.botonGuardar.isEnabled = false
                is EstadoGuardadoCatalogo.Exito -> {
                    Toast.makeText(requireContext(), getString(R.string.mensaje_elemento_guardado), Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is EstadoGuardadoCatalogo.Error -> {
                    binding.botonGuardar.isEnabled = true
                    Toast.makeText(requireContext(), "Error: ${estado.mensaje}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun guardar() {
        val nombre = binding.editNombre.text.toString().trim()
        val descripcion = binding.editDescripcion.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresá un nombre", Toast.LENGTH_SHORT).show()
            return
        }

        val categoria = Categoria(
            id = if (categoriaIdEditando != -1) categoriaIdEditando else 0,
            nombre = nombre,
            descripcion = descripcion
        )
        viewModel.guardar(categoria)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
