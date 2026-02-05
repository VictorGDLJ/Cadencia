package com.example.cadencia_tfg.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cadencia_tfg.R
import com.example.cadencia_tfg.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. ESTA ES LA LÍNEA MÁGICA QUE TE FALTA
        setSupportActionBar(binding.toolbar)
        // Ahora Android sabe que esa barra morada debe recibir los menús (el +)

        // 2. Configuración de navegación (Igual que antes)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        // 3. Control de visibilidad de las barras
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.login -> {
                    binding.toolbar.visibility = View.GONE
                    binding.bottomNavigation.visibility = View.GONE
                }
                else -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.bottomNavigation.visibility = View.VISIBLE

                    // Opcional: Quitar el título automático para que no choque con el tuyo
                    supportActionBar?.setDisplayShowTitleEnabled(false)
                }
            }
        }
    }
}