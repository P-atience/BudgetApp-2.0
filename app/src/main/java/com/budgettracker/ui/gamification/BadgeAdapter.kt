package com.budgettracker.ui.gamification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.budgettracker.databinding.ItemBadgeBinding
import com.budgettracker.util.GamificationManager

class BadgeAdapter(
    private val allBadges: List<GamificationManager.BadgeInfo>,
    private val earnedTypes: Set<String>
) : RecyclerView.Adapter<BadgeAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemBadgeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBadgeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val badge = allBadges[position]
        val earned = badge.type in earnedTypes
        with(holder.binding) {
            tvEmoji.text = badge.emoji
            tvBadgeName.text = badge.name
            tvBadgeDesc.text = badge.description
            tvPoints.text = "+${badge.points} pts"
            if (earned) {
                root.alpha = 1f
                tvStatus.text = "✅ Earned"
                tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            } else {
                root.alpha = 0.5f
                tvStatus.text = "🔒 Locked"
                tvStatus.setTextColor(android.graphics.Color.parseColor("#9E9E9E"))
            }
        }
    }

    override fun getItemCount() = allBadges.size
}
