package com.example.cadencia_tfg.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cadencia_tfg.data.remote.Habito
import com.example.cadencia_tfg.data.repository.HabitoRepository

class HabitoViewModel : ViewModel() {

    private val repository = HabitoRepository()

    // LiveData para comunicar mensajes (éxito o error) a la vista
    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    // LiveData para la lista de hábitos (cuando los carguemos en el calendario)
    private val _listaHabitos = MutableLiveData<List<Habito>>()
    val listaHabitos: LiveData<List<Habito>> = _listaHabitos

    /**
     * Función actualizada para crear hábitos con fechas y duración
     */
    fun crearHabito(
        nombre: String,
        descripcion: String,
        diasFrecuencia: List<String>,
        esIndefinido: Boolean,
        fechaInicio: Long,
        fechaFin: Long
    ) {
        // Creamos el objeto Habito con TODOS los nuevos campos
        val nuevoHabito = Habito(
            nombre = nombre,
            descripcion = descripcion,
            diasFrecuencia = diasFrecuencia,
            esIndefinido = esIndefinido,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            fechaCreacion = System.currentTimeMillis()
        )

        // Llamamos al repositorio para guardar en Firebase
        repository.guardarHabito(nuevoHabito,
            onSuccess = {
                _mensaje.value = "Hábito guardado correctamente"
                // Opcional: Recargar la lista si estuviéramos en la pantalla de lista
            },
            onFailure = { error ->
                _mensaje.value = "Error al guardar: ${error.message}"
            }
        )
    }


    fun cargarHabitos() {
        repository.obtenerHabitos { habitos ->
            _listaHabitos.value = habitos
        }
    }
}