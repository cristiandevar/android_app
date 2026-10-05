package com.example.gestionproductos.ui.reportes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gestionproductos.databinding.FragmentEstadisticaBinding

/**
 * Pantalla stub: queda lista en el menú para cuando se agreguen ventas y
 * compras al sistema, momento en el que va a tener sentido mostrar acá
 * estadísticas reales (producto más vendido, etc.). Por ahora es solo un
 * mensaje de "próximamente".
 */
class EstadisticaFragment : Fragment() {

    private var _binding: FragmentEstadisticaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEstadisticaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
