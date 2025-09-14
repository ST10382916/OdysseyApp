package com.example.odysseyapp.Services

import android.content.Context
import com.example.odysseyapp.Model.Achievement
import com.example.odysseyapp.Model.CategoryBudget
import com.example.odysseyapp.Model.Challenge
import com.example.odysseyapp.Model.Expense
import com.example.odysseyapp.Model.SavingGoal
import com.example.odysseyapp.Utils.SessionManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseHelper {
    //Firebase (2025). Firebase Realtime Database.
    // Version not specified. Source code. Available at: https://firebase.google.com/docs/database/?utm_source=studio [Accessed 2 May 2025].
    private lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    private fun getDatabase(): DatabaseReference = FirebaseDatabase.getInstance().reference

    private fun getUserId(): String? {
        return SessionManager.getUserId(context)
    }

    // --- USER PROFILE ---

    fun saveUserProfile(
        userId: String,
        fullName: String,
        email: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userMap = mapOf("uid" to userId, "fullName" to fullName, "email" to email)

        getDatabase().child("users").child(userId)
            .setValue(userMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUserProfile(onLoaded: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        getDatabase().child("users").child(userId)
            .get()
            .addOnSuccessListener {
                val fullName = it.child("fullName").getValue(String::class.java) ?: "User"
                onLoaded(fullName)
            }
            .addOnFailureListener { onFailure(it) }
    }

    // --- CATEGORY BUDGETS ---

    fun saveCategoryBudget(
        budget: CategoryBudget,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        val safeName = budget.categoryName.replace("[.#$\\[\\]/]".toRegex(), "_")
        getDatabase().child("Categories").child(userId).child(safeName)
            .setValue(budget)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun loadCategoryBudgets(
        onLoaded: (List<CategoryBudget>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        getDatabase().child("Categories").child(userId)
            .get()
            .addOnSuccessListener { snap ->
                val budgets = snap.children.mapNotNull { it.getValue(CategoryBudget::class.java) }
                onLoaded(budgets)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteCategoryBudget(
        categoryName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        val safeName = categoryName.replace("[.#$\\[\\]/]".toRegex(), "_")
        getDatabase().child("Categories").child(userId).child(safeName)
            .removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun clearCategoryBudgets(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        getDatabase().child("Categories").child(userId)
            .removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // --- EXPENSES ---

    fun saveExpense(expense: Expense, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        // Save expense
        getDatabase().child("Expenses").child(userId).push()
            .setValue(expense)
            .addOnSuccessListener {

                // Now update the category's currentAmount
                val safeCategory = expense.category.replace("[.#$\\[\\]/]".toRegex(), "_")
                val categoryRef =
                    getDatabase().child("Categories").child(userId).child(safeCategory)

                categoryRef.get().addOnSuccessListener { snapshot ->
                    val currentAmount =
                        snapshot.child("currentAmount").getValue(Double::class.java) ?: 0.0
                    val newAmount = currentAmount + expense.amount

                    categoryRef.child("currentAmount").setValue(newAmount)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener {
                            onFailure(it)
                        }

                }.addOnFailureListener {
                    onFailure(it)
                }

            }
            .addOnFailureListener { onFailure(it) }
    }

    fun loadExpenses(onLoaded: (List<Expense>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        getDatabase().child("Expenses").child(userId)
            .get()
            .addOnSuccessListener { snap ->
                val expenses = snap.children.mapNotNull { it.getValue(Expense::class.java) }
                onLoaded(expenses)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteExpense(expenseId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        getDatabase().child("Expenses").child(userId).child(expenseId)
            .removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun clearExpenses(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        getDatabase().child("Expenses").child(userId)
            .removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // --- CHALLENGES ---

    fun saveChallenge(challenge: Challenge, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        getDatabase().child("Challenges").child(userId).push()
            .setValue(challenge)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun loadChallenges(onLoaded: (List<Challenge>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        getDatabase().child("Challenges").child(userId)
            .get()
            .addOnSuccessListener { snap ->
                val challenges = snap.children.mapNotNull { it.getValue(Challenge::class.java) }
                onLoaded(challenges)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteChallenge(
        challengeId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        getDatabase().child("Challenges").child(userId).child(challengeId)
            .removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // --- SAVING GOALS ---

    fun saveSavingGoal(goal: SavingGoal, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        getDatabase().child("SavingGoals").child(userId).push()
            .setValue(goal)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun loadSavingGoals(onLoaded: (List<SavingGoal>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        getDatabase().child("SavingGoals").child(userId)
            .get()
            .addOnSuccessListener { snap ->
                val goals = snap.children.mapNotNull { it.getValue(SavingGoal::class.java) }
                onLoaded(goals)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteSavingGoal(goalId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        getDatabase().child("SavingGoals").child(userId).child(goalId)
            .removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // --- ACHIEVEMENTS ---

    fun saveAchievement(
        achievement: Achievement,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getUserId() ?: run {
            onFailure(Exception("User ID not found"))
            return
        }

        getDatabase().child("UserGoals").child(userId).child("achievements").push()
            .setValue(achievement)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

//    fun loadAchievements(onLoaded: (List<Achievement>) -> Unit, onFailure: (Exception) -> Unit) {
//        val userId = getUserIdOrNull() ?: run {
//            onFailure(Exception("User ID not found"))
//            return
//        }
//
//        getDatabase().child("UserGoals").child(userId).child("achievements")
//            .get()
//            .addOnSuccessListener { snap ->
//                val achievements = snap.children.mapNotNull { it.getValue(Achievement::class.java) }
//                onLoaded(achievements)
//            }
//            .addOnFailureListener { onFailure(it) }
//    }

    // --- RECOMMENDED GOALS ---

    fun saveRecommendedGoal(
        goal: SavingGoal,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getDatabase().child("Goals").child("recommendedGoals").push()
            .setValue(goal)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun loadRecommendedGoals(onLoaded: (List<SavingGoal>) -> Unit, onFailure: (Exception) -> Unit) {
        getDatabase().child("Goals").child("recommendedGoals")
            .get()
            .addOnSuccessListener { snap ->
                val goals = snap.children.mapNotNull { it.getValue(SavingGoal::class.java) }
                onLoaded(goals)
            }
            .addOnFailureListener { onFailure(it) }
    }
}