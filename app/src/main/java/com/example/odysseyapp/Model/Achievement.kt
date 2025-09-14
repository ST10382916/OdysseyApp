package com.example.odysseyapp.Model


data class Achievement(
    val title: String = ""
)

data class SavingGoal(
    val title: String = "",
    val targetAmount: Int = 0,
    val currentAmount: Int = 0,
    val description: String = "",
    val isCompleted: Boolean = false
)

data class CurrentGoal(
    val title: String = "",
    val targetAmount: Int = 0,
    val amountSaved: Int = 0
)