package com.example.gestionproductos.ui.reportes

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.gestionproductos.R
import com.example.gestionproductos.data.SessionManager
import com.example.gestionproductos.data.model.Producto
import com.example.gestionproductos.data.network.RetrofitClient
import com.example.gestionproductos.data.repository.ProductoRepository
import com.example.gestionproductos.databinding.FragmentVistaPreviaReporteBinding
import com.example.gestionproductos.ui.common.ViewModelFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Vista previa del reporte: muestra en una tabla los productos y
 * columnas que el usuario ya confirmó en "Generar Listado" (recibidos
 * vía VistaPreviaReporteViewModel, con alcance de Activity, cargados
 * por CrearListadoFragment antes de navegar acá). Solo se llega a esta
 * pantalla desde ahí, no hay entrada de menú ni otra forma de navegar.
 *
 * El formato (PDF/Excel) ya no se elige en la pantalla anterior: se
 * pregunta acá recién al tocar Descargar o Compartir, y ahí se genera
 * el archivo llamando al backend.
 */
class VistaPreviaReporteFragment : Fragment() {

    private var _binding: FragmentVistaPreviaReporteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VistaPreviaReporteViewModel by activityViewModels {
        val sessionManager = SessionManager(requireContext())
        val api = RetrofitClient.obtenerInstancia(sessionManager)
        ViewModelFactory { VistaPreviaReporteViewModel(ProductoRepository(api)) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVistaPreviaReporteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productos = viewModel.productos.value.orEmpty()
        val columnas = viewModel.columnas.value.orEmpty()
        binding.textResumenVistaPrevia.text =
            getString(R.string.mensaje_resumen_vista_previa, productos.size)
        armarTabla(productos, columnas)

        binding.botonDescargarReporte.setOnClickListener {
            mostrarSelectorDeFormato(AccionArchivo.DESCARGAR)
        }
        binding.botonCompartirReporte.setOnClickListener {
            mostrarSelectorDeFormato(AccionArchivo.COMPARTIR)
        }

        viewModel.estadoExportacion.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is EstadoExportacion.Generando -> {
                    binding.progressGenerarDesdeVistaPrevia.visibility = View.VISIBLE
                    habilitarBotones(false)
                }
                is EstadoExportacion.Exito -> {
                    binding.progressGenerarDesdeVistaPrevia.visibility = View.GONE
                    habilitarBotones(true)
                    when (estado.accion) {
                        AccionArchivo.DESCARGAR -> descargarArchivo(estado.bytes, estado.formato)
                        AccionArchivo.COMPARTIR -> compartirArchivo(estado.bytes, estado.formato)
                    }
                }
                is EstadoExportacion.Error -> {
                    binding.progressGenerarDesdeVistaPrevia.visibility = View.GONE
                    habilitarBotones(true)
                    Toast.makeText(requireContext(), "Error: ${estado.mensaje}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun habilitarBotones(habilitados: Boolean) {
        binding.botonDescargarReporte.isEnabled = habilitados
        binding.botonCompartirReporte.isEnabled = habilitados
    }

    private fun mostrarSelectorDeFormato(accion: AccionArchivo) {
        val formatos = arrayOf(getString(R.string.formato_pdf), getString(R.string.formato_excel))
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.titulo_elegir_formato)
            .setItems(formatos) { _, which ->
                val formato = if (which == 0) "pdf" else "excel"
                viewModel.generarYProcesar(formato, accion)
            }
            .show()
    }

    // --- Armado de la tabla de vista previa -------------------------

    private fun armarTabla(productos: List<Producto>, columnas: List<String>) {
        binding.tablaVistaPrevia.removeAllViews()

        val filaEncabezado = TableRow(requireContext())
        columnas.forEach { campo -> filaEncabezado.addView(celda(etiquetaColumna(campo), negrita = true)) }
        binding.tablaVistaPrevia.addView(filaEncabezado)

        productos.forEach { producto ->
            val fila = TableRow(requireContext())
            columnas.forEach { campo -> fila.addView(celda(valorColumna(producto, campo))) }
            binding.tablaVistaPrevia.addView(fila)
        }
    }

    private fun celda(texto: String, negrita: Boolean = false): TextView {
        return TextView(requireContext()).apply {
            text = texto
            setPadding(20, 16, 20, 16)
            if (negrita) setTypeface(typeface, Typeface.BOLD)
        }
    }

    // Mismas claves que maneja el backend en COLUMNAS_LISTADO
    // (productos_api/listados.py); se mantiene mapeado a mano acá para
    // reutilizar los mismos strings que ya usan los checkboxes de
    // columnas en Crear Listado.
    private fun etiquetaColumna(campo: String): String = when (campo) {
        "nombre" -> getString(R.string.hint_nombre)
        "categoria_nombre" -> getString(R.string.label_categoria)
        "marca_nombre" -> getString(R.string.label_marca)
        "proveedor_nombre" -> getString(R.string.label_proveedor)
        "precio_unitario" -> getString(R.string.columna_precio_unitario)
        "porcentaje_ganancia" -> getString(R.string.columna_porcentaje_ganancia)
        "precio_venta" -> getString(R.string.columna_precio_venta)
        "stock" -> getString(R.string.columna_stock)
        else -> campo
    }

    private fun valorColumna(producto: Producto, campo: String): String = when (campo) {
        "nombre" -> producto.nombre
        "categoria_nombre" -> producto.categoria_nombre ?: ""
        "marca_nombre" -> producto.marca_nombre ?: ""
        "proveedor_nombre" -> producto.proveedor_nombre ?: ""
        "precio_unitario" -> producto.precio_unitario
        "porcentaje_ganancia" -> producto.porcentaje_ganancia
        "precio_venta" -> producto.precio_venta ?: ""
        "stock" -> producto.stock.toString()
        else -> ""
    }

    // --- Descargar / compartir el archivo ya generado ---------------

    /**
     * Guarda el archivo directo en la carpeta Descargas del celular vía
     * MediaStore (no pide ningún permiso en Android 10+). En versiones
     * anteriores no se puede escribir ahí sin pedir un permiso aparte,
     * así que en esos casos se abre el selector de compartir en su
     * lugar, que también permite elegir guardarlo.
     */
    private fun descargarArchivo(bytes: ByteArray, formato: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(
                requireContext(),
                "Tu versión de Android no permite descargar directo: elegí dónde guardarlo",
                Toast.LENGTH_LONG
            ).show()
            compartirArchivo(bytes, formato)
            return
        }

        val extension = if (formato == "excel") "xlsx" else "pdf"
        val mimeType = if (formato == "excel") {
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        } else {
            "application/pdf"
        }
        val marcaTiempo = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val nombreArchivo = "listado_productos_$marcaTiempo.$extension"

        try {
            val resolver = requireContext().contentResolver
            val valores = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, valores)
            if (uri == null) {
                Toast.makeText(requireContext(), "No se pudo guardar el reporte", Toast.LENGTH_LONG).show()
                return
            }
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
            Toast.makeText(
                requireContext(), "Reporte guardado en Descargas: $nombreArchivo", Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(), "No se pudo guardar el reporte: ${e.message}", Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Mismo mecanismo que antes vivía en CrearListadoFragment: guarda el
     * archivo en caché y abre el selector nativo de Android para
     * compartirlo/guardarlo con cualquier app, vía FileProvider (ver
     * AndroidManifest.xml y res/xml/file_paths.xml).
     */
    private fun compartirArchivo(bytes: ByteArray, formato: String) {
        val extension = if (formato == "excel") "xlsx" else "pdf"
        val mimeType = if (formato == "excel") {
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        } else {
            "application/pdf"
        }

        try {
            val carpeta = File(requireContext().cacheDir, "listados").apply { mkdirs() }
            val marcaTiempo = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val archivo = File(carpeta, "listado_productos_$marcaTiempo.$extension")
            archivo.writeBytes(bytes)

            val uri = FileProvider.getUriForFile(
                requireContext(), "${requireContext().packageName}.fileprovider", archivo
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.titulo_compartir_listado)))
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(), "No se pudo abrir el listado generado: ${e.message}", Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
