package com.example.cadencia_tfg.ui.detalle

import android.app.Activity
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
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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

    private lateinit var recaptchaClient: RecaptchaClient
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val usuarioActual = FirebaseAuth.getInstance().currentUser
        if (usuarioActual != null) {
            findNavController().navigate(R.id.action_login_to_pagina_Inicio)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val analytics = FirebaseAnalytics.getInstance(requireContext())
        analytics.logEvent("InitScreen", Bundle().apply { putString("message", "Login Iniciado") })

        inicializarRecaptcha()
        configurarGoogleSignIn()
        setup()
    }

    private fun configurarGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
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

    private fun inicializarRecaptcha() {
        lifecycleScope.launch {
            try {
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

        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        binding.button2.setOnClickListener {
            if (validarCampos()) verificarHumanoYRegistrar()
        }

        binding.button1.setOnClickListener {
            if (validarCampos()) hacerLoginCorreo()
        }

        binding.tvOlvidaste.setOnClickListener {
            try { findNavController().navigate(R.id.action_login_to_olvidar) } catch (e: Exception) {}
        }
    }

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