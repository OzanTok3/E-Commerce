package com.ozantok.ecommerce.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozantok.ecommerce.data.local.db.ProductEntity
import com.ozantok.ecommerce.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repo: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    data class DetailState(
        val isLoading: Boolean = true,
        val error: String? = null,
        val product: ProductEntity? = null
    )

    companion object {
        const val ARG_PRODUCT_ID = "productId"
    }

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state

    private val productId: String = checkNotNull(savedStateHandle[ARG_PRODUCT_ID]) {
        "DetailFragment requires '$ARG_PRODUCT_ID' arg"
    }

    init {
        load()
    }

    fun retry() = load()

    private fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            val product = repo.getProductById(productId)
            _state.value = if (product == null) {
                DetailState(isLoading = false, error = "No product found.")
            } else {
                DetailState(isLoading = false, product = product)
            }
        } catch (t: Throwable) {
            _state.value = DetailState(
                isLoading = false,
                error = t.message ?: "Something went wrong."
            )
        }
    }
}
