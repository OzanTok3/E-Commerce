package com.ozantok.ecommerce.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ozantok.ecommerce.data.local.db.ProductEntity
import com.ozantok.ecommerce.data.repository.ProductRepository
import com.ozantok.ecommerce.ui.home.filter.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _pagingFlow =
        MutableStateFlow<kotlinx.coroutines.flow.Flow<PagingData<ProductEntity>>?>(null)
    val pagingFlow: StateFlow<kotlinx.coroutines.flow.Flow<PagingData<ProductEntity>>?> =
        _pagingFlow.asStateFlow()

    init {
        rebuildPager()
    }

    fun refresh() = rebuildPager()

    fun onSearchChanged(newQuery: String) {
        if (newQuery == _uiState.value.query) return
        _uiState.update { it.copy(query = newQuery) }
        rebuildPager()
    }

    fun onApplyFilters(brands: Set<String>, models: Set<String>) {
        _uiState.update {
            it.copy(
                filters = it.filters.copy(
                    brands = brands,
                    models = models
                )
            )
        }
        rebuildPager()
    }

    fun updateFilters(sort: SortOption, brands: List<String>, models: List<String>) {
        _uiState.update {
            it.copy(
                filters = it.filters.copy(
                    sort = sort,
                    brands = brands.toSet(),
                    models = models.toSet()
                )
            )
        }
        rebuildPager()
    }

    fun currentFilters(): Triple<SortOption, List<String>, List<String>> =
        _uiState.value.let {
            Triple(
                it.filters.sort,
                it.filters.brands.toList(),
                it.filters.models.toList()
            )
        }

    private fun rebuildPager() {
        val s = _uiState.value
        val f = s.filters
        _pagingFlow.value = repository
            .getPagedProducts(
                query = s.query.trim(),
                brands = f.brands.toList(),
                models = f.models.toList(),
                sort = f.sort,
                pageSize = 20
            )
            .cachedIn(viewModelScope)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
