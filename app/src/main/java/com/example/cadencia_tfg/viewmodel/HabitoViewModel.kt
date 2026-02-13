package com.example.cadencia_tfg.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cadencia_tfg.data.remote.Habito
import com.example.cadencia_tfg.data.repository.HabitoRepository

class HabitoViewModel : ViewModel() {

    private val repository = HabitoRepository()

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    private val _listaHabitos = MutableLiveData<List<Habito>>()
    val listaHabitos: LiveData<List<Habito>> = _listaHabitos

    init {
        cargarHabitos()
    }
    // --------------------------------

    /**
     * Función para crear hábitos
     */
    fun crearHabito(
        nombre: String,
        descripcion: String,
        diasFrecuencia: List<String>,
        esIndefinido: Boolean,
        fechaInicio: Long,
        fechaFin: Long
    ) {
        val nuevoHabito = Habito(
            nombre = nombre,
            descripcion = descripcion,
            diasFrecuencia = diasFrecuencia,
            esIndefinido = esIndefinido,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            fechaCreacion = System.currentTimeMillis()
        )

        repository.guardarHabito(
            nuevoHabito,
            onSuccess = {
                _mensaje.value = "Hábito guardado correctamente"
            },
            onFailure = { error ->
                _mensaje.value = "Error al guardar: ${error.message}"
            }
        )
    }

    /**
     * Función que activa la escucha en tiempo real (Offline first)
     */
    fun cargarHabitos() {
        repository.obtenerHabitos { habitos ->
            _listaHabitos.value = habitos
        }
    }
}