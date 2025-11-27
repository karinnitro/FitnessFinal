package com.example.fitnessfinal.model

data class Progress(
    val id: Long = 0,
    val userId: Long,
    val weight: Double,        // вес (кг) - обязательно
    val height: Double? = null, // рост (см) - опционально
    val chest: Double? = null,  // обхват груди (см) - больше не используем
    val waist: Double? = null,  // обхват талии (см) - больше не используем
    val hips: Double? = null,   // обхват бедер (см) - больше не используем
    val date: String           // дата замера в формате "2024-01-15"
)