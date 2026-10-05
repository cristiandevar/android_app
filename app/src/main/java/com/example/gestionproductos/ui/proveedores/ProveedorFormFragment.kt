package com.example.gestionproductos.ui.proveedores

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
import com.example.gestionproductos.data.model.Proveedor
import com.example.gestionproductos.data.network.RetrofitClient
import com.example.gestionproductos.data.repository.CatalogoRepository
import com.example.gestionproductos.databinding.FragmentProveedorFormBinding
import com.example.gestionproductos.ui.common.EstadoGuardadoCatalogo
import com.example.gestionproductos.ui.common.ViewModelFactory

class ProveedorFormFragment : Fragment() {

    private var _binding: FragmentProveedorFormBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProveedorFormViewModel
    private var proveedorIdEditando: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProveedorFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        proveedorIdEditando = arguments?.getInt("proveedorId", -1) ?: -1

        val sessionManager = SessionManager(requireContext())
        val api = RetrofitClient.obtenerInstancia(sessionManager)
        val repository = CatalogoRepository(api)
        viewModel = ViewModelProvider(
            this, ViewModelFactory { ProveedorFormViewModel(repository) }
        )[ProveedorFormViewModel::class.java]

        observarViewModel()
        if (proveedorIdEditando != -1) {
            viewModel.cargarProveedor(proveedorIdEditando)
        }

        binding.botonGuardar.setOnClickListener { guardar() }
    }

    private fun observarViewModel() {
        viewModel.proveedorCargado.observe(viewLifecycleOwner) { proveedor ->
            if (proveedor != null) {
                binding.editNombre.setText(proveedor.nombre)
                binding.editContacto.setText(proveedor.contacto)
                binding.editEmail.setText(proveedor.email)
                binding.editTelefono.setText(proveedor.telefono)
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
        val contacto = binding.editContacto.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val telefono = binding.editTelefono.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresá un nombre", Toast.LENGTH_SHORT).show()
            return
        }

        val proveedor = Proveedor(
            id = if (proveedorIdEditando != -1) proveedorIdEditando else 0,
            nombre = nombre,
            contacto = contacto,
            email = email,
            telefono = telefono
        )
        viewModel.guardar(proveedor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
