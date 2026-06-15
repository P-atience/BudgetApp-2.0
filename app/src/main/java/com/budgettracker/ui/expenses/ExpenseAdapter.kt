package com.budgettracker.ui.expenses

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.budgettracker.R
import com.budgettracker.data.entity.Expense
import com.budgettracker.databinding.ItemExpenseBinding
import com.budgettracker.util.DateUtils
import java.io.File

class ExpenseAdapter(
    private val onClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Expense>() {
            override fun areItemsTheSame(a: Expense, b: Expense) = a.id == b.id
            override fun areContentsTheSame(a: Expense, b: Expense) = a == b
        }
    }

    inner class ViewHolder(val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = getItem(position)
        with(holder.binding) {
            tvCategory.text = expense.categoryName
            tvDescription.text = expense.description
            tvAmount.text = "R %.2f".format(expense.amount)
            tvDate.text = DateUtils.displayDate(expense.date)
            tvTime.text = "${expense.startTime} - ${expense.endTime}"

            if (!expense.photoPath.isNullOrEmpty()) {
                val file = File(expense.photoPath)
                if (file.exists()) {
                    ivExpensePhoto.visibility = View.VISIBLE
                    Glide.with(root.context)
                        .load(Uri.fromFile(file))
                        .centerCrop()
                        .placeholder(R.drawable.ic_photo_placeholder)
                        .into(ivExpensePhoto)
                } else {
                    ivExpensePhoto.visibility = View.GONE
                }
            } else {
                ivExpensePhoto.visibility = View.GONE
            }

            root.setOnClickListener { onClick(expense) }
        }
    }
}
