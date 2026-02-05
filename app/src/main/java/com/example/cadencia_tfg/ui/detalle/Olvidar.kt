package com.example.cadencia_tfg.ui.detalle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cadencia_tfg.databinding.FragmentOlvidarBinding
import com.google.firebase.auth.FirebaseAuth

class Olvidar : Fragment() {

    private var _binding: FragmentOlvidarBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOlvidarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón Enviar
        binding.btnRecuperar.setOnClickListener {
            val email = binding.etEmailRecuperacion.text.toString().trim()

            if (email.isNotEmpty()) {
                enviarCorreoRecuperacion(email)
            } else {
                Toast.makeText(context, "Por favor, escribe tu correo", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Volver
        binding.btnVolver.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun enviarCorreoRecuperacion(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Correo enviado. Revisa tu bandeja de entrada (y spam).", Toast.LENGTH_LONG).show()
                    // Volvemos al Login automáticamente para que el usuario inicie sesión
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}