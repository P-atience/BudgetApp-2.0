package com.budgettracker.ui.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.data.entity.SpendingGoal
import com.budgettracker.databinding.FragmentGoalsBinding
import com.budgettracker.util.DateUtils
import com.budgettracker.util.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar

class GoalsFragment : Fragment() {
    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        sessionManager = SessionManager(requireContext())

        val now = Calendar.getInstance()
        val month = now.get(Calendar.MONTH) + 1
        val year = now.get(Calendar.YEAR)

        binding.tvMonthYear.text = "${DateUtils.monthName(month)} $year"

        loadCurrentGoal(month, year)

        binding.btnSaveGoal.setOnClickListener {
            saveGoal(month, year)
        }
    }

    private fun loadCurrentGoal(month: Int, year: Int) {
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            val goal = db.spendingGoalDao().getGoalForMonth(userId, month, year)
            activity?.runOnUiThread {
                if (goal != null) {
                    binding.etMinGoal.setText(goal.minimumGoal.toString())
                    binding.etMaxGoal.setText(goal.maximumGoal.toString())
                    binding.tvCurrentGoal.text = "Current: Min R%.2f | Max R%.2f".format(goal.minimumGoal, goal.maximumGoal)
                    binding.tvCurrentGoal.visibility = View.VISIBLE
                } else {
                    binding.tvCurrentGoal.text = "No goal set for this month yet"
                    binding.tvCurrentGoal.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun saveGoal(month: Int, year: Int) {
        val minStr = binding.etMinGoal.text.toString().trim()
        val maxStr = binding.etMaxGoal.text.toString().trim()

        binding.tilMinGoal.error = null
        binding.tilMaxGoal.error = null

        var valid = true
        if (minStr.isEmpty()) { binding.tilMinGoal.error = "Required"; valid = false }
        if (maxStr.isEmpty()) { binding.tilMaxGoal.error = "Required"; valid = false }
        if (!valid) return

        val min = minStr.toDoubleOrNull()
        val max = maxStr.toDoubleOrNull()

        if (min == null || min < 0) { binding.tilMinGoal.error = "Enter a valid amount"; return }
        if (max == null || max < 0) { binding.tilMaxGoal.error = "Enter a valid amount"; return }
        if (min > max) { binding.tilMinGoal.error = "Min must be less than Max"; return }

        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            val existing = db.spendingGoalDao().getGoalForMonth(userId, month, year)
            if (existing != null) {
                db.spendingGoalDao().update(existing.copy(minimumGoal = min, maximumGoal = max))
            } else {
                db.spendingGoalDao().insert(SpendingGoal(userId = userId, minimumGoal = min, maximumGoal = max, month = month, year = year))
            }
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Goal saved!", Toast.LENGTH_SHORT).show()
                loadCurrentGoal(month, year)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
