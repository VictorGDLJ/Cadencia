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

    fun cargarHabitos() {
        repository.obtenerHabitos { habitos ->
            _listaHabitos.value = habitos
        }
    }

    fun actualizarHabito(
        id: String,
        nombre: String,
        descripcion: String,
        diasFrecuencia: List<String>,
        esIndefinido: Boolean,
        fechaInicio: Long,
        fechaFin: Long
    ) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        val datosActualizados = hashMapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "diasFrecuencia" to diasFrecuencia,
            "esIndefinido" to esIndefinido,
            "fechaInicio" to fechaInicio,
            "fechaFin" to fechaFin
        )

        db.collection("habitos").document(id)
            .update(datosActualizados as Map<String, Any>)
            .addOnSuccessListener {
                _mensaje.value = "Hábito actualizado con éxito"
                cargarHabitos()
            }
            .addOnFailureListener { error ->
                _mensaje.value = "Error al actualizar: ${error.message}"
            }
    }

    fun eliminarHabito(id: String) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        db.collection("habitos").document(id)
            .delete()
            .addOnSuccessListener {
                _mensaje.value = "Hábito eliminado correctamente"
                cargarHabitos() // Refrescamos la lista para que desaparezca al instante
            }
            .addOnFailureListener { error ->
                _mensaje.value = "Error al eliminar: ${error.message}"
            }
    }
}