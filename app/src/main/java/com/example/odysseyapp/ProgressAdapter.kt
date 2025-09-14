package com.example.odysseyapp.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.odysseyapp.Model.CategoryBudget
import com.example.odysseyapp.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView

class ProgressAdapter(private val budgets: List<CategoryBudget>) :
    RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder>() {

    class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameText: MaterialTextView = itemView.findViewById(R.id.categoryNameText)
        val spendingAmountText: MaterialTextView = itemView.findViewById(R.id.spendingAmountText)
        val budgetAmountText: MaterialTextView = itemView.findViewById(R.id.budgetAmountText)
        val progressBar: LinearProgressIndicator = itemView.findViewById(R.id.progressBar)
        val progressPercentageText: MaterialTextView = itemView.findViewById(R.id.progressPercentageText)
        val statusText: MaterialTextView = itemView.findViewById(R.id.statusText)
        val cardView: MaterialCardView = itemView.findViewById(R.id.progressCardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.progress_item, parent, false)
        return ProgressViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val budget = budgets[position]
        val progressPercentage = if (budget.budgetAmount > 0) {
            ((budget.currentAmount / budget.budgetAmount) * 100).toInt()
        } else 0

        // Set texts
        holder.categoryNameText.text = budget.categoryName
        holder.spendingAmountText.text = "R%.2f".format(budget.currentAmount)
        holder.budgetAmountText.text = "/ R%.2f".format(budget.budgetAmount)
        holder.progressPercentageText.text = "$progressPercentage%"

        // Set progress bar
        holder.progressBar.progress = progressPercentage.coerceAtMost(100)

        // Set status and colors based on spending
        when {
            budget.currentAmount > budget.budgetAmount -> {
                // Over budget - RED
                holder.statusText.text = "OVER BUDGET"
                holder.statusText.setTextColor(Color.parseColor("#F44336"))
                holder.progressPercentageText.setTextColor(Color.parseColor("#F44336"))
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                holder.progressBar.setIndicatorColor(Color.parseColor("#F44336"))
                holder.spendingAmountText.setTextColor(Color.parseColor("#F44336"))
            }
            progressPercentage >= 80 -> {
                // Approaching limit - ORANGE
                holder.statusText.text = "APPROACHING LIMIT"
                holder.statusText.setTextColor(Color.parseColor("#FF9800"))
                holder.progressPercentageText.setTextColor(Color.parseColor("#FF9800"))
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
                holder.progressBar.setIndicatorColor(Color.parseColor("#FF9800"))
                holder.spendingAmountText.setTextColor(Color.parseColor("#FF9800"))
            }
            progressPercentage >= 50 -> {
                // On track - GREEN
                holder.statusText.text = "ON TRACK"
                holder.statusText.setTextColor(Color.parseColor("#4CAF50"))
                holder.progressPercentageText.setTextColor(Color.parseColor("#4CAF50"))
                holder.cardView.setCardBackgroundColor(Color.parseColor("#E8F5E8"))
                holder.progressBar.setIndicatorColor(Color.parseColor("#4CAF50"))
                holder.spendingAmountText.setTextColor(Color.parseColor("#4CAF50"))
            }
            else -> {
                // Well within budget - BLUE
                holder.statusText.text = "WELL WITHIN BUDGET"
                holder.statusText.setTextColor(Color.parseColor("#2196F3"))
                holder.progressPercentageText.setTextColor(Color.parseColor("#2196F3"))
                holder.cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD"))
                holder.progressBar.setIndicatorColor(Color.parseColor("#2196F3"))
                holder.spendingAmountText.setTextColor(Color.parseColor("#2196F3"))
            }
        }

        // Reset other text colors to default
        holder.categoryNameText.setTextColor(Color.parseColor("#212121"))
        holder.budgetAmountText.setTextColor(Color.parseColor("#757575"))
    }

    override fun getItemCount(): Int {
        return budgets.size
    }
}