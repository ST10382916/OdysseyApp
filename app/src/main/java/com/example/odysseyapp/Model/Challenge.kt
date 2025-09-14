package com.example.odysseyapp.Model


data class Challenge(
    val id: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)