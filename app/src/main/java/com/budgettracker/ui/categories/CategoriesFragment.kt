package com.budgettracker.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.data.entity.Category
import com.budgettracker.databinding.FragmentCategoriesBinding
import com.budgettracker.util.GamificationManager
import com.budgettracker.util.SessionManager
import kotlinx.coroutines.launch

class CategoriesFragment : Fragment() {
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        sessionManager = SessionManager(requireContext())

        adapter = CategoryAdapter(
            onDelete = { category ->
                lifecycleScope.launch {
                    db.categoryDao().delete(category)
                }
            }
        )
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = adapter

        val userId = sessionManager.getUserId()
        db.categoryDao().getCategoriesForUser(userId).observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        // Category selection chips for adding
        setupCategoryChips()

        binding.btnAddCategory.setOnClickListener {
            addCategory()
        }
    }

    private fun setupCategoryChips() {
        val presets = listOf("Food & Dining", "Transport", "Entertainment", "Shopping", "Health", "Utilities", "Education", "Gym", "Travel", "Insurance")
        binding.chipGroupPresets.removeAllViews()
        presets.forEach { name ->
            val chip = com.google.android.material.chip.Chip(requireContext())
            chip.text = name
            chip.isCheckable = true
            binding.chipGroupPresets.addView(chip)
            chip.setOnClickListener {
                binding.etCategoryName.setText(name)
            }
        }
    }

    private fun addCategory() {
        val name = binding.etCategoryName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilCategoryName.error = "Category name is required"
            return
        }
        if (name.length < 2) {
            binding.tilCategoryName.error = "Name must be at least 2 characters"
            return
        }
        binding.tilCategoryName.error = null

        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            db.categoryDao().insert(
                Category(userId = userId, name = name, isCustom = true)
            )
            // Award badge for first custom category
            GamificationManager.awardBadgeDirectly(userId, "CATEGORY_CREATOR", db.badgeDao(), db.userDao())

            activity?.runOnUiThread {
                binding.etCategoryName.text?.clear()
                Toast.makeText(requireContext(), "Category '$name' added!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
