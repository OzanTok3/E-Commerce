package com.ozantok.ecommerce.ui.favorites


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozantok.ecommerce.data.local.db.ProductEntity
import com.ozantok.ecommerce.data.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repo: FavoritesRepository
) : ViewModel() {

    val favoriteProducts: StateFlow<List<ProductEntity>> =
        repo.observeFavoriteProducts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteIds: StateFlow<Set<String>> =
        favoriteProducts
            .map { list -> list.map { it.id }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggle(id: String) = viewModelScope.launch {
        repo.toggle(id)
    }
}