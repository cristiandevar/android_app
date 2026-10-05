package com.example.gestionproductos.ui.inicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gestionproductos.databinding.FragmentInicioBinding

/**
 * Pantalla "Inicio": una entrada más del menú, no la pantalla de
 * aterrizaje real de la app (esa sigue siendo Productos, ver
 * nav_graph.xml startDestination). Por ahora son tarjetas con datos de
 * ejemplo (hardcodeados en fragment_inicio.xml) a modo de vidriera de lo
 * que se va a poder mostrar acá una vez que el sistema registre ventas y
 * compras reales.
 */
class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
