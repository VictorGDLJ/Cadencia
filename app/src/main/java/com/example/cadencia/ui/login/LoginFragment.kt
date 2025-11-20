package com.example.cadencia.ui.login

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.example.cadencia.databinding.FragmentLoginBinding
import com.example.cadencia.R

// NUEVO: Importaciones necesarias para Firebase y Navegación
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import com.example.cadencia.MainActivity // Asegúrate de importar tu Activity principal

class LoginFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    private var _binding: FragmentLoginBinding? = null

    // NUEVO: Variable para Firebase Auth
    private lateinit var auth: FirebaseAuth

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    // NUEVO: Aquí es donde ocurre la magia de "Recordar sesión"
    // Se ejecuta cada vez que el fragmento se hace visible
    override fun onStart() {
        super.onStart()
        // Inicializamos Auth por si acaso no está hecho
        auth = FirebaseAuth.getInstance()

        // Verificamos si hay usuario guardado
        val usuarioActual = auth.currentUser
        if (usuarioActual != null) {
            // Si ya existe, saltamos el login directamente
            irAlMenuPrincipal()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login
        val loadingProgressBar = binding.loading

        // ... (Mantenemos los observers de validación de texto igual) ...
        loginViewModel.loginFormState.observe(viewLifecycleOwner, Observer { loginFormState ->
            if (loginFormState == null) {
                return@Observer
            }
            loginButton.isEnabled = loginFormState.isDataValid
            loginFormState.usernameError?.let {
                usernameEditText.error = getString(it)
            }
            loginFormState.passwordError?.let {
                passwordEditText.error = getString(it)
            }
        })

        // NOTA: El observer de loginResult lo puedes dejar para errores visuales,
        // pero la lógica real la haremos con Firebase abajo.

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                loginViewModel.loginDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)

        // Listener para el teclado (Enter)
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                realizarLoginFirebase(usernameEditText.text.toString(), passwordEditText.text.toString())
            }
            false
        }

        // Listener del Botón Login
        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE

            // MODIFICADO: En lugar de llamar a loginViewModel.login (que es falso),
            // llamamos a nuestra función de Firebase
            realizarLoginFirebase(
                usernameEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }
    }

    // NUEVO: Función para hacer el login real con Firebase
    private fun realizarLoginFirebase(email: String, pass: String) {
        // Pequeña validación extra por seguridad
        if (email.isBlank() || pass.isBlank()) {
            binding.loading.visibility = View.GONE
            return
        }

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                binding.loading.visibility = View.GONE // Ocultar carga

                if (task.isSuccessful) {
                    // Éxito: Firebase guarda la sesión automáticamente aquí
                    irAlMenuPrincipal()
                } else {
                    // Error
                    showLoginFailed(R.string.login_failed) // Asegúrate de tener este string o usa un texto fijo
                    // O muestra el error real:
                    // Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // NUEVO: Función para cambiar de pantalla
    private fun irAlMenuPrincipal() {
        // Asumiendo que MainActivity es tu pantalla principal tras el login
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish() // Cierra la actividad actual para que no puedan volver atrás
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        // Esta función venía en la plantilla, ya no la usamos tanto porque
        // redirigimos en irAlMenuPrincipal(), pero puedes dejarla.
        val welcome = getString(R.string.welcome) + model.displayName
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}