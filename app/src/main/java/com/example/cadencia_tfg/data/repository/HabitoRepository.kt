package com.example.cadencia_tfg.data.repository

import com.example.cadencia_tfg.data.remote.Habito
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HabitoRepository {

    // Instancias de Firebase
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // FUNCIÓN PARA GUARDAR
    fun guardarHabito(habito: Habito, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid // 1. Obtenemos el ID del usuario logueado

        if (userId != null) {
            // 2. Creamos una referencia en SU carpeta personal
            val nuevoDocRef = db.collection("users").document(userId).collection("habitos").document()

            // 3. Le asignamos ese ID al hábito para poder borrarlo o editarlo luego
            val habitoConId = habito.copy(id = nuevoDocRef.id)

            // 4. Guardamos en la nube
            nuevoDocRef.set(habitoConId)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        } else {
            onFailure(Exception("Usuario no identificado (No hay sesión iniciada)"))
        }
    }

    // FUNCIÓN PARA LEER (Solo sus hábitos)
    fun obtenerHabitos(onResult: (List<Habito>) -> Unit) {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId).collection("habitos")
                .get()
                .addOnSuccessListener { result ->
                    // Convertimos los documentos de Firebase a objetos Habito
                    val lista = result.toObjects(Habito::class.java)
                    onResult(lista)
                }
                .addOnFailureListener {
                    onResult(emptyList()) // Si falla, devolvemos lista vacía
                }
        } else {
            onResult(emptyList())
        }
    }
}