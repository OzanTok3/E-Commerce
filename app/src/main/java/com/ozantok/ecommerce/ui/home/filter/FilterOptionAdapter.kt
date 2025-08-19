package com.ozantok.ecommerce.ui.home.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ozantok.ecommerce.databinding.ItemFilterCheckBinding

class FilterOptionAdapter(
    private val selected: MutableSet<String>,
    private val onToggle: (String, Boolean) -> Unit
) : ListAdapter<String, FilterOptionAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemFilterCheckBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(label: String) = with(b) {
            this.label.text = label
            check.isChecked = selected.contains(label)

            root.setOnClickListener {
                check.isChecked = !check.isChecked
            }
            check.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selected.add(label) else selected.remove(label)
                onToggle(label, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemFilterCheckBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(o: String, n: String) = o == n
            override fun areContentsTheSame(o: String, n: String) = o == n
        }
    }
}
