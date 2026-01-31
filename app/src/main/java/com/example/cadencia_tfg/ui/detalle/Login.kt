package com.example.cadencia_tfg.ui.detalle

import com.example.cadencia_tfg.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cadencia_tfg.databinding.FragmentLoginBinding
import com.example.cadencia_tfg.ui.Pagina_Inicio
import com.example.cadencia_tfg.ui.main.MainActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth

class Login : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)


        val analytics = FirebaseAnalytics.getInstance(requireContext())
        val bundle = Bundle()

        bundle.putString("message", "Integracion de firebase completa")
        analytics.logEvent("InitScreen", bundle)

        setup()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setup(){

        activity?.title = "Autenticación"

        binding.button2.setOnClickListener {
            if (binding.editTextMail.text.isNotEmpty() && binding.editTextTextPassword.text.isNotEmpty()){
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.editTextMail.text.toString(), binding.editTextTextPassword.text.toString()).addOnCompleteListener {
                    if (!it.isSuccessful){
                        showAlert()
                    }
                }
            }
        }
        binding.button1.setOnClickListener {
            if (binding.editTextMail.text.isNotEmpty() && binding.editTextTextPassword.text.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(binding.editTextMail.text.toString(), binding.editTextTextPassword.text.toString()).addOnCompleteListener {
                    if (!it.isSuccessful){
                        showAlert()
                    }else{
                        if (it.isSuccessful) {
                            Toast.makeText(context, "Bienvenido!", Toast.LENGTH_SHORT).show()



                            findNavController().navigate(R.id.action_login_to_pagina_Inicio)                        }
                    }
                }
            }
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}