package com.example.gestionproductos.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gestionproductos.R
import com.example.gestionproductos.databinding.FragmentProximamenteBinding

/**
 * Pantalla stub genérica reutilizada por las entradas del menú lateral que
 * todavía no tienen funcionalidad propia (Órdenes, Generar Orden, Ventas,
 * Registrar Venta, Escanear listado): por ahora solo confirman que el
 * ítem existe en el menú, mientras se definen sus reglas de negocio antes
 * de construir cada pantalla real.
 */
class ProximamenteFragment : Fragment() {

    private var _binding: FragmentProximamenteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProximamenteBinding.inflate(inflater, container, false)
        binding.textTitulo.text = arguments?.getString("titulo") ?: getString(R.string.titulo_proximamente)
        binding.textMensaje.text = getString(R.string.mensaje_proximamente_generico)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
