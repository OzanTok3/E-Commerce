package com.ozantok.ecommerce.ui.favorites

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ozantok.ecommerce.R
import com.ozantok.ecommerce.databinding.FragmentFavoritesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private val favVm: FavoritesViewModel by activityViewModels()
    private lateinit var binding: FragmentFavoritesBinding
    private val adapter by lazy {
        FavoritesAdapter(
            onItemClick = { p ->
                val args = Bundle().apply { putString("productId", p.id) }
                findNavController().navigate(R.id.detailFragment, args)
            },
            onToggleFavorite = { p ->
                favVm.toggle(p.id)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentFavoritesBinding.bind(view)

        binding.toolbar.setTitle("E-Market")
        binding.toolbar.setShowBack(false)

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favVm.favoriteProducts.collect { list ->
                    binding.emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    adapter.submitList(list)
                }
            }
        }
    }
}