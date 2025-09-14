package com.example.odysseyapp.Model

data class HistoryEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String = "",
    val details: String = ""
)
//for part 3 to add advice history