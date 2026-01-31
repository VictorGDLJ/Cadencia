package com.example.cadencia_tfg.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cadencia_tfg.R
import com.example.cadencia_tfg.databinding.FragmentPaginaInicioBinding

class Pagina_Inicio : Fragment() {

    private var _binding: FragmentPaginaInicioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaginaInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->

            val fechaSeleccionada = "$dayOfMonth/${month + 1}/$year"

            Toast.makeText(context, "Fecha: $fechaSeleccionada", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}