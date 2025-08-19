package com.ozantok.ecommerce.ui.detail

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.ozantok.ecommerce.R
import com.ozantok.ecommerce.data.local.db.ProductEntity
import com.ozantok.ecommerce.databinding.FragmentDetailBinding
import com.ozantok.ecommerce.ui.cart.CartViewModel
import com.ozantok.ecommerce.ui.favorites.FavoritesViewModel
import com.ozantok.ecommerce.utils.toTurkishCurrency
import com.ozantok.ecommerce.utils.toTurkishCurrencyOrRaw
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailFragment : Fragment(R.layout.fragment_detail) {

    private val viewModel: DetailViewModel by viewModels()
    private val cartVm: CartViewModel by activityViewModels()
    private val favVm: FavoritesViewModel by activityViewModels()

    private lateinit var binding: FragmentDetailBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentDetailBinding.bind(view)
        binding.toolbar.setShowBack(true) { findNavController().popBackStack() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { st ->
                    when {
                        st.isLoading -> showLoading()
                        st.error != null -> showError(st.error)
                        st.product != null -> bindUi(st.product)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favVm.favoriteIds.collect { ids ->
                    val p = viewModel.state.value.product ?: return@collect
                    updateFavoriteIcon(ids.contains(p.id))
                }
            }
        }
    }

    private fun showLoading() {
        binding.progress.isVisible = true
        binding.errorGroup.isVisible = false
        binding.contentGroup.isVisible = false
    }

    private fun showError(msg: String) {
        binding.progress.isVisible = false
        binding.errorGroup.isVisible = true
        binding.contentGroup.isVisible = false
        binding.errorText.text = msg
    }

    private fun bindUi(product: ProductEntity) {
        binding.progress.isVisible = false
        binding.errorGroup.isVisible = false
        binding.contentGroup.isVisible = true

        binding.toolbar.setTitleDetail(product.name)
        binding.title.text = product.name
        binding.desc.text = product.description.orEmpty()

        val numeric = product.price?.toString()?.replace(',', '.')?.toDoubleOrNull()
        binding.price.text = numeric?.toTurkishCurrency()
            ?: product.price?.toTurkishCurrencyOrRaw()
                    ?: "—"

        Glide.with(this)
            .load(product.image)
            .placeholder(R.color.dark_grey)
            .error(R.color.dark_grey)
            .into(binding.image)

        updateFavoriteIcon(favVm.favoriteIds.value.contains(product.id))

        binding.addToCartButton.setOnClickListener {
            val current = viewModel.state.value.product ?: run {
                Snackbar.make(binding.root, R.string.error, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val price = current.price?.toString()?.replace(',', '.')?.toDoubleOrNull() ?: 0.0
            cartVm.addOne(current.id, current.name, current.image ?: "", price)
            Snackbar.make(binding.root, R.string.added_to_cart, Snackbar.LENGTH_SHORT).show()
        }

        binding.favoriteBtn.setOnClickListener {
            favVm.toggle(product.id)
        }
    }

    private fun updateFavoriteIcon(selected: Boolean) {
        binding.favoriteBtn.setImageResource(
            if (selected) R.drawable.icon_star_selected
            else R.drawable.icon_star_unselected
        )
    }
}
