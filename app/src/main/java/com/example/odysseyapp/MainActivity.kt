package com.example.odysseyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.odysseyapp.Services.FirebaseHelper
import com.example.odysseyapp.UI.AddExpenseFragment
import com.example.odysseyapp.UI.AiAssistantFragment
import com.example.odysseyapp.UI.DashboardFragment
import com.example.odysseyapp.UI.ExpenseListFragment
import com.example.odysseyapp.UI.GoalsFragment
import com.example.odysseyapp.UI.LoginActivity
import com.example.odysseyapp.UI.ProgressDashboardFragment
import com.example.odysseyapp.UI.SettingsFragment
import com.example.odysseyapp.UI.SpendingGraphFragment
import com.example.odysseyapp.UI.categoryFragment
import com.example.odysseyapp.Utils.SessionManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    //Firebase (2025). Firebase Realtime Database. Version not specified. Source code.
    // Available at: https://firebase.google.com/docs/database/?utm_source=studio [Accessed 2 May 2025].
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize FirebaseHelper
        FirebaseHelper.initialize(this)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)

        toolbar.title = "Welcome to Odyssey Finance!"
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // ✅ Check session
        val userEmail = SessionManager.getUserEmail(this)
        if (SessionManager.isLoggedIn(this)) {
            Toast.makeText(this, "Welcome back $userEmail!", Toast.LENGTH_SHORT).show()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardFragment())
                .commit()
            navigationView.setCheckedItem(R.id.nav_home)
            Toast.makeText(this, "Home loaded", Toast.LENGTH_SHORT).show()
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            val (fragment, title) = when (menuItem.itemId) {
                R.id.nav_home -> DashboardFragment() to "Home"
                R.id.nav_log_expense -> AddExpenseFragment() to "Log Expense"
                R.id.nav_set_category -> categoryFragment() to "Set Expense Category"
                R.id.nav_set_goals -> GoalsFragment() to "Goals and Achievements"
                R.id.nav_view_expenses -> ExpenseListFragment() to "Expense List"
                R.id.nav_progress_dashboard -> ProgressDashboardFragment() to "Budget Progress"
                R.id.nav_spending_graph -> SpendingGraphFragment() to "Spending Graph"
                R.id.nav_ai_advice -> AiAssistantFragment() to "AI Assistant"
                R.id.settingsFragment -> SettingsFragment() to "Settings"
                R.id.LogOutFragment -> {
                    logout()
                    null to null
                }

                else -> null to null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, it)
                    .commit()

                toolbar.title = title
                Toast.makeText(this, "$title loaded", Toast.LENGTH_SHORT).show()
            }

            drawerLayout.closeDrawers()
            true
        }
    }

    private fun logout() {

        FirebaseAuth.getInstance().signOut()
        SessionManager.logout(this)

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}