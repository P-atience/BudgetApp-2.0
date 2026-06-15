package com.budgettracker.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.FragmentDashboardBinding
import com.budgettracker.util.DateUtils
import com.budgettracker.util.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        sessionManager = SessionManager(requireContext())
        loadDashboardData()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val userId = sessionManager.getUserId()
        val now = Calendar.getInstance()
        val month = now.get(Calendar.MONTH) + 1
        val year = now.get(Calendar.YEAR)
        val (start, end) = DateUtils.getMonthStartEnd(year, month)

        lifecycleScope.launch {
            val totalSpent = db.expenseDao().getTotalSpent(userId, start, end) ?: 0.0
            val goal = db.spendingGoalDao().getGoalForMonth(userId, month, year)
            val user = db.userDao().getUserById(userId)
            val badges = db.badgeDao().getBadgesForUserSync(userId)
            val expenseCount = db.expenseDao().getExpenseCount(userId)

            activity?.runOnUiThread {
                binding.tvWelcome.text = "Welcome, ${sessionManager.getUsername()}!"
                binding.tvMonthLabel.text = "${DateUtils.monthName(month)} $year"
                binding.tvTotalSpent.text = "R %.2f".format(totalSpent)
                binding.tvExpenseCount.text = "$expenseCount total expenses"
                binding.tvPoints.text = "${user?.totalPoints ?: 0} pts"
                binding.tvBadgeCount.text = "${badges.size} badges"

                if (goal != null) {
                    binding.cardGoal.visibility = View.VISIBLE
                    binding.tvMinGoal.text = "Min: R %.2f".format(goal.minimumGoal)
                    binding.tvMaxGoal.text = "Max: R %.2f".format(goal.maximumGoal)

                    val progress = when {
                        goal.maximumGoal <= 0 -> 0
                        else -> ((totalSpent / goal.maximumGoal) * 100).toInt().coerceIn(0, 100)
                    }
                    binding.progressSpending.progress = progress

                    val statusText: String
                    val statusColor: Int
                    when {
                        totalSpent < goal.minimumGoal -> {
                            statusText = "Under minimum goal — keep spending wisely!"
                            statusColor = Color.parseColor("#FF9800")
                        }
                        totalSpent > goal.maximumGoal -> {
                            statusText = "⚠️ Over budget! Time to cut back."
                            statusColor = Color.parseColor("#F44336")
                        }
                        else -> {
                            statusText = "✅ On track! Great budgeting!"
                            statusColor = Color.parseColor("#4CAF50")
                        }
                    }
                    binding.tvGoalStatus.text = statusText
                    binding.tvGoalStatus.setTextColor(statusColor)
                } else {
                    binding.cardGoal.visibility = View.VISIBLE
                    binding.tvGoalStatus.text = "No goal set for this month — set one in Goals!"
                    binding.tvGoalStatus.setTextColor(Color.parseColor("#9E9E9E"))
                    binding.tvMinGoal.text = ""
                    binding.tvMaxGoal.text = ""
                }

                // Recent badges
                if (badges.isNotEmpty()) {
                    val recentBadge = badges.first()
                    binding.tvLatestBadge.text = "Latest: ${recentBadge.badgeName}"
                    binding.tvLatestBadge.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
