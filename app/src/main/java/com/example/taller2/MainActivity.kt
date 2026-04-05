package com.example.taller2

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.taller2.Activities.main.admin.AdminFragment
import com.example.taller2.Activities.main.admin.UsuariosFragment
import com.example.taller2.Activities.main.perfil.PerfilFragment
import com.example.taller2.Activities.main.productos.CarritoFragment
import com.example.taller2.Activities.main.productos.CatalogoFragment
import com.example.taller2.Activities.main.productos.FavoritosFragment
import com.example.taller2.Activities.main.productos.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layaut)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        //cargar fragmenst

        cargarFragment(HomeFragment())
        bottomNav.selectedItemId = R.id.nav_home

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
            }
            drawerLayout.closeDrawers()
            true
        }
    }
        private fun cargarFragment(fragment: Fragment) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

    }
}