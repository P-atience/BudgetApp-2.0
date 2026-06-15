package com.budgettracker.ui.gamification

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.FragmentGameBinding
import com.budgettracker.util.GamificationManager
import com.budgettracker.util.SessionManager

class GameFragment : Fragment() {
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        sessionManager = SessionManager(requireContext())

        binding.btnPlayGame.setOnClickListener {
            startActivity(Intent(requireContext(), GameActivity::class.java))
        }

        loadBadges()
    }

    override fun onResume() {
        super.onResume()
        loadBadges()
    }

    private fun loadBadges() {
        val userId = sessionManager.getUserId()
        db.badgeDao().getBadgesForUser(userId).observe(viewLifecycleOwner) { badges ->
            val earnedTypes = badges.map { it.badgeType }.toSet()
            val adapter = BadgeAdapter(GamificationManager.ALL_BADGES, earnedTypes)
            binding.rvBadges.layoutManager = LinearLayoutManager(requireContext())
            binding.rvBadges.adapter = adapter
            binding.tvBadgeCount.text = "${badges.size} / ${GamificationManager.ALL_BADGES.size} badges earned"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
