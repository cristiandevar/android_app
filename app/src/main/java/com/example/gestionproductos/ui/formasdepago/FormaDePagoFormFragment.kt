package com.example.gestionproductos.ui.formasdepago

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
import com.example.gestionproductos.data.model.FormaDePago
import com.example.gestionproductos.data.network.RetrofitClient
import com.example.gestionproductos.data.repository.CatalogoRepository
import com.example.gestionproductos.databinding.FragmentFormadepagoFormBinding
import com.example.gestionproductos.ui.common.EstadoGuardadoCatalogo
import com.example.gestionproductos.ui.common.ViewModelFactory

class FormaDePagoFormFragment : Fragment() {

    private var _binding: FragmentFormadepagoFormBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FormaDePagoFormViewModel
    private var formaDePagoIdEditando: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormadepagoFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        formaDePagoIdEditando = arguments?.getInt("formaDePagoId", -1) ?: -1

        val sessionManager = SessionManager(requireContext())
        val api = RetrofitClient.obtenerInstancia(sessionManager)
        val repository = CatalogoRepository(api)
        viewModel = ViewModelProvider(
            this, ViewModelFactory { FormaDePagoFormViewModel(repository) }
        )[FormaDePagoFormViewModel::class.java]

        observarViewModel()
        if (formaDePagoIdEditando != -1) {
            viewModel.cargarFormaDePago(formaDePagoIdEditando)
        }

        binding.botonGuardar.setOnClickListener { guardar() }
    }

    private fun observarViewModel() {
        viewModel.formaDePagoCargada.observe(viewLifecycleOwner) { formaDePago ->
            if (formaDePago != null) {
                binding.editNombre.setText(formaDePago.nombre)
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
        if (nombre.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresá un nombre", Toast.LENGTH_SHORT).show()
            return
        }

        val formaDePago = FormaDePago(
            id = if (formaDePagoIdEditando != -1) formaDePagoIdEditando else 0,
            nombre = nombre
        )
        viewModel.guardar(formaDePago)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
