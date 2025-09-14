package com.example.odysseyapp.Services

import com.example.odysseyapp.Model.Expense
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object AiHelper {

    private val client = OkHttpClient()
//Rapidapi.com. (2024). ChatGPT 4. [online]
// Available at: https://rapidapi.com/rphrp1985/api/chatgpt-42/playground/apiendpoint_d49fca21-77ca-4cd5-8047-1b85bda8f68e [Accessed 2 May 2025].

    /**
     * Sends a prompt to the AI API and receives a financial advisory response.
     * Automatically handles different API response formats.
     */
    fun getAdviceFromExpenses(
        promptText: String,
        callback: (String) -> Unit
    ) {
        val mediaType = "application/json".toMediaTypeOrNull()

        // Prepare request body
        val requestJson = JSONObject().apply {
            put("messages", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("content", promptText)
            }))
            put("model", "gpt-4o-mini")
        }

        val body = requestJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://chatgpt-42.p.rapidapi.com/chat")
            .post(body)
            .addHeader("x-rapidapi-key", "6880942c7msh1eafddfb6157c59p18dd2jsnce122cbd943c")
            .addHeader("x-rapidapi-host", "chatgpt-42.p.rapidapi.com")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("AI Request Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                println("AI API Response: $responseBody")

                if (responseBody.isNullOrEmpty()) {
                    callback("AI response is empty.")
                    return
                }

                val result = JSONObject(responseBody)

                // Universal parsing
                val advice = when {
                    result.has("output") -> result.optString("output", "No advice returned.")
                    result.has("choices") -> {
                        val choices = result.optJSONArray("choices")
                        val firstChoice = choices?.optJSONObject(0)
                        val message = firstChoice?.optJSONObject("message")
                        message?.optString("content", "No advice returned.")
                    }

                    else -> "No advice returned."
                }

                callback(advice ?: "No advice returned.")
            }
        })
    }

    /**
     * Analyzes spending and returns a category-to-total map.
     */
    fun analyzeSpending(expenses: List<Expense>): Map<String, Double> {
        return expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    /**
     * Builds an AI prompt with advice on spending patterns, savings goals, and personalized suggestions.
     */
    fun generateComprehensivePrompt(
        spending: Map<String, Double>,
        budgetThresholds: Map<String, Double>,
        savingsGoal: Double? = null
    ): String {
        val builder = StringBuilder("User's monthly financial report:\n\n")

        spending.forEach { (category, total) ->
            val budget = budgetThresholds[category]
            if (budget != null && total > budget) {
                builder.append("- $category: R$total (Over budget by R${"%.2f".format(total - budget)})\n")
            } else if (budget != null) {
                builder.append("- $category: R$total (Within budget)\n")
            } else {
                builder.append("- $category: R$total\n")
            }
        }

        builder.append("\nGenerate 2 achievable financial goals for the user based on this data.\n")

        savingsGoal?.let {
            builder.append("\nThe user's savings goal is R$it this month. Provide suggestions to meet this target.\n")
        }

        builder.append("\nOffer personalized strategies to help the user achieve both the savings goal and the generated goals.")

        return builder.toString()
    }
}
