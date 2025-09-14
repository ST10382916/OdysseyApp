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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.odysseyapp.Model.Expense
import com.example.odysseyapp.R
import com.example.odysseyapp.Services.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpenseListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private lateinit var categorySpinner: Spinner
    private lateinit var dateRangeSpinner: Spinner
    private lateinit var applyFilterButton: Button
    private lateinit var clearFilterButton: Button
    private lateinit var activeFilterText: TextView
    private lateinit var totalSpentText: TextView
    private lateinit var remainingFundsText: TextView

    private val expenses = mutableListOf<Expense>()
    private val filteredExpenses = mutableListOf<Expense>()

    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expense_list, container, false)

        recyclerView = view.findViewById(R.id.expenseRecyclerView)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        dateRangeSpinner = view.findViewById(R.id.dateRangeSpinner)
        applyFilterButton = view.findViewById(R.id.applyFilterButton)
        clearFilterButton = view.findViewById(R.id.clearFiltersButton)
        activeFilterText = view.findViewById(R.id.activeFilterText)
        totalSpentText = view.findViewById(R.id.totalSpentText)
        remainingFundsText = view.findViewById(R.id.remainingFundsText)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ExpenseAdapter(filteredExpenses) { showExpenseDialog(it) }
        recyclerView.adapter = adapter

        applyFilterButton.setOnClickListener { applyFilters() }
        clearFilterButton.setOnClickListener {
            categorySpinner.setSelection(0)
            dateRangeSpinner.setSelection(0)
            applyFilters()
        }

        setupDateRangeSpinner()
        setupCategorySpinner(listOf("All"))
        loadExpenses()

        return view
    }

    private fun loadExpenses() {
        FirebaseHelper.loadExpenses({ loadedExpenses ->
            expenses.clear()
            expenses.addAll(loadedExpenses)

            val categories = mutableListOf("All")
            categories.addAll(expenses.map { it.category }.distinct())
            setupCategorySpinner(categories)

            applyFilters()
        }, {
            Toast.makeText(requireContext(), "Failed to load expenses", Toast.LENGTH_SHORT).show()
        })
    }

    private fun setupCategorySpinner(categories: List<String>) {
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun setupDateRangeSpinner() {
        val options = listOf("All Time", "Last 7 Days", "Last 30 Days")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dateRangeSpinner.adapter = adapter
    }

    private fun applyFilters() {
        val selectedCategory = categorySpinner.selectedItem?.toString() ?: "All"
        val selectedDateRange = dateRangeSpinner.selectedItem?.toString() ?: "All Time"

        val now = Date()
        val calendar = Calendar.getInstance()

        filteredExpenses.clear()
        filteredExpenses.addAll(expenses.filter { expense ->
            val categoryMatch = selectedCategory == "All" || expense.category == selectedCategory
            val dateMatch = when (selectedDateRange) {
                "Last 7 Days" -> {
                    calendar.time = now
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    expense.date?.after(calendar.time) == true
                }

                "Last 30 Days" -> {
                    calendar.time = now
                    calendar.add(Calendar.DAY_OF_YEAR, -30)
                    expense.date?.after(calendar.time) == true
                }

                else -> true
            }
            categoryMatch && dateMatch
        })

        updateActiveFilters(selectedCategory, selectedDateRange)
        updateTotalSpent(selectedCategory)
        updateRemainingFunds(selectedCategory)
        adapter.notifyDataSetChanged()
    }
//Kotlin Help (2025). kotlin.math. Version not specified.
// Source code. Available at: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.math/ [Accessed 2 May 2025].

    private fun updateTotalSpent(selectedCategory: String) {
        val totalSpent = filteredExpenses.sumOf { it.amount }
        totalSpentText.text = "Total Spent: R${String.format("%.2f", totalSpent)}"
    }

    private fun updateRemainingFunds(category: String) {
        FirebaseHelper.loadCategoryBudgets(
            onLoaded = { budgets ->
                val remainingFunds = if (category == "All") {
                    budgets.sumOf { it.budgetAmount - it.currentAmount }
                } else {
                    budgets.find { it.categoryName == category }?.let {
                        it.budgetAmount - it.currentAmount
                    } ?: 0.0
                }

                val remainingText = if (remainingFunds >= 0) {
                    "Remaining Funds: R${String.format("%.2f", remainingFunds)}"
                } else {
                    "Overspent by: R${String.format("%.2f", Math.abs(remainingFunds))}"
                }

                remainingFundsText.text = remainingText
                remainingFundsText.setTextColor(
                    if (remainingFunds >= 0) resources.getColor(R.color.teal_700)
                    else resources.getColor(android.R.color.holo_red_dark)
                )
            },
            onFailure = {
                remainingFundsText.text = "Remaining Funds: N/A"
            }
        )
    }

    private fun updateActiveFilters(category: String, dateRange: String) {
        val filterTextBuilder = StringBuilder()

        if (category != "All") {
            filterTextBuilder.append("Category: $category")
        }
        if (dateRange != "All Time") {
            if (filterTextBuilder.isNotEmpty()) filterTextBuilder.append(" | ")
            filterTextBuilder.append("Date: $dateRange")
        }

        activeFilterText.text =
            if (filterTextBuilder.isEmpty()) "Showing all expenses" else filterTextBuilder.toString()
        clearFilterButton.visibility = if (filterTextBuilder.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showExpenseDialog(expense: Expense) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_expense_details, null)

        dialogView.findViewById<TextView>(R.id.expenseName).text = "Name: ${expense.name}"
        dialogView.findViewById<TextView>(R.id.expenseCategory).text =
            "Category: ${expense.category}"
        dialogView.findViewById<TextView>(R.id.expenseAmount).text = "Amount: R${expense.amount}"
        dialogView.findViewById<TextView>(R.id.expenseDescription).text =
            "Description: ${expense.description}"
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
            .create()
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

    class ExpenseAdapter(
        private val expenseList: List<Expense>,
        private val onItemClicked: (Expense) -> Unit
    ) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

        inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val title = itemView.findViewById<TextView>(R.id.itemTitle)
            private val amount = itemView.findViewById<TextView>(R.id.itemAmount)
            private val category = itemView.findViewById<TextView>(R.id.itemCategory)

            fun bind(expense: Expense) {
                title.text = "${expense.name}"
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

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }
}