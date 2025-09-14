package com.example.odysseyapp.UI

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.odysseyapp.MainActivity
import com.example.odysseyapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
//Firebase (2025). Get Started with Firebase Authentication on Android. Version not specified.
// Source code. Available at: https://firebase.google.com/docs/auth/android/start [Accessed 2 May 2025].
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users")
        auth = FirebaseAuth.getInstance()

        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userMap = hashMapOf(
                                "uid" to user?.uid,
                                "email" to email
                            )

                            userRef.child(user?.uid ?: "").setValue(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Registration successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    sharedPreferences =
                                        getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putString("email", email)
                                    editor.putString("uid", user?.uid)
                                    editor.putBoolean("isLoggedIn", true)
                                    editor.apply()

                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Failed to save user info: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                        } else {
                            Toast.makeText(
                                this,
                                "Registration failed: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}