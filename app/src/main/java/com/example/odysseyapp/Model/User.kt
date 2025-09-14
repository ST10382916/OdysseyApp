package com.example.odysseyapp.Model


data class User(
    val id: Int,
    val password: String,
    val email: String,
    val isLoggedIn: Boolean
)

