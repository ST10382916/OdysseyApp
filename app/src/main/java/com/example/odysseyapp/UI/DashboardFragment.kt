package com.example.odysseyapp.UI

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.odysseyapp.Model.Expense
import com.example.odysseyapp.R
import com.example.odysseyapp.Services.FirebaseHelper
import com.example.odysseyapp.Utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var budgetProgressBar: ProgressBar
    private lateinit var budgetSummaryText: TextView
    private lateinit var budgetRemainingText: TextView
    private lateinit var recentExpensesRecyclerView: RecyclerView
    private lateinit var welcomeMessage: TextView

    private val expenses = mutableListOf<Expense>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize UI elements
        budgetProgressBar = view.findViewById(R.id.budgetProgressBar)
        budgetSummaryText = view.findViewById(R.id.budgetSummaryText)
        budgetRemainingText = view.findViewById(R.id.budgetRemainingText)
        recentExpensesRecyclerView = view.findViewById(R.id.recentExpensesRecyclerView)
        welcomeMessage = view.findViewById(R.id.WelcomeMessage)

        recentExpensesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Welcome Message
        val userEmail = SessionManager.getUserEmail(requireContext()) ?: "User"
        welcomeMessage.text = "Welcome to your expense dashboard!"

        loadBudgetInfo()
        loadRecentExpenses()

        return view
    }

    private fun loadBudgetInfo() {
        val userId = SessionManager.getUserId(requireContext()) ?: return

        FirebaseHelper.loadCategoryBudgets(
            onLoaded = { budgets ->
                var totalBudget = 0.0
                var totalUsed = 0.0

                for (budget in budgets) {
                    totalBudget += budget.budgetAmount
                    totalUsed += budget.currentAmount
                }

                val percentage =
                    if (totalBudget > 0) ((totalUsed / totalBudget) * 100).toInt() else 0
                budgetProgressBar.progress = percentage

                budgetSummaryText.text = "R$totalUsed used of R$totalBudget"

                val remaining = totalBudget - totalUsed
                budgetRemainingText.text = if (remaining >= 0) {
                    "Remaining: R$remaining"
                } else {
                    "Overspent: R${Math.abs(remaining)}"
                }

                budgetRemainingText.setTextColor(
                    if (remaining >= 0) resources.getColor(R.color.teal_700)
                    else resources.getColor(android.R.color.holo_red_dark)
                )
            },
            onFailure = {
                budgetProgressBar.progress = 0
                budgetSummaryText.text = "Failed to load budget"
            }
        )
    }

    private fun loadRecentExpenses() {
        FirebaseHelper.loadExpenses(
            onLoaded = { loadedExpenses ->
                expenses.clear()
                expenses.addAll(loadedExpenses.sortedByDescending { it.date?.time ?: 0 })

                recentExpensesRecyclerView.adapter = ExpenseAdapter(expenses) { selectedExpense ->
                    showExpenseDialog(selectedExpense)
                }
            },
            onFailure = {
                Toast.makeText(requireContext(), "Failed to load expenses", Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }

    private fun showExpenseDialog(expense: Expense) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_expense_details, null)

        dialogView.findViewById<TextView>(R.id.expenseName).text = "Name: ${expense.name}"
        dialogView.findViewById<TextView>(R.id.expenseCategory).text =
            "Category: ${expense.category}"
        dialogView.findViewById<TextView>(R.id.expenseAmount).text = "Amount: R${expense.amount}"

        val formattedDate =
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(expense.date ?: Date())
        dialogView.findViewById<TextView>(R.id.expenseDate).text = "Date: $formattedDate"

        val recurringText = if (expense.isRecurring) "Recurring?: Yes" else "Recurring?: No"
        dialogView.findViewById<TextView>(R.id.expenseRecurring).text = recurringText

        val downloadBtn = dialogView.findViewById<Button>(R.id.downloadImageBtn)

        if (!expense.imageUri.isNullOrEmpty()) {
            downloadBtn.visibility = View.VISIBLE
            downloadBtn.setOnClickListener {
                downloadImageToGallery(expense.imageUri!!)
            }
        } else {
            downloadBtn.visibility = View.GONE
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun downloadImageToGallery(imageUrl: String) {
        val request = DownloadManager.Request(Uri.parse(imageUrl))
            .setTitle("Downloading Receipt")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "expense_image.jpg")

        val manager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    inner class ExpenseAdapter(
        private val expenseList: List<Expense>,
        private val onItemClicked: (Expense) -> Unit
    ) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

        inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val title = itemView.findViewById<TextView>(R.id.itemTitle)
            private val amount = itemView.findViewById<TextView>(R.id.itemAmount)
            private val category = itemView.findViewById<TextView>(R.id.itemCategory)

            fun bind(expense: Expense) {
                title.text = expense.name
                amount.text = "R${expense.amount}"
                category.text = "Category: " + expense.category
                itemView.setOnClickListener { onItemClicked(expense) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_expense_simple, parent, false)
            return ExpenseViewHolder(view)
        }

        override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
            holder.bind(expenseList[position])
        }

        override fun getItemCount(): Int = expenseList.size
    }
}