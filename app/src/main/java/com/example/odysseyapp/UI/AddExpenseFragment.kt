package com.example.odysseyapp.UI

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.odysseyapp.Model.CategoryBudget
import com.example.odysseyapp.Model.Expense
import com.example.odysseyapp.R
import com.example.odysseyapp.Services.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddExpenseFragment : Fragment() {

    private lateinit var expenseNameField: EditText
    private lateinit var expenseAmountField: EditText
    private lateinit var expenseDateField: EditText
    private lateinit var recurringCheckBox: CheckBox
    private lateinit var categorySpinner: Spinner
    private lateinit var addCategoryButton: Button
    private lateinit var saveExpenseButton: Button
    private lateinit var pickImageButton: Button
    private lateinit var receiptPreview: ImageView
    private lateinit var expenseDescription: EditText

    private var receiptUri: Uri? = null
    private val calendar = Calendar.getInstance()
    private val REQUEST_CAMERA = 101
    private val REQUEST_GALLERY = 102
    private var imageFilePath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_expense, container, false)

        expenseNameField = view.findViewById(R.id.expenseNameField)
        expenseAmountField = view.findViewById(R.id.expenseAmountField)
        expenseDateField = view.findViewById(R.id.expenseDateField)
        recurringCheckBox = view.findViewById(R.id.recurringCheckBox)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        addCategoryButton = view.findViewById(R.id.addCategoryButton)
        saveExpenseButton = view.findViewById(R.id.saveExpenseButton)
        pickImageButton = view.findViewById(R.id.pickImageButton)
        receiptPreview = view.findViewById(R.id.receiptPreview)
        expenseDescription = view.findViewById(R.id.expenseDescription)

        loadCategories()

        expenseDateField.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    expenseDateField.setText(sdf.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        addCategoryButton.setOnClickListener { showAddCategoryDialog() }
        pickImageButton.setOnClickListener { showImagePickerDialog() }
        saveExpenseButton.setOnClickListener { saveExpense() }

        return view
    }

    private fun saveExpense() {
        val name = expenseNameField.text.toString()
        val amount = expenseAmountField.text.toString().toDoubleOrNull() ?: 0.0
        val description = expenseDescription.text.toString()
        val date = calendar.time
        val category = categorySpinner.selectedItem?.toString() ?: ""
        val recurring = recurringCheckBox.isChecked

        if (name.isEmpty() || category.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val expense = Expense(
            name = name,
            amount = amount,
            date = date,
            description = description,
            category = category,
            isRecurring = recurring,
            imageUri = receiptUri?.toString()
        )

        FirebaseHelper.saveExpense(expense, {
            Toast.makeText(requireContext(), "Expense saved!", Toast.LENGTH_SHORT).show()
            clearFields()
        }, {
            Toast.makeText(requireContext(), "Failed to save expense", Toast.LENGTH_SHORT).show()
        })
    }

    private fun loadCategories(selectAfterLoad: String? = null) {
        FirebaseHelper.loadCategoryBudgets({ budgets ->
            val categories = budgets.map { it.categoryName }

            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter

            selectAfterLoad?.let {
                val index = categories.indexOf(it)
                if (index >= 0) categorySpinner.setSelection(index)
            }
        }, {
            Toast.makeText(requireContext(), "Failed to load categories", Toast.LENGTH_SHORT).show()
        })
    }

    private fun showAddCategoryDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, null)
        val inputField = dialogView.findViewById<EditText>(R.id.CatNametxt)
        val budgetField = dialogView.findViewById<EditText>(R.id.CatBidgettxt)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirm_button)
//Android Developers (2025). Dialogs. Version not specified.
// Source code. Available at: https://developer.android.com/develop/ui/views/components/dialogs [Accessed 2 May 2025].
        val dialog =
            AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(true).create()

        confirmButton.setOnClickListener {
            val categoryName = inputField.text.toString().trim()
            val budgetAmount = budgetField.text.toString().toDoubleOrNull()

            if (categoryName.isEmpty() || budgetAmount == null) {
                Toast.makeText(
                    requireContext(),
                    "Enter valid category name and budget amount",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val newBudget = CategoryBudget(
                categoryName = categoryName,
                budgetAmount = budgetAmount,
                currentAmount = 0.0,
                userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                isNew = true
            )

            FirebaseHelper.saveCategoryBudget(newBudget, {
                Toast.makeText(requireContext(), "Category added successfully!", Toast.LENGTH_SHORT)
                    .show()
                dialog.dismiss()
                loadCategories(selectAfterLoad = categoryName)
            }, {
                Toast.makeText(requireContext(), "Failed to save category", Toast.LENGTH_SHORT)
                    .show()
            })
        }

        dialog.show()
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Attach Receipt")
            .setItems(options) { _, which ->
                if (which == 0) openCamera() else openGallery()
            }.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun openCamera() {
        val imageFile = File(requireContext().filesDir, "receipt_${System.currentTimeMillis()}.jpg")
        imageFilePath = imageFile.absolutePath
        val imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            imageFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == android.app.Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_GALLERY -> {
                    data?.data?.let { uri ->
                        val bitmap = BitmapFactory.decodeStream(
                            requireContext().contentResolver.openInputStream(uri)
                        )
                        val file = File(
                            requireContext().filesDir,
                            "receipt_${System.currentTimeMillis()}.jpg"
                        )
                        FileOutputStream(file).use {
                            bitmap.compress(
                                Bitmap.CompressFormat.JPEG,
                                90,
                                it
                            )
                        }
                        receiptUri = Uri.fromFile(file)
                        receiptPreview.setImageURI(receiptUri)
                        receiptPreview.visibility = View.VISIBLE
                    }
                }

                REQUEST_CAMERA -> {
                    imageFilePath?.let {
                        receiptUri = Uri.fromFile(File(it))
                        receiptPreview.setImageURI(receiptUri)
                        receiptPreview.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun clearFields() {
        expenseNameField.text.clear()
        expenseAmountField.text.clear()
        expenseDateField.text.clear()
        recurringCheckBox.isChecked = false
        receiptPreview.setImageDrawable(null)
        receiptPreview.visibility = View.GONE
        expenseDescription.text.clear()
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }
}