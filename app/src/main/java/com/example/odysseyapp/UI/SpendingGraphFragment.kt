package com.example.odysseyapp.UI

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.odysseyapp.Model.CategoryBudget
import com.example.odysseyapp.Model.Expense
import com.example.odysseyapp.R
import com.example.odysseyapp.Services.FirebaseHelper
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*

class SpendingGraphFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var refreshButton: Button
    private lateinit var dateRangeText: TextView

    private var startDate: Date = Calendar.getInstance().apply {
        add(Calendar.MONTH, -1)
    }.time
    private var endDate: Date = Date()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val expenses = mutableListOf<Expense>()
    private val categoryBudgets = mutableListOf<CategoryBudget>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_spending_graph, container, false)

        initializeViews(view)
        setupChart()
        setupClickListeners()
        loadData()

        return view
    }

    private fun initializeViews(view: View) {
        barChart = view.findViewById(R.id.spending_bar_chart)
        startDateButton = view.findViewById(R.id.start_date_button)
        endDateButton = view.findViewById(R.id.end_date_button)
        refreshButton = view.findViewById(R.id.refresh_button)
        dateRangeText = view.findViewById(R.id.date_range_text)

        updateDateRangeText()
    }

    private fun setupChart() {
        barChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)

            // X-axis setup
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                isGranularityEnabled = true
                labelRotationAngle = -45f //Rotate for better readability
                barChart.extraBottomOffset = 15f //Add spacing for visibility
                textSize = 10f
                setAvoidFirstLastClipping(true)
                spaceMin = 0.5f
                spaceMax = 0.5f
            }

            // Y-axis setup
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f          // Axis minimum padding
                spaceTop = 15f            // Top space padding
            }
            axisRight.isEnabled = false

            // Legend setup
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(true)
            }
        }
    }

    private fun setupClickListeners() {
        startDateButton.setOnClickListener { showDatePicker(true) }
        endDateButton.setOnClickListener { showDatePicker(false) }
        refreshButton.setOnClickListener { loadData() }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val currentDate = if (isStartDate) startDate else endDate
        calendar.time = currentDate

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, day)

                if (isStartDate) {
                    startDate = selectedCalendar.time
                    if (startDate.after(endDate)) {
                        endDate = Date(startDate.time + 24 * 60 * 60 * 1000)
                    }
                } else {
                    endDate = selectedCalendar.time
                    if (endDate.before(startDate)) {
                        startDate = Date(endDate.time - 24 * 60 * 60 * 1000)
                    }
                }

                updateDateRangeText()
                loadData()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateRangeText() {
        dateRangeText.text = "Period: ${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
    }

    private fun loadData() {
        FirebaseHelper.loadExpenses(
            onLoaded = { loadedExpenses ->
                expenses.clear()
                expenses.addAll(loadedExpenses)

                // Load budgets before processing chart
                FirebaseHelper.loadCategoryBudgets(
                    onLoaded = { loadedBudgets ->
                        categoryBudgets.clear()
                        categoryBudgets.addAll(loadedBudgets)
                        processDataAndUpdateChart()
                    },
                    onFailure = { exception ->
                        Toast.makeText(requireContext(), "Failed to load budgets: ${exception.message}", Toast.LENGTH_SHORT).show()
                        processDataAndUpdateChart()
                    }
                )
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to load expenses: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }


    private fun processDataAndUpdateChart() {
        val filteredExpenses = expenses.filter { expense ->
            expense.date?.let { date ->
                !date.before(startDate) && !date.after(endDate)
            } ?: false
        }

        val categorySpending = filteredExpenses
            .groupBy { it.category }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }

        val barEntries = mutableListOf<BarEntry>()
        val categories = mutableListOf<String>()

        var index = 0f
        for ((category, spending) in categorySpending) {
            barEntries.add(BarEntry(index, spending.toFloat()))
            categories.add(category)
            index++
        }

        if (barEntries.isEmpty()) {
            Toast.makeText(requireContext(), "No expenses found for selected period", Toast.LENGTH_SHORT).show()
            barChart.clear()
            barChart.invalidate()
            return
        }

        val dataSet = BarDataSet(barEntries, "Category Spending").apply {
            colors = ColorTemplate.createColors(ColorTemplate.MATERIAL_COLORS) // Dynamic color assignment
            valueTextSize = 12f
            setDrawValues(true)
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.7f // Adjust bar width
        }

        barChart.data = barData

        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(categories)
            labelCount = categories.size
            granularity = 1f
            isGranularityEnabled = true
            setAvoidFirstLastClipping(true)
        }

        barChart.axisLeft.removeAllLimitLines()

        barChart.notifyDataSetChanged()
        barChart.invalidate()

        barChart.post {
            barChart.invalidate()
        }

        showSpendingSummary(categorySpending)
    }

    private fun showSpendingSummary(categorySpending: Map<String, Double>) {
        val totalSpent = categorySpending.values.sum()
        val categoryCount = categorySpending.size

        val summaryMessage = """
            Total Spent: ${"%.2f".format(totalSpent)}
            Categories: $categoryCount
        """.trimIndent()

        Toast.makeText(requireContext(), summaryMessage, Toast.LENGTH_LONG).show()
    }
}
