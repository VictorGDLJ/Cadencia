package com.example.cadencia_tfg.data.remote

data class Habito(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val diasFrecuencia: List<String> = emptyList(), // ["Lunes", "Miercoles"...]

    // --- NUEVOS CAMPOS ---
    val esIndefinido: Boolean = true,      // Switch activado por defecto
    val fechaInicio: Long = 0L,            // Timestamp (ms)
    val fechaFin: Long = 0L,               // Timestamp (ms), opcional si es indefinido
    // ---------------------

    val fechaCreacion: Long = System.currentTimeMillis()
)