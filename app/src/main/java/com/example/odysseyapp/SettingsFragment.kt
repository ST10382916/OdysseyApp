package com.example.odysseyapp.UI

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.odysseyapp.R
import com.example.odysseyapp.Services.FirebaseHelper
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SettingsFragment : Fragment() {

    private lateinit var fullNameField: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var updateButton: Button
    private lateinit var clearButton: Button

    private lateinit var sharedPrefs: SharedPreferences
    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        emailField = view.findViewById(R.id.emailField)
        passwordField = view.findViewById(R.id.passwordFieldtxt)
        updateButton = view.findViewById(R.id.updateButton)
        clearButton = view.findViewById(R.id.clearButton)

        sharedPrefs = requireActivity().getSharedPreferences("UserSession", 0)

        // Load existing data
        emailField.setText(FirebaseAuth.getInstance().currentUser?.email)

        updateButton.setOnClickListener {
            val newEmail = emailField.text.toString().trim()
            val password = passwordField.text.toString()

            if (newEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            updateProfile(newEmail, password)
        }

        clearButton.setOnClickListener {
            showClearDataConfirmation()
        }

        return view
    }

    private fun updateProfile(newEmail: String, password: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        val credential = EmailAuthProvider.getCredential(user.email ?: "", password)

        // Re-authenticate user
        user.reauthenticate(credential).addOnSuccessListener {

            // Update email in Authentication
            user.updateEmail(newEmail).addOnSuccessListener {


                // Update Realtime DB user info
                val userRef =
                    FirebaseDatabase.getInstance().getReference("users").child(user.uid)
                userRef.child("email").setValue(newEmail)

                // Update SharedPrefs
                with(sharedPrefs.edit()) {
                    putString("email", newEmail)
                    apply()
                }

                Toast.makeText(
                    requireContext(),
                    "Profile updated successfully!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }.addOnFailureListener {
            Toast.makeText(
                requireContext(),
                "Failed to update email: ${it.message}",
                Toast.LENGTH_SHORT
            ).show()
        }

            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Authentication failed: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showClearDataConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear all data")
            .setMessage("Are you sure you want to delete all expenses and budget categories? This cannot be undone.")
            .setPositiveButton("Yes") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllData() {
        FirebaseHelper.clearExpenses(
            onSuccess = {
                FirebaseHelper.clearCategoryBudgets(
                    onSuccess = {
                        Toast.makeText(requireContext(), "All data cleared!", Toast.LENGTH_SHORT)
                            .show()
                    },
                    onFailure = {
                        Toast.makeText(
                            requireContext(),
                            "Failed to clear categories",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onFailure = {
                Toast.makeText(requireContext(), "Failed to clear expenses", Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }
}