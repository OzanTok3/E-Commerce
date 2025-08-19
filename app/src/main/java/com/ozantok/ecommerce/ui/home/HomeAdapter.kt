package com.ozantok.ecommerce.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ozantok.ecommerce.R
import com.ozantok.ecommerce.data.local.db.ProductEntity
import com.ozantok.ecommerce.databinding.ItemProductBinding
import com.ozantok.ecommerce.utils.toTurkishCurrency
import com.ozantok.ecommerce.utils.toTurkishCurrencyOrRaw

class HomeAdapter(
    private val onItemClick: (ProductEntity) -> Unit,
    private val onFavClick: (ProductEntity) -> Unit,
    private val onAddToCartClick: (ProductEntity) -> Unit
) : PagingDataAdapter<ProductEntity, HomeAdapter.ProductViewHolder>(ProductDiff) {

    private var favoriteIds: Set<String> = emptySet()

    fun submitFavoriteIds(newIds: Set<String>) {
        favoriteIds = newIds
        notifyItemRangeChanged(0, itemCount, FAV_PAYLOAD)
    }

    object ProductDiff : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.any { it === FAV_PAYLOAD }) {
            getItem(position)?.let { holder.updateFavoriteVisual(it) }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductEntity) = with(binding) {
            textViewName.text = product.name

            val numeric = product.price?.toString()?.replace(',', '.')?.toDoubleOrNull()
            textViewPrice.text = numeric?.toTurkishCurrency()
                ?: product.price?.toTurkishCurrencyOrRaw()
                        ?: "—"

            Glide.with(imageView)
                .load(product.image)
                .placeholder(R.color.dark_grey)
                .error(R.color.dark_grey)
                .into(imageView)

            updateFavoriteVisual(product)

            addFavorites.setOnClickListener { onFavClick(product) }
            textViewAddCart.setOnClickListener { onAddToCartClick(product) }
            root.setOnClickListener { onItemClick(product) }
        }

        fun updateFavoriteVisual(product: ProductEntity) = with(binding) {
            val selected = favoriteIds.contains(product.id)
            addFavorites.setImageResource(
                if (selected) R.drawable.icon_star_selected
                else R.drawable.icon_star_unselected
            )
        }
    }

    private companion object {
        val FAV_PAYLOAD = Any()
    }
}
