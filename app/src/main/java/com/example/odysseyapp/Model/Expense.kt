package com.example.odysseyapp.Model

import java.util.Date


//Reference:
//The IIE. 2025. [PROGRAMMING 3C/ Open Source Coding(Introduction)]. Unpublished


data class Expense(
    var name: String = "",
    var category: String = "",
    var amount: Double = 0.0,
    var description: String = "",
    var date: Date? = null,
    var isRecurring: Boolean = false,
    var imageUri: String? = null
)