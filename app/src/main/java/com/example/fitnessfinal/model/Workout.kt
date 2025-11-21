package com.example.fitnessfinal.model

data class Workout(
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val description: String,
    val duration: Int,
    val date: String
)