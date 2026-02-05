package com.example.cadencia_tfg.ui.detalle

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cadencia_tfg.R
import com.example.cadencia_tfg.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.recaptcha.Recaptcha
import com.google.android.recaptcha.RecaptchaAction
import com.google.android.recaptcha.RecaptchaClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class Login : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Cliente de reCAPTCHA
    private lateinit var recaptchaClient: RecaptchaClient

    // Cliente de Google
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Analytics
        val analytics = FirebaseAnalytics.getInstance(requireContext())
        analytics.logEvent("InitScreen", Bundle().apply { putString("message", "Login Iniciado") })

        // 1. Inicializar reCAPTCHA (Para correo y contraseña)
        inicializarRecaptcha()

        // 2. Inicializar Google Sign-In
        configurarGoogleSignIn()

        setup()
    }

    // --- CONFIGURACIÓN DE GOOGLE ---
    private fun configurarGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Esto lo coge solo del google-services.json
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    // Este lanzador gestiona el resultado de cuando vuelves de seleccionar tu cuenta de Google
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Google ha dicho que sí, ahora autenticamos en Firebase
                val account = task.getResult(ApiException::class.java)
                autenticarConFirebaseGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(context, "Fallo Google: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun autenticarConFirebaseGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "¡Login con Google exitoso!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_login_to_pagina_Inicio)
                } else {
                    showAlert("Error al conectar con Firebase mediante Google.")
                }
            }
    }
    // --------------------------------

    private fun inicializarRecaptcha() {
        lifecycleScope.launch {
            try {
                // TU CLAVE RECAPTCHA
                val siteKey = "6LcwoVssAAAAAClcCLnPuKQCh4CMzNWVu9gx5hVs"
                Recaptcha.getClient(requireActivity().application, siteKey)
                    .onSuccess { client -> recaptchaClient = client }
            } catch (e: Exception) {
                Log.e("Recaptcha", "Error: ${e.message}")
            }
        }
    }

    private fun setup(){
        activity?.title = "Autenticación"

        // BOTÓN GOOGLE
        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        // BOTÓN REGISTRO (Con reCAPTCHA)
        binding.button2.setOnClickListener {
            if (validarCampos()) verificarHumanoYRegistrar()
        }

        // BOTÓN LOGIN (Directo)
        binding.button1.setOnClickListener {
            if (validarCampos()) hacerLoginCorreo()
        }

        binding.tvOlvidaste.setOnClickListener {
            try { findNavController().navigate(R.id.action_login_to_olvidar) } catch (e: Exception) {}
        }
    }

    // --- MÉTODOS DE CORREO Y CONTRASEÑA ---
    private fun verificarHumanoYRegistrar() {
        if (!::recaptchaClient.isInitialized) {
            Toast.makeText(context, "Cargando seguridad...", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            recaptchaClient.execute(RecaptchaAction.SIGNUP)
                .onSuccess { registrarseEnFirebase() }
                .onFailure { Toast.makeText(context, "No eres humano.", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun registrarseEnFirebase() {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            binding.editTextMail.text.toString(), binding.editTextTextPassword.text.toString()
        ).addOnCompleteListener {
            if (it.isSuccessful) findNavController().navigate(R.id.action_login_to_pagina_Inicio)
            else showAlert(it.exception?.message ?: "Error registro")
        }
    }

    private fun hacerLoginCorreo() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            binding.editTextMail.text.toString(), binding.editTextTextPassword.text.toString()
        ).addOnCompleteListener {
            if (it.isSuccessful) findNavController().navigate(R.id.action_login_to_pagina_Inicio)
            else showAlert("Datos incorrectos")
        }
    }

    private fun validarCampos() = binding.editTextMail.text.isNotEmpty() && binding.editTextTextPassword.text.isNotEmpty()

    private fun showAlert(msg: String){
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(msg)
            .setPositiveButton("Ok", null)
            .show()
    }
}