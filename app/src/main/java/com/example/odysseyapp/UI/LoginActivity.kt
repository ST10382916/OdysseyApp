package com.example.odysseyapp.UI

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.odysseyapp.MainActivity
import com.example.odysseyapp.R
import com.example.odysseyapp.Services.FirebaseHelper
import com.example.odysseyapp.Utils.SessionManager
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseHelper.initialize(this)
        auth = FirebaseAuth.getInstance()

        // ✅ Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Save session if not saved
            SessionManager.saveUser(this, currentUser.uid, currentUser.email ?: "")

            // Show welcome back toast
            Toast.makeText(this, "Welcome back, ${currentUser.email}", Toast.LENGTH_SHORT).show()

            // Go to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerRedirect = findViewById<TextView>(R.id.registerText)

        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                Toast.makeText(this, "Attempting to log in...", Toast.LENGTH_SHORT).show()

                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                        val user = auth.currentUser

                        // ✅ Save session
                        SessionManager.saveUser(this, user?.uid ?: "", user?.email ?: "")

                        // ✅ Go to dashboard
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(
                            this,
                            "Login failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } else {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        registerRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}