package com.budgettracker.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgettracker.R
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityAdminDashboardBinding
import com.budgettracker.ui.auth.LoginActivity
import com.budgettracker.util.DateUtils
import com.budgettracker.util.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: AdminUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Admin Dashboard"

        adapter = AdminUserAdapter { userId ->
            val intent = Intent(this, UserDetailActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter

        db.userDao().getAllRegularUsers().observe(this) { users ->
            adapter.submitList(users)
            binding.tvTotalUsers.text = "Total Users: ${users.size}"
        }

        loadStats()
    }

    private fun loadStats() {
        val now = Calendar.getInstance()
        val month = now.get(Calendar.MONTH) + 1
        val year = now.get(Calendar.YEAR)
        val (start, end) = DateUtils.getMonthStartEnd(year, month)

        lifecycleScope.launch {
            val badges = db.badgeDao().getAllBadges().value
            runOnUiThread {
                binding.tvMonth.text = "Month: ${DateUtils.monthName(month)} $year"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                sessionManager.clearSession()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
