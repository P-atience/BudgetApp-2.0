package com.budgettracker.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.budgettracker.R
import com.budgettracker.databinding.ActivityMainBinding
import com.budgettracker.ui.auth.LoginActivity
import com.budgettracker.ui.categories.CategoriesFragment
import com.budgettracker.ui.expenses.ExpensesFragment
import com.budgettracker.ui.gamification.GameFragment
import com.budgettracker.ui.goals.GoalsFragment
import com.budgettracker.ui.graphs.GraphsFragment
import com.budgettracker.util.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Budget Tracker"

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> loadFragment(DashboardFragment())
                R.id.nav_expenses -> loadFragment(ExpensesFragment())
                R.id.nav_categories -> loadFragment(CategoriesFragment())
                R.id.nav_goals -> loadFragment(GoalsFragment())
                R.id.nav_graphs -> loadFragment(GraphsFragment())
            }
            true
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_game -> {
                    startActivity(Intent(this, com.budgettracker.ui.gamification.GameActivity::class.java))
                    true
                }
                R.id.action_logout -> {
                    sessionManager.clearSession()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
}
