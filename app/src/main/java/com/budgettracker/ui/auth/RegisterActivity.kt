package com.budgettracker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.data.entity.Category
import com.budgettracker.data.entity.User
import com.budgettracker.databinding.ActivityRegisterBinding
import com.budgettracker.ui.dashboard.MainActivity
import com.budgettracker.util.SessionManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create Account"

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (!validateInputs(username, password, confirmPassword)) return@setOnClickListener

            binding.progressBar.visibility = View.VISIBLE
            binding.btnRegister.isEnabled = false

            lifecycleScope.launch {
                val existing = db.userDao().getUserByUsername(username)
                if (existing != null) {
                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        binding.tilUsername.error = "Username already taken"
                    }
                    return@launch
                }

                val userId = db.userDao().insert(User(username = username, password = password)).toInt()

                // Seed default categories for new user
                val defaultCategories = listOf(
                    Category(userId = userId, name = "Food & Dining", iconName = "ic_food", colorHex = "#FF6B6B"),
                    Category(userId = userId, name = "Transport", iconName = "ic_transport", colorHex = "#4ECDC4"),
                    Category(userId = userId, name = "Entertainment", iconName = "ic_entertainment", colorHex = "#45B7D1"),
                    Category(userId = userId, name = "Shopping", iconName = "ic_shopping", colorHex = "#96CEB4"),
                    Category(userId = userId, name = "Health", iconName = "ic_health", colorHex = "#FFEAA7"),
                    Category(userId = userId, name = "Utilities", iconName = "ic_utilities", colorHex = "#DDA0DD"),
                    Category(userId = userId, name = "Other", iconName = "ic_other", colorHex = "#B0BEC5")
                )
                defaultCategories.forEach { db.categoryDao().insert(it) }

                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    sessionManager.saveSession(userId, username, false)
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finishAffinity()
                }
            }
        }
    }

    private fun validateInputs(username: String, password: String, confirm: String): Boolean {
        var valid = true
        binding.tilUsername.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        if (username.length < 3) {
            binding.tilUsername.error = "Username must be at least 3 characters"
            valid = false
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            valid = false
        }
        if (password != confirm) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            valid = false
        }
        return valid
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
