package com.example.cadencia_tfg.ui.detalle

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cadencia_tfg.R
import com.example.cadencia_tfg.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class Perfil : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.ivFotoPerfil.setPadding(0, 0, 0, 0)
            binding.ivFotoPerfil.imageTintList = null
            binding.ivFotoPerfil.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.ivFotoPerfil.setImageURI(uri)

            subirFotoAFirebase(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivFotoPerfil.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        val usuarioActual = FirebaseAuth.getInstance().currentUser

        if (usuarioActual != null) {
            binding.tvEmailUsuario.text = usuarioActual.email
            cargarFotoDesdeFirebase()
            cargarEstadisticasReales() // Llamamos a las estadísticas
        } else {
            binding.tvEmailUsuario.text = "Usuario no invitado"
        }

        binding.btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
            try {
                findNavController().navigate(R.id.action_menu_perfil_to_login)
            } catch (e: Exception) {
                Log.e("Perfil", "Error al navegar al login: ${e.message}")
            }
        }
    }

    private fun cargarEstadisticasReales() {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        val calendar = java.util.Calendar.getInstance()
        val dia = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        val mes = calendar.get(java.util.Calendar.MONTH)
        val anio = calendar.get(java.util.Calendar.YEAR)
        val fechaHoy = "$anio-${mes + 1}-$dia"

        db.collection("habitos").get()
            .addOnSuccessListener { snapshot ->
                val totalHabitos = snapshot.size()
                binding.tvTotalHabitos.text = totalHabitos.toString()

                if (totalHabitos == 0) {
                    binding.tvRacha.text = "0"
                    return@addOnSuccessListener
                }

                var completadosHoy = 0
                var comprobacionesTerminadas = 0

                for (document in snapshot.documents) {
                    db.collection("habitos").document(document.id)
                        .collection("registros").document(fechaHoy)
                        .get()
                        .addOnSuccessListener { docRegistro ->
                            if (docRegistro.exists() && docRegistro.getBoolean("completado") == true) {
                                completadosHoy++
                            }

                            comprobacionesTerminadas++
                            if (comprobacionesTerminadas == totalHabitos) {
                                binding.tvRacha.text = completadosHoy.toString()
                            }
                        }
                        .addOnFailureListener {
                            comprobacionesTerminadas++
                            if (comprobacionesTerminadas == totalHabitos) {
                                binding.tvRacha.text = completadosHoy.toString()
                            }
                        }
                }
            }
            .addOnFailureListener {
                Log.e("Perfil", "No se pudieron cargar las estadísticas", it)
            }
    }

    private fun subirFotoAFirebase(fileUri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        Toast.makeText(requireContext(), "Subiendo foto...", Toast.LENGTH_SHORT).show()

        val storageRef = FirebaseStorage.getInstance().reference
            .child("fotosPerfil/${user.uid}.jpg")

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Foto guardada con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al subir la foto", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarFotoDesdeFirebase() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        val storageRef = FirebaseStorage.getInstance().reference
            .child("fotosPerfil/${user.uid}.jpg")

        storageRef.downloadUrl.addOnSuccessListener { uri ->
            com.bumptech.glide.Glide.with(requireContext())
                .load(uri)
                .into(binding.ivFotoPerfil) // Actualizado el ID

            binding.ivFotoPerfil.setPadding(0, 0, 0, 0)
            binding.ivFotoPerfil.imageTintList = null
            binding.ivFotoPerfil.scaleType = ImageView.ScaleType.CENTER_CROP
        }.addOnFailureListener {
            Log.d("Perfil", "El usuario aún no tiene foto de perfil")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}