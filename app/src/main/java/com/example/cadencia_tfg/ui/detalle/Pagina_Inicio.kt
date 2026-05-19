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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

import com.example.cadencia_tfg.BuildConfig

class Pagina_Inicio : Fragment() {

    private var _binding: FragmentPaginaInicioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HabitoViewModel by viewModels()

    private lateinit var adapter: HabitoAdapter
    private var listaCompletaHabitos: List<Habito> = emptyList()

    // Nueva variable para controlar los hábitos del día seleccionado y calcular la barra
    private var habitosActualesEnPantalla: List<Habito> = emptyList()

    private var fechaSeleccionadaFormateada: String = ""

    private var habitoPendienteDeVerificar: Habito? = null

    val takePictureLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            Log.d("Camara", "¡Foto capturada con éxito!")
            habitoPendienteDeVerificar?.let { habito ->
                analizarFotoConIA(bitmap, habito)
            }
        } else {
            Log.d("Camara", "Cámara cancelada")
            adapter.notifyDataSetChanged()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            android.widget.Toast.makeText(requireContext(), "Se necesita la cámara para verificar", android.widget.Toast.LENGTH_SHORT).show()
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaginaInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, insets.top, 0, 0)
            androidx.core.view.WindowInsetsCompat.CONSUMED
        }

        setupMenu()

        binding.rvHabitos.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        adapter = HabitoAdapter(
            listaHabitos = emptyList(),
            onAbrirCamara = { habitoClickeado ->
                habitoPendienteDeVerificar = habitoClickeado
                verificarPermisoCamaraYAbrir()
            },
            onEditarHabito = { habitoClickeado ->
                // Creamos una "mochila" con todos los datos del hábito
                val bundle = android.os.Bundle().apply {
                    putString("id", habitoClickeado.id)
                    putString("nombre", habitoClickeado.nombre)
                    putString("descripcion", habitoClickeado.descripcion)
                    putBoolean("esIndefinido", habitoClickeado.esIndefinido)
                    putLong("fechaInicio", habitoClickeado.fechaInicio)
                    putLong("fechaFin", habitoClickeado.fechaFin)
                    putStringArrayList("dias", ArrayList(habitoClickeado.diasFrecuencia))
                }
                // Viajamos a la pantalla enviando la mochila
                try {
                    findNavController().navigate(R.id.action_pagina_Inicio_to_fragment_nuevo_habito, bundle)
                } catch (e: Exception) {
                    android.util.Log.e("Navegacion", "Fallo al navegar a edición: ${e.message}")
                }
            }
        )
        binding.rvHabitos.adapter = adapter

        viewModel.cargarHabitos()

        viewModel.listaHabitos.observe(viewLifecycleOwner) { habitosDescargados ->
            listaCompletaHabitos = habitosDescargados
            Log.d("Calendario", "Hábitos recibidos: ${habitosDescargados.size}")

            val fechaActualMilis = binding.calendarView.date

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = fechaActualMilis
            val dia = calendar.get(Calendar.DAY_OF_MONTH)
            val mes = calendar.get(Calendar.MONTH)
            val anio = calendar.get(Calendar.YEAR)

            val habitosDeHoy = obtenerHabitosParaElDia(fechaActualMilis, listaCompletaHabitos)

            actualizarPantalla(habitosDeHoy, dia, mes, anio)
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth, 23, 59, 59)
            val fechaSeleccionadaMilis = calendar.timeInMillis

            val habitosDelDia = obtenerHabitosParaElDia(fechaSeleccionadaMilis, listaCompletaHabitos)

            actualizarPantalla(habitosDelDia, dayOfMonth, month, year)
        }
    }

    private fun actualizarPantalla(habitos: List<Habito>, dia: Int, mes: Int, anio: Int) {
        val fechaFormateada = "$anio-${mes + 1}-$dia"
        fechaSeleccionadaFormateada = fechaFormateada

        habitosActualesEnPantalla = habitos

        if (habitos.isEmpty()) {
            binding.tvEstadoVacio.text = "Nada planeado para el $dia/${mes + 1}."
            binding.tvEstadoVacio.visibility = View.VISIBLE
            binding.rvHabitos.visibility = View.GONE

            actualizarBarraDeProgreso()
        } else {
            binding.tvEstadoVacio.visibility = View.GONE
            binding.rvHabitos.visibility = View.VISIBLE

            // Cargamos la lista inicialmente
            adapter.actualizarLista(habitos, fechaFormateada)

            // Le pedimos a Firebase que nos diga cuáles están completados hoy
            comprobarHabitosCompletadosEnFirebase(habitos, fechaFormateada)
        }
    }

    // NUEVA FUNCIÓN: Pregunta a Firebase por el historial del día
    private fun comprobarHabitosCompletadosEnFirebase(habitos: List<Habito>, fecha: String) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        var verificacionesCompletadas = 0

        for (habito in habitos) {
            db.collection("habitos").document(habito.id)
                .collection("registros").document(fecha)
                .get()
                .addOnSuccessListener { document ->
                    // Si el documento existe y dice true, el hábito está completado hoy
                    habito.completado = document.exists() && document.getBoolean("completado") == true

                    verificacionesCompletadas++

                    // Cuando termine de comprobar el último hábito de la lista...
                    if (verificacionesCompletadas == habitos.size) {
                        actualizarBarraDeProgreso() // Recalcula el % real
                        adapter.notifyDataSetChanged() // Refresca los colores/checks en pantalla
                    }
                }
                .addOnFailureListener {
                    habito.completado = false
                    verificacionesCompletadas++

                    if (verificacionesCompletadas == habitos.size) {
                        actualizarBarraDeProgreso()
                        adapter.notifyDataSetChanged()
                    }
                }
        }
    }

    private fun actualizarBarraDeProgreso() {
        if (habitosActualesEnPantalla.isEmpty()) {
            binding.pbProgresoDiario.progress = 0
            return
        }

        val completados = habitosActualesEnPantalla.count { it.completado }
        val total = habitosActualesEnPantalla.size
        val porcentaje = (completados * 100) / total

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            binding.pbProgresoDiario.setProgress(porcentaje, true)
        } else {
            binding.pbProgresoDiario.progress = porcentaje
        }
    }

    private fun obtenerHabitosParaElDia(
        fechaSeleccionadaMilis: Long,
        listaTodos: List<Habito>
    ): List<Habito> {
        val resultado = mutableListOf<Habito>()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fechaSeleccionadaMilis

        val diaSemanaLetra = obtenerLetraDia(calendar.get(Calendar.DAY_OF_WEEK))

        for (habito in listaTodos) {
            val diasDelHabito = habito.diasFrecuencia.map { it.uppercase() }

            if (diasDelHabito.contains(diaSemanaLetra)) {
                if (habito.esIndefinido) {
                    if (fechaSeleccionadaMilis >= habito.fechaInicio) {
                        resultado.add(habito)
                    }
                } else {
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

    private fun verificarPermisoCamaraYAbrir() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            takePictureLauncher.launch(null)
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun analizarFotoConIA(bitmap: android.graphics.Bitmap, habito: Habito) {
        android.widget.Toast.makeText(requireContext(), "Iniciando análisis (v2.0)...", android.widget.Toast.LENGTH_SHORT).show()

        val requestOptions = com.google.ai.client.generativeai.type.RequestOptions(
            apiVersion = "v1beta"
        )

        val generativeModel = com.google.ai.client.generativeai.GenerativeModel(
            modelName = "gemini-flash-latest",
            apiKey = com.example.cadencia_tfg.BuildConfig.GEMINI_API_KEY,
            requestOptions = requestOptions
        )

        lifecycleScope.launch {
            try {
                val prompt = """Eres un verificador automático de hábitos.
    Analiza esta imagen y responde ÚNICAMENTE con la palabra "SI" o la palabra "NO".
    ¿Hay pruebas visuales claras en esta foto de que se está cumpliendo el hábito: '${habito.nombre}'?
    No añadas ninguna explicación ni ninguna otra palabra.
""".trimIndent()
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )

                val respuestaIA = response.text?.trim()?.uppercase() ?: ""
                Log.d("GeminiIA", "Respuesta: $respuestaIA")

                if (respuestaIA.contains("SI")) {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val registroRef = db.collection("habitos").document(habito.id)
                        .collection("registros").document(fechaSeleccionadaFormateada)

                    val datos = hashMapOf(
                        "completado" to true,
                        "timestamp" to System.currentTimeMillis()
                    )

                    registroRef.set(datos).addOnSuccessListener {
                        android.widget.Toast.makeText(requireContext(), "¡Verificado con éxito!", android.widget.Toast.LENGTH_SHORT).show()

                        // Actualizamos el estado interno del hábito
                        habito.completado = true
                        adapter.notifyDataSetChanged()

                        // RECALCULAMOS LA BARRA AUTOMÁTICAMENTE
                        actualizarBarraDeProgreso()
                    }
                } else {
                    android.widget.Toast.makeText(requireContext(), "Hábito no detectado", android.widget.Toast.LENGTH_SHORT).show()
                    habito.completado = false
                    adapter.notifyDataSetChanged()

                    actualizarBarraDeProgreso()
                }

            } catch (e: Exception) {
                Log.e("GeminiIA", "Fallo con modelo 2.0", e)
                android.widget.Toast.makeText(requireContext(), "Error de conexión", android.widget.Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}