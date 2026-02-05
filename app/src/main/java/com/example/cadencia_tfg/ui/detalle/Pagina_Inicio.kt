package com.example.cadencia_tfg.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.cadencia_tfg.R
import com.example.cadencia_tfg.data.remote.Habito
import com.example.cadencia_tfg.databinding.FragmentPaginaInicioBinding
import com.example.cadencia_tfg.viewmodel.HabitoViewModel
import java.util.Calendar

class Pagina_Inicio : Fragment() {

    private var _binding: FragmentPaginaInicioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HabitoViewModel by viewModels()
    private var listaCompletaHabitos: List<Habito> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaginaInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()

        // 1. Pedimos los datos al ViewModel
        viewModel.cargarHabitos()

        // 2. RECIBIMOS DATOS (Aquí estaba el problema)
        viewModel.listaHabitos.observe(viewLifecycleOwner) { habitosDescargados ->
            listaCompletaHabitos = habitosDescargados
            Log.d("Calendario", "Hábitos recibidos: ${habitosDescargados.size}")

            // --- SOLUCIÓN: FORZAR ACTUALIZACIÓN INICIAL ---
            // Cogemos la fecha que tiene el calendario seleccionada AHORA MISMO (por defecto es HOY)
            val fechaActualMilis = binding.calendarView.date

            // Calculamos qué día y mes son para pintar el texto
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = fechaActualMilis
            val dia = calendar.get(Calendar.DAY_OF_MONTH)
            val mes = calendar.get(Calendar.MONTH)

            // Filtramos los hábitos para esa fecha
            val habitosDeHoy = obtenerHabitosParaElDia(fechaActualMilis, listaCompletaHabitos)

            // Actualizamos la pantalla sin esperar a clics
            actualizarPantalla(habitosDeHoy, dia, mes)
        }

        // 3. CUANDO EL USUARIO TOCA UN DÍA
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Preparamos la fecha seleccionada (final del día para asegurar comparaciones)
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth, 23, 59, 59)
            val fechaSeleccionadaMilis = calendar.timeInMillis

            // Filtramos y mostramos
            val habitosDelDia =
                obtenerHabitosParaElDia(fechaSeleccionadaMilis, listaCompletaHabitos)
            actualizarPantalla(habitosDelDia, dayOfMonth, month)
        }
    }

    private fun actualizarPantalla(habitos: List<Habito>, dia: Int, mes: Int) {
        if (habitos.isEmpty()) {
            binding.tvListaHabitos.text = "Nada planeado para el $dia/${mes + 1}."
        } else {
            val texto = StringBuilder()
            texto.append("Hábitos para el $dia/${mes + 1}:\n\n")
            for (habito in habitos) {
                texto.append("✅ ${habito.nombre}\n")
            }
            binding.tvListaHabitos.text = texto.toString()
        }
    }

    private fun obtenerHabitosParaElDia(
        fechaSeleccionadaMilis: Long,
        listaTodos: List<Habito>
    ): List<Habito> {
        val resultado = mutableListOf<Habito>()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fechaSeleccionadaMilis

        // Obtenemos la letra del día (L, M, X...)
        val diaSemanaLetra = obtenerLetraDia(calendar.get(Calendar.DAY_OF_WEEK))

        for (habito in listaTodos) {
            // Pasamos a mayúsculas para evitar errores (l vs L)
            val diasDelHabito = habito.diasFrecuencia.map { it.uppercase() }

            if (diasDelHabito.contains(diaSemanaLetra)) {
                if (habito.esIndefinido) {
                    // Si es indefinido, solo importa que la fecha sea posterior al inicio
                    if (fechaSeleccionadaMilis >= habito.fechaInicio) {
                        resultado.add(habito)
                    }
                } else {
                    // Si tiene fin, debe estar dentro del rango
                    if (fechaSeleccionadaMilis >= habito.fechaInicio && fechaSeleccionadaMilis <= habito.fechaFin) {
                        resultado.add(habito)
                    }
                }
            }
        }
        return resultado
    }

    private fun obtenerLetraDia(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.MONDAY -> "L"
            Calendar.TUESDAY -> "M"
            Calendar.WEDNESDAY -> "X"
            Calendar.THURSDAY -> "J"
            Calendar.FRIDAY -> "V"
            Calendar.SATURDAY -> "S"
            Calendar.SUNDAY -> "D"
            else -> "?"
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_toolbar_home, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add_habito -> {
                        try {
                            findNavController().navigate(R.id.action_pagina_Inicio_to_fragment_nuevo_habito)
                        } catch (e: Exception) {
                        }
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}