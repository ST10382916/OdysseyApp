package com.example.odysseyapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.odysseyapp.Adapter.BudgetAdapter
import com.example.odysseyapp.Model.CategoryBudget
import com.example.odysseyapp.R
import com.example.odysseyapp.Services.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth

class categoryFragment : Fragment() {

    private lateinit var categoryNameField: EditText
    private lateinit var categoryBudgetField: EditText
    private lateinit var addButton: Button
    private lateinit var saveButton: Button
    private lateinit var budgetsRecyclerView: RecyclerView

    private val categoryList = mutableListOf<CategoryBudget>()
    private lateinit var adapter: BudgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        categoryNameField = view.findViewById(R.id.CategoryName)
        categoryBudgetField = view.findViewById(R.id.budgetAmountField)
        addButton = view.findViewById(R.id.addButton)
        saveButton = view.findViewById(R.id.saveButton)
        budgetsRecyclerView = view.findViewById(R.id.budgetsRecyclerView)

        adapter = BudgetAdapter(categoryList)
        budgetsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        budgetsRecyclerView.adapter = adapter

        addButton.setOnClickListener {
            val name = categoryNameField.text.toString().trim()
            val budget = categoryBudgetField.text.toString().trim().toDoubleOrNull()

            if (name.isEmpty() || budget == null) {
                Toast.makeText(
                    requireContext(),
                    "Please enter valid name and budget",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = CategoryBudget(
                categoryName = name,
                budgetAmount = budget,
                currentAmount = 0.0,
                userId = user.uid,
                isNew = true
            )

            categoryList.add(category)
            adapter.notifyDataSetChanged()

            categoryNameField.text.clear()
            categoryBudgetField.text.clear()
        }

        saveButton.setOnClickListener {
            saveNewBudgets()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    private fun loadCategories() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseHelper.loadCategoryBudgets(
            onLoaded = { categories ->
                categoryList.clear()
                categoryList.addAll(categories)
                adapter.notifyDataSetChanged()
            },
            onFailure = {
                Toast.makeText(requireContext(), "Failed to load categories", Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }

    private fun saveNewBudgets() {
        val newCategories = categoryList.filter { it.isNew }

        if (newCategories.isEmpty()) {
            Toast.makeText(requireContext(), "No new budgets to save", Toast.LENGTH_SHORT).show()
            return
        }

        var savedCount = 0
        for (category in newCategories) {
            FirebaseHelper.saveCategoryBudget(
                category,
                onSuccess = {
                    savedCount++
                    if (savedCount == newCategories.size) {
                        Toast.makeText(
                            requireContext(),
                            "Budgets saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadCategories()
                    }
                },
                onFailure = {
                    Toast.makeText(
                        requireContext(),
                        "Failed to save: ${category.categoryName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
}