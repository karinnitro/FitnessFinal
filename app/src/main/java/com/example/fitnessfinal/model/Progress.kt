package com.example.fitnessfinal.model

data class Progress(
    val id: Long = 0,
    val userId: Long,
    val weight: Double,        // вес (кг) - обязательно
    val height: Double? = null, // рост (см) - один раз при регистрации
    val chest: Double? = null,  // обхват груди (см) - опционально
    val waist: Double? = null,  // обхват талии (см) - опционально
    val hips: Double? = null,   // обхват бедер (см) - опционально
    val date: String           // дата замера в формате "2024-01-15"
)