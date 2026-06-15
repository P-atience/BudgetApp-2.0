package com.budgettracker.ui.categories

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.budgettracker.data.entity.Category
import com.budgettracker.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onDelete: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(a: Category, b: Category) = a.id == b.id
            override fun areContentsTheSame(a: Category, b: Category) = a == b
        }
    }

    inner class ViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cat = getItem(position)
        with(holder.binding) {
            tvCategoryName.text = cat.name
            tvCustomTag.text = if (cat.isCustom) "Custom" else "Default"
            try {
                viewColorDot.setBackgroundColor(Color.parseColor(cat.colorHex))
            } catch (e: Exception) {
                viewColorDot.setBackgroundColor(Color.GRAY)
            }
            btnDelete.setOnClickListener { onDelete(cat) }
        }
    }
}
