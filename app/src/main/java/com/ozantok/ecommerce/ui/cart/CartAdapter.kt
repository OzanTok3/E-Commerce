package com.ozantok.ecommerce.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ozantok.ecommerce.data.local.db.CartItemEntity
import com.ozantok.ecommerce.databinding.ItemCartBinding
import com.ozantok.ecommerce.utils.toTurkishCurrency

class CartAdapter(
    private val onMinusClicked: (CartItemEntity) -> Unit,
    private val onPlusClicked: (CartItemEntity) -> Unit
) : ListAdapter<CartItemEntity, CartAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItemEntity) = with(binding) {
            textViewName.text = item.name
            textViewPrice.text = item.unitPrice.toTurkishCurrency()
            textViewAmount.text = item.quantity.toString()

            textViewButtonMinus.setOnClickListener { onMinusClicked(item) }
            textViewButtonPlus.setOnClickListener { onPlusClicked(item) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<CartItemEntity>() {
            override fun areItemsTheSame(
                cartItemEntityOld: CartItemEntity,
                cartItemEntityNew: CartItemEntity
            ) =
                cartItemEntityOld.productId == cartItemEntityNew.productId

            override fun areContentsTheSame(
                cartItemEntityOld: CartItemEntity,
                cartItemEntityNew: CartItemEntity
            ) = cartItemEntityOld == cartItemEntityNew
        }
    }
}
