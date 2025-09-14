package com.example.odysseyapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.odysseyapp.Model.CategoryBudget
import com.example.odysseyapp.R

class BudgetAdapter(private val budgets: List<CategoryBudget>) :
    RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        val amountText: TextView = itemView.findViewById(R.id.amountText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.budget_item, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = budgets[position]

        holder.categoryText.text = "Category: ${budget.categoryName}"
        holder.amountText.text = "Amount: R${budget.budgetAmount}"


    }

    override fun getItemCount(): Int {
        return budgets.size
    }
}