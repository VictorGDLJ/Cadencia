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
import com.example.cadencia_tfg.databinding.FragmentPerfilBinding
import androidx.navigation.fragment.findNavController
import com.example.cadencia_tfg.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class Perfil : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.ivFotoUsuario.setPadding(0, 0, 0, 0)
            binding.ivFotoUsuario.imageTintList = null
            binding.ivFotoUsuario.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.ivFotoUsuario.setImageURI(uri)

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

        binding.cvFotoPerfil.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        val usuarioActual = FirebaseAuth.getInstance().currentUser

        if (usuarioActual != null) {
            binding.tvUserEmail.text = usuarioActual.email
            cargarFotoDesdeFirebase()
        } else {
            binding.tvUserEmail.text = "Usuario no invitado"
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
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return

        // Buscamos la ruta exacta donde guardamos la foto antes
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
            .child("fotosPerfil/${user.uid}.jpg")

        // Pedimos la URL de descarga
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            // Si la encuentra, usamos Glide para pegarla en el círculo
            com.bumptech.glide.Glide.with(requireContext())
                .load(uri)
                .into(binding.ivFotoUsuario)

            // Ajustamos el diseño igual que cuando la elegimos de la galería
            binding.ivFotoUsuario.setPadding(0, 0, 0, 0)
            binding.ivFotoUsuario.imageTintList = null
            binding.ivFotoUsuario.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP

        }.addOnFailureListener {
            // Si entra aquí es porque el usuario es nuevo y aún no ha subido ninguna foto.
            // No hacemos nada y dejamos el icono del calendario por defecto.
            Log.d("Perfil", "El usuario aún no tiene foto de perfil")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}