package com.ozantok.ecommerce.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozantok.ecommerce.data.local.db.CartItemEntity
import com.ozantok.ecommerce.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repo: CartRepository
) : ViewModel() {

    val items: StateFlow<List<CartItemEntity>> =
        repo.observeCart()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val count: StateFlow<Int> =
        items.map { list -> list.sumOf { it.quantity } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val total: StateFlow<Double> =
        items.map { list -> list.sumOf { it.unitPrice * it.quantity } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun addOne(id: String, name: String, image: String, unitPrice: Double) =
        viewModelScope.launch { repo.addOne(id, name, image, unitPrice) }

    fun setQuantity(id: String, newAmount: Int) = viewModelScope.launch {
        if (newAmount < 1) repo.deleteById(id) else repo.updateQuantity(id, newAmount)
    }
}
