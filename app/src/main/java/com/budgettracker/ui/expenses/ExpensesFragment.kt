package com.budgettracker.ui.expenses

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgettracker.databinding.FragmentExpensesBinding
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.util.DateUtils
import com.budgettracker.util.SessionManager
import java.util.Calendar

class ExpensesFragment : Fragment() {
    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: ExpenseAdapter

    private var startDate = ""
    private var endDate = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        sessionManager = SessionManager(requireContext())

        // Default to current month
        val now = Calendar.getInstance()
        val (s, e) = DateUtils.getMonthStartEnd(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1)
        startDate = s
        endDate = e

        adapter = ExpenseAdapter { expense ->
            val intent = Intent(requireContext(), AddEditExpenseActivity::class.java)
            intent.putExtra("expenseId", expense.id)
            startActivity(intent)
        }
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter

        updateDateRangeLabels()

        binding.btnStartDate.setOnClickListener { pickDate(true) }
        binding.btnEndDate.setOnClickListener { pickDate(false) }

        binding.fabAddExpense.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditExpenseActivity::class.java))
        }

        loadExpenses()
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }

    private fun pickDate(isStart: Boolean) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val picked = "%04d-%02d-%02d".format(year, month + 1, day)
            if (isStart) startDate = picked else endDate = picked
            updateDateRangeLabels()
            loadExpenses()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateRangeLabels() {
        binding.btnStartDate.text = DateUtils.displayDate(startDate)
        binding.btnEndDate.text = DateUtils.displayDate(endDate)
    }

    private fun loadExpenses() {
        val userId = sessionManager.getUserId()
        db.expenseDao().getExpensesByDateRange(userId, startDate, endDate)
            .observe(viewLifecycleOwner) { expenses ->
                adapter.submitList(expenses)
                binding.tvEmpty.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
                binding.tvTotalAmount.text = "Total: R %.2f".format(expenses.sumOf { it.amount })
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
