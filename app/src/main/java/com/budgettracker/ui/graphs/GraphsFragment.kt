package com.budgettracker.ui.graphs

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.FragmentGraphsBinding
import com.budgettracker.util.DateUtils
import com.budgettracker.util.SessionManager
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import java.util.Calendar

class GraphsFragment : Fragment() {
    private var _binding: FragmentGraphsBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    private var startDate = ""
    private var endDate = ""

    private val COLORS = listOf(
        Color.parseColor("#FF6B6B"), Color.parseColor("#4ECDC4"),
        Color.parseColor("#45B7D1"), Color.parseColor("#96CEB4"),
        Color.parseColor("#FFEAA7"), Color.parseColor("#DDA0DD"),
        Color.parseColor("#98D8C8"), Color.parseColor("#F7DC6F")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGraphsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        sessionManager = SessionManager(requireContext())

        val now = Calendar.getInstance()
        val (s, e) = DateUtils.getMonthStartEnd(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1)
        startDate = s
        endDate = e
        updateDateLabels()

        binding.btnStartDate.setOnClickListener { pickDate(true) }
        binding.btnEndDate.setOnClickListener { pickDate(false) }

        loadCharts()
    }

    override fun onResume() {
        super.onResume()
        loadCharts()
    }

    private fun pickDate(isStart: Boolean) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val picked = "%04d-%02d-%02d".format(y, m + 1, d)
            if (isStart) startDate = picked else endDate = picked
            updateDateLabels()
            loadCharts()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateLabels() {
        binding.btnStartDate.text = DateUtils.displayDate(startDate)
        binding.btnEndDate.text = DateUtils.displayDate(endDate)
    }

    private fun loadCharts() {
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            val totals = db.expenseDao().getCategoryTotalsSync(userId, startDate, endDate)
            val goal = db.spendingGoalDao().getLatestGoalSync(userId)
            val totalSpent = db.expenseDao().getTotalSpent(userId, startDate, endDate) ?: 0.0

            activity?.runOnUiThread {
                if (totals.isEmpty()) {
                    binding.chartBar.visibility = View.GONE
                    binding.chartPie.visibility = View.GONE
                    binding.tvNoData.visibility = View.VISIBLE
                    return@runOnUiThread
                }

                binding.tvNoData.visibility = View.GONE
                binding.chartBar.visibility = View.VISIBLE
                binding.chartPie.visibility = View.VISIBLE

                // Bar chart
                val barEntries = totals.mapIndexed { i, ct -> BarEntry(i.toFloat(), ct.total.toFloat()) }
                val barDataSet = BarDataSet(barEntries, "Amount (R)").apply {
                    colors = COLORS.take(totals.size)
                    valueTextSize = 10f
                }
                binding.chartBar.apply {
                    data = BarData(barDataSet)
                    xAxis.valueFormatter = IndexAxisValueFormatter(totals.map { it.categoryName.take(8) })
                    xAxis.granularity = 1f
                    xAxis.labelRotationAngle = -30f
                    description.isEnabled = false
                    legend.isEnabled = true
                    setFitBars(true)

                    // Add goal lines
                    axisLeft.removeAllLimitLines()
                    goal?.let {
                        val minLine = LimitLine(it.minimumGoal.toFloat(), "Min Goal").apply {
                            lineColor = Color.parseColor("#FF9800")
                            lineWidth = 2f
                            textColor = Color.parseColor("#FF9800")
                        }
                        val maxLine = LimitLine(it.maximumGoal.toFloat(), "Max Goal").apply {
                            lineColor = Color.parseColor("#F44336")
                            lineWidth = 2f
                            textColor = Color.parseColor("#F44336")
                        }
                        axisLeft.addLimitLine(minLine)
                        axisLeft.addLimitLine(maxLine)
                    }
                    invalidate()
                    animateY(800)
                }

                // Pie chart
                val pieEntries = totals.mapIndexed { i, ct ->
                    PieEntry(ct.total.toFloat(), ct.categoryName.take(10))
                }
                val pieDataSet = PieDataSet(pieEntries, "").apply {
                    colors = COLORS.take(totals.size)
                    valueTextSize = 11f
                    sliceSpace = 3f
                }
                binding.chartPie.apply {
                    data = PieData(pieDataSet)
                    description.isEnabled = false
                    isDrawHoleEnabled = true
                    holeRadius = 40f
                    centerText = "R %.0f".format(totalSpent)
                    setCenterTextSize(14f)
                    legend.isEnabled = true
                    invalidate()
                    animateY(800)
                }

                binding.tvTotalSummary.text = "Total Spent: R %.2f".format(totalSpent)
                goal?.let {
                    val status = when {
                        totalSpent < it.minimumGoal -> "Under minimum goal"
                        totalSpent > it.maximumGoal -> "⚠️ Over budget!"
                        else -> "✅ Within budget goal"
                    }
                    binding.tvGoalStatus.text = status
                    binding.tvGoalStatus.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
