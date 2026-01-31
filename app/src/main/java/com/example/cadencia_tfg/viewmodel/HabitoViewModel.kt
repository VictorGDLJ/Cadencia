package com.example.cadencia_tfg.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cadencia_tfg.data.remote.Habito
import com.example.cadencia_tfg.data.repository.HabitoRepository

class HabitoViewModel : ViewModel() {

    private val repository = HabitoRepository()

    private val _listaHabitos = MutableLiveData<List<Habito>>()
    val listaHabitos: LiveData<List<Habito>> = _listaHabitos

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    fun cargarHabitos() {
        repository.obtenerHabitos { habitos ->
            _listaHabitos.value = habitos
        }
    }

    fun crearHabito(nombre: String, descripcion: String) {
        val nuevoHabito = Habito(nombre = nombre, descripcion = descripcion)

        repository.guardarHabito(nuevoHabito,
            onSuccess = {
                _mensaje.value = "Hábito guardado correctamente"
                cargarHabitos() // Recargamos la lista automáticamente
            },
            onFailure = {
                _mensaje.value = "Error: ${it.message}"
            }
        )
    }
}