package com.example.cadencia_tfg.ui.detalle

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.cadencia_tfg.databinding.FragmentNuevoHabitoBinding
import com.example.cadencia_tfg.viewmodel.HabitoViewModel
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// He cambiado el nombre de la clase a PascalCase (NuevoHabitoFragment)
// Es la convención correcta en Kotlin.
class NuevoHabitoFragment : Fragment() {

    // 1. Configuración del Binding (Para acceder a los elementos visuales)
    private var _binding: FragmentNuevoHabitoBinding? = null
    private val binding get() = _binding!!

    private var fechaInicioMilis: Long = System.currentTimeMillis()
    private var fechaFinMilis: Long = 0L

    // 2. Conexión con el ViewModel (El cerebro que guarda los datos)
    private val viewModel: HabitoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflamos el layout usando Binding
        _binding = FragmentNuevoHabitoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 3. Configurar el botón de GUARDAR
        binding.btnGuardar.setOnClickListener {
            guardarHabito()
        }




        binding.switchIndefinido.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.layoutFechas.visibility = View.GONE
            } else {
                binding.layoutFechas.visibility = View.VISIBLE
                // Pre-rellenar fecha inicio con hoy si está vacía
                if (binding.etFechaInicio.text.isNullOrEmpty()) {
                    actualizarInputFecha(binding.etFechaInicio, fechaInicioMilis)
                }
            }
        }

        // 2. CLICK EN FECHA INICIO
        binding.etFechaInicio.setOnClickListener {
            mostrarDatePicker { timestamp ->
                fechaInicioMilis = timestamp
                actualizarInputFecha(binding.etFechaInicio, timestamp)
            }
        }

        // 3. CLICK EN FECHA FIN
        binding.etFechaFin.setOnClickListener {
            mostrarDatePicker { timestamp ->
                fechaFinMilis = timestamp
                actualizarInputFecha(binding.etFechaFin, timestamp)
            }
        }

        // 4. GUARDAR
        binding.btnGuardar.setOnClickListener {
            guardarHabito()
        }
    }

    private fun guardarHabito() {
        val nombre = binding.etNombre.text.toString()
        val descripcion = binding.etDescripcion.text.toString()
        val esIndefinido = binding.switchIndefinido.isChecked

        // Días seleccionados (tu código de los Chips)
        val diasSeleccionados = mutableListOf<String>()
        val idsChips = binding.chipGroupDias.checkedChipIds
        for (id in idsChips) {
            val chip = binding.root.findViewById<Chip>(id)
            diasSeleccionados.add(chip.text.toString()) // Asegúrate de que el texto sea "Lunes", "Martes", etc.
        }

        // Validaciones
        if (nombre.isEmpty() || diasSeleccionados.isEmpty()) {
            Toast.makeText(context, "Faltan datos", Toast.LENGTH_SHORT).show()
            return
        }

        if (!esIndefinido && fechaFinMilis < fechaInicioMilis) {
            Toast.makeText(context, "La fecha fin no puede ser antes de la de inicio", Toast.LENGTH_SHORT).show()
            return
        }

        // ENVIAR AL VIEWMODEL
        viewModel.crearHabito(
            nombre = nombre,
            descripcion = descripcion,
            diasFrecuencia = diasSeleccionados,
            esIndefinido = esIndefinido,
            fechaInicio = fechaInicioMilis,
            fechaFin = if (esIndefinido) 0L else fechaFinMilis
        )

        findNavController().popBackStack()
    }

    private fun mostrarDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun actualizarInputFecha(editText: EditText, millis: Long) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        editText.setText(sdf.format(millis))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}