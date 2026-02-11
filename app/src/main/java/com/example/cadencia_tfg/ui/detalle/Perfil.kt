package com.example.cadencia_tfg.ui.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions // <--- IMPORTANTE
import androidx.navigation.fragment.findNavController
import com.example.cadencia_tfg.R
import com.example.cadencia_tfg.databinding.FragmentPerfilBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarDatosUsuario()

        binding.btnCerrarSesion.setOnClickListener {
            cerrarSesionCompleta()
        }
    }

    private fun cargarDatosUsuario() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            binding.tvUserEmail.text = user.email
            binding.tvUserId.text = "ID: ${user.uid.take(5)}..."
        } else {
            binding.tvUserEmail.text = "Invitado"
        }
    }

    private fun cerrarSesionCompleta() {
        FirebaseAuth.getInstance().signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        googleSignInClient.signOut().addOnCompleteListener {

            val opcionesBorrado = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true) // Borra toda la pila de navegación
                .build()

            try {

                findNavController().navigate(R.id.login, null, opcionesBorrado)
            } catch (e: Exception) {
                Toast.makeText(context, "Error al navegar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}