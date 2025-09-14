package com.example.odysseyapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.odysseyapp.R
import com.example.odysseyapp.Services.FirebaseHelper

class GoalsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_goals, container, false)

        loadRecommendedGoals(view)
        loadCurrentGoalProgress(view)
        //loadAchievements(view)

        return view
    }

    // Load current saving goal progress
    private fun loadCurrentGoalProgress(view: View) {
        FirebaseHelper.loadSavingGoals(
            onLoaded = { goals ->
                // Assume first goal is current (or filter if needed)
                if (goals.isNotEmpty()) {
                    val currentGoal = goals.first()

                    val progressBar = view.findViewById<ProgressBar>(R.id.goalProgressBar)
                    val progressText = view.findViewById<TextView>(R.id.goalProgressText)

                    val percent =
                        (currentGoal.currentAmount.toFloat() / currentGoal.targetAmount.toFloat() * 100).toInt()
                    progressBar.progress = percent
                    progressText.text =
                        "R${currentGoal.currentAmount} saved of R${currentGoal.targetAmount}"
                }
            },
            onFailure = {
                Toast.makeText(requireContext(), "Failed to load saving goals", Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }

    // Load achievements
//    private fun loadAchievements(view: View) {
//        val container = view.findViewById<LinearLayout>(R.id.achievementsContainer)
//
//        FirebaseHelper.loadAchievements(
//            onLoaded = { achievements ->
//                for (achievement in achievements) {
//                    val textView = TextView(requireContext()).apply {
//                        text = "🏆 ${achievement.title}"
//                        textSize = 16f
//                        setPadding(4, 4, 4, 4)
//                    }
//                    container.addView(textView)
//                }
//            },
//            onFailure = {
//                Toast.makeText(requireContext(), "Failed to load achievements", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        )
//    }

    // Load recommended goals
    private fun loadRecommendedGoals(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.recommendedGoalsContainer)

        FirebaseHelper.loadRecommendedGoals(
            onLoaded = { goals ->
                for (goal in goals) {
                    val goalView = TextView(requireContext()).apply {
                        text = "${goal.title} (Target: R${goal.targetAmount})"
                        textSize = 16f
                        setPadding(8, 8, 8, 8)
                    }
                    container.addView(goalView)
                }
            },
            onFailure = {
                Toast.makeText(
                    requireContext(),
                    "Failed to load recommended goals",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}