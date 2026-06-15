package com.budgettracker.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.budgettracker.data.entity.User
import com.budgettracker.databinding.ItemAdminUserBinding

class AdminUserAdapter(
    private val onViewDetail: (Int) -> Unit
) : ListAdapter<User, AdminUserAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(a: User, b: User) = a.id == b.id
            override fun areContentsTheSame(a: User, b: User) = a == b
        }
    }

    inner class ViewHolder(val binding: ItemAdminUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemAdminUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        with(holder.binding) {
            tvUsername.text = user.username
            tvPoints.text = "${user.totalPoints} points"
            tvStreak.text = "Streak: ${user.currentStreak} days"
            btnViewDetail.setOnClickListener { onViewDetail(user.id) }
        }
    }
}
