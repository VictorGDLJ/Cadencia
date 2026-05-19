package com.example.cadencia_tfg.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cadencia_tfg.R
import com.example.cadencia_tfg.data.remote.Habito
import com.google.firebase.firestore.FirebaseFirestore

class HabitoAdapter(
    private var listaHabitos: List<Habito>,
    private val onAbrirCamara: (Habito) -> Unit,
    private val onEditarHabito: (Habito) -> Unit
) : RecyclerView.Adapter<HabitoAdapter.HabitoViewHolder>() {

    private var fechaSeleccionada: String = ""
    private val db = FirebaseFirestore.getInstance()

    class HabitoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreHabito)
        val cbCompletado: CheckBox = view.findViewById(R.id.cbCompletado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habito, parent, false)
        return HabitoViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitoViewHolder, position: Int) {
        val habito = listaHabitos[position]
        holder.tvNombre.text = habito.nombre

        holder.cbCompletado.setOnCheckedChangeListener(null)
        holder.cbCompletado.isChecked = false

        holder.itemView.setOnLongClickListener {
            onEditarHabito(habito)
            true
        }

        if (fechaSeleccionada.isNotEmpty() && habito.id.isNotEmpty()) {
            val registroRef = db.collection("habitos").document(habito.id)
                .collection("registros").document(fechaSeleccionada)

            registroRef.get().addOnSuccessListener { document ->
                holder.cbCompletado.isChecked = document.exists()

                holder.cbCompletado.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.isPressed) {
                        if (isChecked) {
                            onAbrirCamara(habito)
                        } else {
                            registroRef.delete()
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = listaHabitos.size

    fun actualizarLista(nuevaLista: List<Habito>, nuevaFecha: String) {
        listaHabitos = nuevaLista
        fechaSeleccionada = nuevaFecha
        notifyDataSetChanged()
    }
}