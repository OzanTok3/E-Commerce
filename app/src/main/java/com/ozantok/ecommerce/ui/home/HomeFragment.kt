package com.ozantok.ecommerce.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.ozantok.ecommerce.R
import com.ozantok.ecommerce.databinding.FragmentHomeBinding
import com.ozantok.ecommerce.ui.cart.CartViewModel
import com.ozantok.ecommerce.ui.favorites.FavoritesViewModel
import com.ozantok.ecommerce.ui.home.filter.FilterBottomSheetDialogFragment
import com.ozantok.ecommerce.ui.home.filter.SortOption
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val cartVm: CartViewModel by activityViewModels()
    private val favVm: FavoritesViewModel by activityViewModels()

    private lateinit var adapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ecomToolbar.setTitle("E-Market")
        binding.ecomToolbar.setShowBack(false)

        setupOutsideFocusClear()
        binding.editTextSearch.setOnFocusChangeListener { _, hasFocus ->
            binding.layoutSearch.isActivated = hasFocus
        }

        childFragmentManager.setFragmentResultListener(
            FilterBottomSheetDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val sort =
                SortOption.valueOf(bundle.getString(FilterBottomSheetDialogFragment.ARG_SORT)!!)
            val brands = bundle.getStringArrayList(FilterBottomSheetDialogFragment.ARG_BRANDS)
                ?: arrayListOf()
            val models = bundle.getStringArrayList(FilterBottomSheetDialogFragment.ARG_MODELS)
                ?: arrayListOf()
            viewModel.updateFilters(sort, brands, models)
        }

        binding.textViewFilter.setOnClickListener {
            val (sort, b, m) = viewModel.currentFilters()
            FilterBottomSheetDialogFragment.newInstance(sort, ArrayList(b), ArrayList(m))
                .show(childFragmentManager, "filters")
        }

        setupRecycler()
        setupSearchAndFilter()
        observeFavorites()
        observePaging()
        observeLoadState()
    }

    private fun observeFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favVm.favoriteIds.collect { ids ->
                    adapter.submitFavoriteIds(ids)
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setupRecycler() {
        adapter = HomeAdapter(
            onItemClick = { product ->
                findNavController().navigate(
                    R.id.detailFragment,
                    bundleOf("productId" to product.id)
                )
            },
            onFavClick = { product ->
                favVm.toggle(product.id)
            },
            onAddToCartClick = { product ->
                val price = product.price?.toString()?.replace(',', '.')?.toDoubleOrNull() ?: 0.0
                cartVm.addOne(product.id, product.name, product.image ?: "", price)
                Snackbar.make(binding.root, R.string.added_to_cart, Snackbar.LENGTH_SHORT).show()
            }
        )

        binding.recyclerViewProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewProducts.adapter = adapter
    }

    private fun setupSearchAndFilter() {
        binding.editTextSearch.doAfterTextChanged { text ->
            viewModel.onSearchChanged(text?.toString().orEmpty())
        }
    }

    private fun observePaging() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pagingFlow.collectLatest { flow ->
                flow?.let {
                    launch {
                        it.collectLatest { pagingData ->
                            adapter.submitData(pagingData)
                        }
                    }
                }
            }
        }
    }

    private fun observeLoadState() {
        adapter.addLoadStateListener { loadStates ->
            val isLoading = loadStates.refresh is LoadState.Loading
            val isError = loadStates.refresh is LoadState.Error
            val isEmpty = loadStates.refresh is LoadState.NotLoading && adapter.itemCount == 0

            binding.progressBar.isVisible = isLoading
            binding.textViewError.isVisible = isError
            binding.textViewEmpty.isVisible = isEmpty
            binding.stateContainer.isVisible = isLoading || isError || isEmpty
            binding.recyclerViewProducts.isVisible = !binding.stateContainer.isVisible

            if (isError) {
                val err = (loadStates.refresh as LoadState.Error).error
                binding.textViewError.text =
                    err.localizedMessage ?: getString(R.string.something_went_wrong)
            }

            if (loadStates.refresh is LoadState.NotLoading &&
                loadStates.mediator?.refresh !is LoadState.Loading
            ) {
                binding.recyclerViewProducts.scrollToPosition(0)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupOutsideFocusClear() {
        val clear: () -> Unit = {
            if (binding.editTextSearch.hasFocus()) {
                binding.editTextSearch.clearFocus()
                binding.layoutSearch.isActivated = false
                binding.root.hideKeyboard()
            }
        }
        binding.root.setOnTouchListener { _, _ -> clear(); false }
        binding.recyclerViewProducts.setOnTouchListener { _, _ -> clear(); false }
    }
}
