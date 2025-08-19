package com.ozantok.ecommerce.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ozantok.ecommerce.R
import com.ozantok.ecommerce.data.local.db.ProductEntity
import com.ozantok.ecommerce.databinding.ItemFavoritesBinding
import com.ozantok.ecommerce.utils.toTurkishCurrency
import com.ozantok.ecommerce.utils.toTurkishCurrencyOrRaw

class FavoritesAdapter(
    private val onItemClick: (ProductEntity) -> Unit,
    private val onToggleFavorite: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, FavoritesAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding =
            ItemFavoritesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val b: ItemFavoritesBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ProductEntity) = with(b) {
            title.text = item.name
            val numeric = item.price?.toString()?.replace(',', '.')?.toDoubleOrNull()
            price.text = numeric?.toTurkishCurrency()
                ?: item.price?.toTurkishCurrencyOrRaw()
                        ?: "—"

            Glide.with(image)
                .load(item.image)
                .placeholder(R.color.dark_grey)
                .error(R.color.dark_grey)
                .into(image)

            favBtn.setImageResource(R.drawable.icon_star_selected)

            root.setOnClickListener { onItemClick(item) }
            favBtn.setOnClickListener { onToggleFavorite(item) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ProductEntity>() {
            override fun areItemsTheSame(o: ProductEntity, n: ProductEntity) = o.id == n.id
            override fun areContentsTheSame(o: ProductEntity, n: ProductEntity) = o == n
        }
    }
}
