package com.example.cadencia_tfg.data.remote

data class Habito(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val horaInicio: String = "",
    val horaFin: String = "",
    val completado: Boolean = false
)