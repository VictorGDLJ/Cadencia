package com.example.cadencia_tfg.data.repository

import android.util.Log
import com.example.cadencia_tfg.data.remote.Habito
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HabitoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun guardarHabito(habito: Habito, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        val nuevoHabito = habito.copy(userId = userId)

        db.collection("habitos")
            .add(nuevoHabito)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun obtenerHabitos(onDatosRecibidos: (List<Habito>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onDatosRecibidos(emptyList())
            return
        }

        db.collection("habitos")
            .whereEqualTo("userId", userId)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Repository", "Error escuchando hábitos", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val lista = snapshot.toObjects(Habito::class.java)
                    onDatosRecibidos(lista)

                    val origen = if (snapshot.metadata.isFromCache) "CACHÉ LOCAL" else "SERVIDOR"
                    Log.d("Repository", "Datos recibidos desde: $origen")
                }
            }
    }
}