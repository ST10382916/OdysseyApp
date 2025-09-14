package com.example.odysseyapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.odysseyapp.R
import com.example.odysseyapp.Services.AiHelper
import com.example.odysseyapp.Services.FirebaseHelper

class AiAssistantFragment : Fragment() {

    private lateinit var analyzeButton: Button
    private lateinit var adviceTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ai_assistant, container, false)

        analyzeButton = view.findViewById(R.id.analyzeButton)
        adviceTextView = view.findViewById(R.id.aiAdviceTextView)
        progressBar = view.findViewById(R.id.progressBar)

        analyzeButton.setOnClickListener {
            startAnalysis()
        }

        return view
    }

    private fun startAnalysis() {
        // Show progress
        adviceTextView.text = "Analyzing your spending habits..."
        progressBar.visibility = View.VISIBLE
        analyzeButton.isEnabled = false

        // Load expenses from Firebase
        FirebaseHelper.loadExpenses(
            onLoaded = { expenses ->
                if (expenses.isEmpty()) {
                    showResult("No expenses found. Please add some expenses first.")
                    return@loadExpenses
                }

                // Analyze spending
                val spending = AiHelper.analyzeSpending(expenses)

                // Define budget thresholds (You can load this from Firebase later too)
                val budgets = mapOf(
                    "Food" to 2000.0,
                    "Transport" to 1000.0,
                    "Entertainment" to 800.0
                )

                // Define savings goal
                val savingsGoal = 1500.0

                // Generate prompt
                val prompt = AiHelper.generateComprehensivePrompt(spending, budgets, savingsGoal)

                // Send to AI for advice
                AiHelper.getAdviceFromExpenses(prompt) { advice ->
                    showResult(advice)
                }
            },
            onFailure = { exception ->
                showResult("Failed to load expenses: ${exception.message}")
            }
        )
    }

    private fun showResult(message: String) {
        requireActivity().runOnUiThread {
            progressBar.visibility = View.GONE
            analyzeButton.isEnabled = true
            adviceTextView.text = message
        }
    }
}