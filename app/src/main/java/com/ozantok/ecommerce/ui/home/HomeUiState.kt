package com.ozantok.ecommerce.ui.home

import com.ozantok.ecommerce.ui.home.filter.SortOption

data class FilterState(
    val sort: SortOption = SortOption.DATE_OLD_TO_NEW,
    val brands: Set<String> = emptySet(),
    val models: Set<String> = emptySet()
)

data class HomeUiState(
    val query: String = "",
    val filters: FilterState = FilterState(),
    val favorites: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)