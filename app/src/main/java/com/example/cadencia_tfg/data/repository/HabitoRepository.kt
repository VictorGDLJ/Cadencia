package com.example.cadencia_tfg.data.repository


import androidx.room.util.copy
import com.example.cadencia_tfg.data.remote.Habito
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HabitoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun guardarHabito(habito: Habito, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        val docRef = if (habito.id.isEmpty()) {
            db.collection("users").document(uid).collection("habitos").document()
        } else {
            db.collection("users").document(uid).collection("habitos").document(habito.id)
        }

        val habitoConId = habito.copy(id = docRef.id)

        docRef.set(habitoConId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun obtenerHabitos(onResult: (List<Habito>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).collection("habitos")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.toObjects(Habito::class.java)
                onResult(lista)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}