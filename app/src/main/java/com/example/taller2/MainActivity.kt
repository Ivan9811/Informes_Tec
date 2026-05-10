package com.example.taller2

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.taller2.Activities.Auth.LoginActivity
import com.example.taller2.Activities.main.admin.AdminFragment
import com.example.taller2.Activities.main.admin.UsuariosFragment
import com.example.taller2.Activities.main.perfil.PerfilFragment
import com.example.taller2.Activities.main.productos.CarritoFragment
import com.example.taller2.Activities.main.productos.CatalogoFragment
import com.example.taller2.Activities.main.productos.FavoritosFragment
import com.example.taller2.Activities.main.productos.HomeFragment
import com.example.taller2.data.UsuarioRepository
import com.example.taller2.proccess.SupabaseClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layaut) // 👈 tu ID tiene este nombre con typo
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.fondo_inicio) // color igual que nativas

        cargarFragment(HomeFragment())
        bottomNav.selectedItemId = R.id.nav_home

        configurarMenuPorRol(navView.menu) // 👈 control de visibilidad por rol

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> cargarFragment(HomeFragment())
                R.id.nav_catalogo -> cargarFragment(CatalogoFragment())
                R.id.nav_carrito -> cargarFragment(CarritoFragment())
                R.id.nav_perfil -> cargarFragment(PerfilFragment())
            }
            true
        }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_favoritos -> cargarFragment(FavoritosFragment())
                R.id.nav_administrar -> cargarFragment(AdminFragment())
                R.id.nav_usuarios -> cargarFragment(UsuariosFragment())
                R.id.nav_logout -> cerrarSesion() // 👈 cierre de sesión
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun configurarMenuPorRol(menu: Menu) {
        lifecycleScope.launch {
            val rol = UsuarioRepository.obtenerRolActual()
            android.util.Log.d("DEBUG_ROL", "Rol obtenido: $rol")

            runOnUiThread {
                when (rol) {
                    "admin" -> {
                        menu.findItem(R.id.nav_administrar).isVisible = true
                        menu.findItem(R.id.nav_usuarios).isVisible = true
                    }
                    "vendedor" -> {
                        menu.findItem(R.id.nav_administrar).isVisible = true
                        menu.findItem(R.id.nav_usuarios).isVisible = false
                    }
                    else -> {
                        menu.findItem(R.id.nav_administrar).isVisible = false
                        menu.findItem(R.id.nav_usuarios).isVisible = false
                    }
                }
            }
        }
    }

    private fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun cerrarSesion() {
        lifecycleScope.launch {
            try {
                //Supabase conectado - cierre de sesión real
                SupabaseClient.client.auth.signOut()

                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finishAffinity()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error al cerrar sesión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}