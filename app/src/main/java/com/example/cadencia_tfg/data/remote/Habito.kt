package com.example.cadencia_tfg.data.remote

data class Habito(
    val id: String = "",
    val userId: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val diasFrecuencia: List<String> = emptyList(),

    val esIndefinido: Boolean = true,
    val fechaInicio: Long = 0L,
    val fechaFin: Long = 0L,
    // ---------------------

    val fechaCreacion: Long = System.currentTimeMillis()
)