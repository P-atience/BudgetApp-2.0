package com.budgettracker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityLoginBinding
import com.budgettracker.ui.admin.AdminDashboardActivity
import com.budgettracker.ui.dashboard.MainActivity
import com.budgettracker.util.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            navigateToApp()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (!validateInputs(username, password)) return@setOnClickListener

            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false

            lifecycleScope.launch {
                val user = db.userDao().login(username, password)
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    if (user != null) {
                        sessionManager.saveSession(user.id, user.username, user.isAdmin)
                        navigateToApp()
                    } else {
                        binding.tilUsername.error = null
                        binding.tilPassword.error = "Invalid username or password"
                    }
                }
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(username: String, password: String): Boolean {
        var isValid = true
        binding.tilUsername.error = null
        binding.tilPassword.error = null

        if (username.isEmpty()) {
            binding.tilUsername.error = "Username is required"
            isValid = false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        }
        return isValid
    }

    private fun navigateToApp() {
        val intent = if (sessionManager.isAdmin()) {
            Intent(this, AdminDashboardActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
