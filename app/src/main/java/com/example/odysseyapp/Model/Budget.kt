package com.example.odysseyapp.Model

data class CategoryBudget(
    val categoryName: String = "",
    val budgetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val userId: String = "",
    var isNew: Boolean = false // <- Added to track new budgets
)


