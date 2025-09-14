package com.example.odysseyapp.UI

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.odysseyapp.Adapter.ProgressAdapter
import com.example.odysseyapp.Model.CategoryBudget
import com.example.odysseyapp.R
import com.example.odysseyapp.Services.FirebaseHelper
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView

class ProgressDashboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressAdapter: ProgressAdapter
    private lateinit var budgets: MutableList<CategoryBudget>

    // Overall progress views
    private lateinit var overallProgressIndicator: CircularProgressIndicator
    private lateinit var overallStatusText: MaterialTextView
    private lateinit var totalSpendingText: MaterialTextView
    private lateinit var budgetSummaryText: MaterialTextView
    private lateinit var overallCard: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_progress_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        loadBudgetProgress()
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewProgress)
        overallProgressIndicator = view.findViewById(R.id.overallProgressIndicator)
        overallStatusText = view.findViewById(R.id.overallStatusText)
        totalSpendingText = view.findViewById(R.id.totalSpendingText)
        budgetSummaryText = view.findViewById(R.id.budgetSummaryText)
        overallCard = view.findViewById(R.id.overallCard)
    }

    private fun setupRecyclerView() {
        budgets = mutableListOf()
        progressAdapter = ProgressAdapter(budgets)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = progressAdapter
    }

    private fun loadBudgetProgress() {
        FirebaseHelper.loadCategoryBudgets(
            onLoaded = { loadedBudgets ->
                budgets.clear()
                budgets.addAll(loadedBudgets)
                progressAdapter.notifyDataSetChanged()
                updateOverallProgress()
            },
            onFailure = { exception ->
                Toast.makeText(context, "Failed to load budgets: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun updateOverallProgress() {
        if (budgets.isEmpty()) {
            showEmptyState()
            return
        }

        val totalBudget = budgets.sumOf { it.budgetAmount }
        val totalSpent = budgets.sumOf { it.currentAmount }
        val overallProgress = if (totalBudget > 0) ((totalSpent / totalBudget) * 100).toInt() else 0

        // Update progress indicator
        overallProgressIndicator.progress = overallProgress.coerceAtMost(100)

        // Update texts
        totalSpendingText.text = "R%.2f spent".format(totalSpent)
        budgetSummaryText.text = "of R%.2f budgeted".format(totalBudget)

        // Update status and colors
        when {
            totalSpent > totalBudget -> {
                overallStatusText.text = "OVER BUDGET"
                overallStatusText.setTextColor(Color.parseColor("#F44336")) // Red
                overallCard.setCardBackgroundColor(Color.parseColor("#FFEBEE")) // Light red background
                overallProgressIndicator.setIndicatorColor(Color.parseColor("#F44336"))
            }
            overallProgress >= 80 -> {
                overallStatusText.text = "APPROACHING LIMIT"
                overallStatusText.setTextColor(Color.parseColor("#FF9800")) // Orange
                overallCard.setCardBackgroundColor(Color.parseColor("#FFF3E0")) // Light orange background
                overallProgressIndicator.setIndicatorColor(Color.parseColor("#FF9800"))
            }
            overallProgress >= 50 -> {
                overallStatusText.text = "ON TRACK"
                overallStatusText.setTextColor(Color.parseColor("#4CAF50")) // Green
                overallCard.setCardBackgroundColor(Color.parseColor("#E8F5E8")) // Light green background
                overallProgressIndicator.setIndicatorColor(Color.parseColor("#4CAF50"))
            }
            else -> {
                overallStatusText.text = "WELL WITHIN BUDGET"
                overallStatusText.setTextColor(Color.parseColor("#2196F3")) // Blue
                overallCard.setCardBackgroundColor(Color.parseColor("#E3F2FD")) // Light blue background
                overallProgressIndicator.setIndicatorColor(Color.parseColor("#2196F3"))
            }
        }
    }

    private fun showEmptyState() {
        overallStatusText.text = "NO BUDGETS SET"
        overallStatusText.setTextColor(Color.parseColor("#757575")) // Gray
        totalSpendingText.text = "Set up category budgets to track progress"
        budgetSummaryText.text = ""
        overallProgressIndicator.progress = 0
        overallCard.setCardBackgroundColor(Color.parseColor("#FAFAFA")) // Light gray background
    }
}