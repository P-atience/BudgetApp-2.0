package com.budgettracker.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityUserDetailBinding
import com.budgettracker.util.DateUtils
import com.budgettracker.util.GamificationManager
import kotlinx.coroutines.launch
import java.util.Calendar

class UserDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserDetailBinding
    private lateinit var db: AppDatabase
    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        userId = intent.getIntExtra("userId", -1)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "User Details"

        if (userId == -1) { finish(); return }

        loadUserDetail()

        binding.btnAwardBudgetMaster.setOnClickListener {
            lifecycleScope.launch {
                val result = GamificationManager.awardBadgeDirectly(userId, "BUDGET_MASTER", db.badgeDao(), db.userDao())
                runOnUiThread {
                    if (result != null) Toast.makeText(this@UserDetailActivity, "Badge awarded!", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this@UserDetailActivity, "User already has this badge", Toast.LENGTH_SHORT).show()
                    loadUserDetail()
                }
            }
        }
    }

    private fun loadUserDetail() {
        val now = Calendar.getInstance()
        val month = now.get(Calendar.MONTH) + 1
        val year = now.get(Calendar.YEAR)
        val (start, end) = DateUtils.getMonthStartEnd(year, month)

        lifecycleScope.launch {
            val user = db.userDao().getUserById(userId) ?: return@launch
            val totalSpent = db.expenseDao().getTotalSpent(userId, start, end) ?: 0.0
            val goal = db.spendingGoalDao().getGoalForMonth(userId, month, year)
            val badges = db.badgeDao().getBadgesForUserSync(userId)
            val expenseCount = db.expenseDao().getExpenseCount(userId)

            runOnUiThread {
                binding.tvUsername.text = user.username
                binding.tvPoints.text = "Points: ${user.totalPoints}"
                binding.tvExpenseCount.text = "Total Expenses: $expenseCount"
                binding.tvMonthlySpend.text = "This Month: R%.2f".format(totalSpent)
                binding.tvBadges.text = "Badges: ${badges.size}"
                binding.tvBadgeList.text = badges.joinToString(", ") { it.badgeName }

                if (goal != null) {
                    val status = when {
                        totalSpent < goal.minimumGoal -> "Under minimum"
                        totalSpent > goal.maximumGoal -> "Over budget!"
                        else -> "On track ✅"
                    }
                    binding.tvGoalStatus.text = "Goal Status: $status (Min: R%.0f | Max: R%.0f)".format(goal.minimumGoal, goal.maximumGoal)
                } else {
                    binding.tvGoalStatus.text = "No goal set this month"
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
