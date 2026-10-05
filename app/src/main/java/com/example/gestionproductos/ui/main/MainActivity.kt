package com.example.gestionproductos.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.gestionproductos.R
import com.example.gestionproductos.data.SessionManager
import com.example.gestionproductos.databinding.ActivityMainBinding
import com.example.gestionproductos.databinding.NavHeaderBinding
import com.example.gestionproductos.ui.login.LoginActivity
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    // Ítems que agrupan a los demás en el menú lateral (título tocable,
    // sin navegación propia) y los hijos que cada uno despliega/oculta.
    private val gruposDelMenu = mapOf(
        R.id.menuGrupoAdministracion to listOf(
            R.id.menuProductos, R.id.menuProveedores, R.id.menuMarcas, R.id.menuCategorias,
            R.id.menuFormasDePago
        ),
        R.id.menuGrupoOperaciones to listOf(
            R.id.menuOrdenes, R.id.menuGenerarOrden, R.id.menuVentas,
            R.id.menuRegistrarVenta, R.id.menuActualizacionMasiva
        ),
        R.id.menuGrupoReportes to listOf(
            R.id.menuCrearListados, R.id.menuEstadistica, R.id.menuEscanearListado
        )
    )

    // De esos hijos, los que además solo debe ver un Administrador (misma
    // restricción que ya existía antes de agrupar el menú). Formas de
    // pago (NUEVO, Fase 0b) sigue el mismo criterio que Marca/Categoría/
    // Proveedor: es un dato maestro que solo administra el Administrador.
    private val idsSoloAdministrador = setOf(
        R.id.menuProveedores, R.id.menuMarcas, R.id.menuCategorias, R.id.menuActualizacionMasiva,
        R.id.menuFormasDePago
    )

    // Estado de expandido/colapsado de cada grupo; arrancan todos
    // colapsados para que el menú se vea compacto al abrir el drawer.
    private val gruposExpandidos = mutableMapOf(
        R.id.menuGrupoAdministracion to false,
        R.id.menuGrupoOperaciones to false,
        R.id.menuGrupoReportes to false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(applicationContext)
        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.navigationView.setupWithNavController(navController)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.app_name, R.string.app_name
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        configurarEncabezadoYPermisos()
        configurarClicksDeMenu(navController)
        aplicarEstadoDelMenu()
    }

    private fun configurarEncabezadoYPermisos() {
        val headerView = binding.navigationView.getHeaderView(0)
        val headerBinding = NavHeaderBinding.bind(headerView)
        headerBinding.textUsuario.text = sessionManager.username() ?: ""
        headerBinding.textRol.text = sessionManager.rol() ?: ""
    }

    // Recalcula desde cero el título de cada grupo (con una flechita chica
    // como parte del texto, a la derecha del nombre; el ícono del grupo
    // en el XML ya es descriptivo y queda fijo, sin sangría) y la
    // visibilidad/sangría de sus hijos (el texto se corre hacia la
    // derecha; el ícono queda en su columna normal, ver nota abajo), a
    // partir de gruposExpandidos y del rol del usuario. Se llama al
    // iniciar y cada vez que se toca un grupo.
    //
    // Nota sobre el ícono de los hijos: se probó envolverlo en un
    // InsetDrawable para correrlo junto con el texto, pero NavigationView
    // fuerza todo ícono a un cuadro fijo chico (icon.setBounds a un
    // tamaño fijo) antes de dibujarlo; un margen ahí no lo desplaza, se
    // lo come del espacio visible y el ícono termina desapareciendo. Por
    // eso el ícono se deja como venía (sin sangría), y la sangría queda
    // solo en el texto.
    //
    // Se fuerza un rearmado completo del menú (clear + inflateMenu) en vez
    // de solo cambiar isVisible/title sobre los ítems existentes:
    // NavigationView no siempre refleja en pantalla un cambio hecho
    // después del primer dibujo si no se lo empuja a reconstruir su lista
    // de ítems, y este método es la forma confiable de lograrlo. Como se
    // vuelve a inflar el XML original en cada llamada, los títulos leídos
    // acá siempre son los "limpios" (sin flechita ni sangría todavía
    // agregada), así que no se van acumulando.
    private fun aplicarEstadoDelMenu() {
        binding.navigationView.menu.clear()
        binding.navigationView.inflateMenu(R.menu.drawer_menu)
        val menu = binding.navigationView.menu
        val esAdministrador = sessionManager.esAdministrador()

        gruposDelMenu.forEach { (grupoId, hijos) ->
            val expandido = gruposExpandidos[grupoId] ?: false
            val grupoItem = menu.findItem(grupoId)
            if (grupoItem != null) {
                val flecha = if (expandido) "▾" else "▸"
                grupoItem.title = "${grupoItem.title}  $flecha"
            }

            hijos.forEach { hijoId ->
                val visible = expandido && (hijoId !in idsSoloAdministrador || esAdministrador)
                val hijoItem = menu.findItem(hijoId)
                if (hijoItem != null) {
                    hijoItem.isVisible = visible
                    if (visible) {
                        hijoItem.title = "    ${hijoItem.title}"
                    }
                }
            }
        }
    }

    // Se llama al tocar el título de un grupo: alterna expandido/colapsado
    // y vuelve a aplicar el estado del menú. No navega a ningún lado ni
    // cierra el drawer, para que el usuario pueda tocar un ítem recién
    // revelado.
    private fun alternarGrupo(grupoId: Int) {
        gruposExpandidos[grupoId] = !(gruposExpandidos[grupoId] ?: false)
        aplicarEstadoDelMenu()
    }

    private fun configurarClicksDeMenu(navController: androidx.navigation.NavController) {
        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuGrupoAdministracion, R.id.menuGrupoOperaciones, R.id.menuGrupoReportes -> {
                    alternarGrupo(item.itemId)
                    true
                }
                R.id.menuInicio -> {
                    navController.navigate(R.id.inicioFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menuProductos -> {
                    navController.navigate(R.id.productoListFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menuActualizacionMasiva -> {
                    navController.navigate(R.id.actualizacionMasivaFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menuCategorias -> {
                    navController.navigate(R.id.categoriaListFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menuMarcas -> {
                    navController.navigate(R.id.marcaListFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menuProveedores -> {
                    navController.navigate(R.id.proveedorListFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menuFormasDePago -> {
                    navController.navigate(R.id.formaDePagoListFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menuCrearListados -> {
                    navController.navigate(R.id.crearListadoFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menuEstadistica -> {
                    navController.navigate(R.id.estadisticaFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menuOrdenes -> {
                    navegarAProximamente(navController, getString(R.string.menu_ordenes))
                    true
                }
                R.id.menuGenerarOrden -> {
                    navegarAProximamente(navController, getString(R.string.menu_generar_orden))
                    true
                }
                R.id.menuVentas -> {
                    navegarAProximamente(navController, getString(R.string.menu_ventas))
                    true
                }
                R.id.menuRegistrarVenta -> {
                    navegarAProximamente(navController, getString(R.string.menu_registrar_venta))
                    true
                }
                R.id.menuEscanearListado -> {
                    navegarAProximamente(navController, getString(R.string.menu_escanear_listado))
                    true
                }
                R.id.menuCerrarSesion -> {
                    sessionManager.cerrarSesion()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    // Usado por las entradas del menú "Operaciones" y "Escanear listado"
    // que todavía no tienen pantalla propia: navegan al mismo destino
    // stub (ProximamenteFragment), pasándole el título del ítem elegido.
    private fun navegarAProximamente(navController: androidx.navigation.NavController, titulo: String) {
        navController.navigate(
            R.id.proximamenteFragment,
            androidx.core.os.bundleOf("titulo" to titulo)
        )
        binding.drawerLayout.closeDrawers()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
            binding.drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}
