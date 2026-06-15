package com.budgettracker.ui.expenses

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.budgettracker.R
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.data.entity.Expense
import com.budgettracker.databinding.ActivityAddEditExpenseBinding
import com.budgettracker.util.DateUtils
import com.budgettracker.util.GamificationManager
import com.budgettracker.util.SessionManager
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddEditExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditExpenseBinding
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    private var expenseId: Int = -1
    private var currentPhotoPath: String? = null
    private var photoUri: Uri? = null
    private var selectedDate = DateUtils.today()
    private var selectedStartTime = "09:00"
    private var selectedEndTime = "10:00"
    private var selectedCategory = ""

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoPath != null) {
            showPhoto(currentPhotoPath!!)
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val file = createImageFile()
            currentPhotoPath = file.absolutePath
            contentResolver.openInputStream(it)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            showPhoto(currentPhotoPath!!)
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms.values.all { it }) launchCamera()
        else Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        expenseId = intent.getIntExtra("expenseId", -1)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (expenseId == -1) "Add Expense" else "Edit Expense"

        updateDateButton()
        updateTimeButtons()
        loadCategories()

        if (expenseId != -1) {
            loadExpense()
        }

        setupListeners()
    }

    private fun loadCategories() {
        val userId = sessionManager.getUserId()
        db.categoryDao().getCategoriesForUser(userId).observe(this) { categories ->
            binding.categoryRadioGroup.removeAllViews()

            val defaultCategories = listOf("Food & Dining", "Transport", "Entertainment", "Shopping", "Health", "Utilities")
            val allNames = (categories.map { it.name } + defaultCategories).distinct()

            allNames.forEach { name ->
                val rb = RadioButton(this)
                rb.text = name
                rb.id = View.generateViewId()
                rb.tag = name
                if (name == selectedCategory) rb.isChecked = true
                binding.categoryRadioGroup.addView(rb)
            }

            // Other option
            val other = RadioButton(this)
            other.text = "Other (specify)"
            other.id = View.generateViewId()
            other.tag = "OTHER"
            binding.categoryRadioGroup.addView(other)

            binding.categoryRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                val rb = group.findViewById<RadioButton>(checkedId)
                if (rb?.tag == "OTHER") {
                    binding.tilCustomCategory.visibility = View.VISIBLE
                } else {
                    binding.tilCustomCategory.visibility = View.GONE
                    selectedCategory = rb?.tag?.toString() ?: ""
                }
            }
        }
    }

    private fun loadExpense() {
        lifecycleScope.launch {
            val expense = db.expenseDao().getAllExpensesForUser(sessionManager.getUserId()).value
                ?: return@launch
            // Load from DB directly
            val allExpenses = db.expenseDao().getExpensesByDateRangeSync(sessionManager.getUserId(), "2000-01-01", "2100-12-31")
            val e = allExpenses.find { it.id == expenseId } ?: return@launch
            runOnUiThread {
                binding.etAmount.setText(e.amount.toString())
                binding.etDescription.setText(e.description)
                selectedDate = e.date
                selectedStartTime = e.startTime
                selectedEndTime = e.endTime
                selectedCategory = e.categoryName
                currentPhotoPath = e.photoPath
                updateDateButton()
                updateTimeButtons()
                if (!currentPhotoPath.isNullOrEmpty()) showPhoto(currentPhotoPath!!)

                // Check correct radio
                for (i in 0 until binding.categoryRadioGroup.childCount) {
                    val child = binding.categoryRadioGroup.getChildAt(i) as? RadioButton
                    if (child?.tag?.toString() == e.categoryName) {
                        child.isChecked = true
                        break
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                selectedDate = "%04d-%02d-%02d".format(y, m + 1, d)
                updateDateButton()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnStartTime.setOnClickListener {
            val parts = selectedStartTime.split(":")
            TimePickerDialog(this, { _, h, m ->
                selectedStartTime = "%02d:%02d".format(h, m)
                updateTimeButtons()
            }, parts[0].toInt(), parts[1].toInt(), true).show()
        }

        binding.btnEndTime.setOnClickListener {
            val parts = selectedEndTime.split(":")
            TimePickerDialog(this, { _, h, m ->
                selectedEndTime = "%02d:%02d".format(h, m)
                updateTimeButtons()
            }, parts[0].toInt(), parts[1].toInt(), true).show()
        }

        binding.btnTakePhoto.setOnClickListener {
            val perms = arrayOf(Manifest.permission.CAMERA)
            if (perms.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
                launchCamera()
            } else {
                permissionLauncher.launch(perms)
            }
        }

        binding.btnChoosePhoto.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnRemovePhoto.setOnClickListener {
            currentPhotoPath = null
            binding.ivExpensePhoto.visibility = View.GONE
            binding.btnRemovePhoto.visibility = View.GONE
        }

        binding.btnSave.setOnClickListener { saveExpense() }

        if (expenseId != -1) {
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnDelete.setOnClickListener { deleteExpense() }
        }
    }

    private fun launchCamera() {
        val file = createImageFile()
        currentPhotoPath = file.absolutePath
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        cameraLauncher.launch(photoUri)
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("EXPENSE_${timestamp}_", ".jpg", dir)
    }

    private fun showPhoto(path: String) {
        binding.ivExpensePhoto.visibility = View.VISIBLE
        binding.btnRemovePhoto.visibility = View.VISIBLE
        Glide.with(this).load(File(path)).centerCrop().into(binding.ivExpensePhoto)
    }

    private fun updateDateButton() {
        binding.btnDate.text = DateUtils.displayDate(selectedDate)
    }

    private fun updateTimeButtons() {
        binding.btnStartTime.text = selectedStartTime
        binding.btnEndTime.text = selectedEndTime
    }

    private fun getSelectedCategory(): String {
        val checkedId = binding.categoryRadioGroup.checkedRadioButtonId
        if (checkedId == -1) return ""
        val rb = binding.categoryRadioGroup.findViewById<RadioButton>(checkedId)
        return if (rb?.tag == "OTHER") {
            binding.etCustomCategory.text.toString().trim()
        } else {
            rb?.tag?.toString() ?: ""
        }
    }

    private fun saveExpense() {
        val amountStr = binding.etAmount.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val category = getSelectedCategory()

        // Validation
        var valid = true
        if (amountStr.isEmpty()) {
            binding.tilAmount.error = "Amount is required"
            valid = false
        } else {
            binding.tilAmount.error = null
            val amt = amountStr.toDoubleOrNull()
            if (amt == null || amt <= 0) {
                binding.tilAmount.error = "Enter a valid positive amount"
                valid = false
            }
        }
        if (description.isEmpty()) {
            binding.tilDescription.error = "Description is required"
            valid = false
        } else { binding.tilDescription.error = null }
        if (category.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            valid = false
        }
        if (selectedStartTime >= selectedEndTime) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
            valid = false
        }
        if (!valid) return

        val amount = amountStr.toDouble()
        val userId = sessionManager.getUserId()

        lifecycleScope.launch {
            if (expenseId == -1) {
                db.expenseDao().insert(
                    Expense(
                        userId = userId,
                        categoryId = null,
                        categoryName = category,
                        amount = amount,
                        description = description,
                        date = selectedDate,
                        startTime = selectedStartTime,
                        endTime = selectedEndTime,
                        photoPath = currentPhotoPath
                    )
                )
            } else {
                val existing = db.expenseDao().getExpensesByDateRangeSync(userId, "2000-01-01", "2100-12-31")
                    .find { it.id == expenseId }
                existing?.let {
                    db.expenseDao().update(
                        it.copy(
                            categoryName = category,
                            amount = amount,
                            description = description,
                            date = selectedDate,
                            startTime = selectedStartTime,
                            endTime = selectedEndTime,
                            photoPath = currentPhotoPath
                        )
                    )
                }
            }

            // Check badges
            val newBadges = GamificationManager.checkAndAwardBadges(
                userId, db.userDao(), db.expenseDao(), db.badgeDao(), db.spendingGoalDao()
            )
            if (currentPhotoPath != null) {
                GamificationManager.awardBadgeDirectly(userId, "PHOTO_PRO", db.badgeDao(), db.userDao())
            }

            runOnUiThread {
                if (newBadges.isNotEmpty()) {
                    val msg = "🏅 Badge earned: ${newBadges.first().name}!"
                    Toast.makeText(this@AddEditExpenseActivity, msg, Toast.LENGTH_LONG).show()
                }
                finish()
            }
        }
    }

    private fun deleteExpense() {
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            val expense = db.expenseDao().getExpensesByDateRangeSync(userId, "2000-01-01", "2100-12-31")
                .find { it.id == expenseId }
            expense?.let { db.expenseDao().delete(it) }
            runOnUiThread {
                Toast.makeText(this@AddEditExpenseActivity, "Expense deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
