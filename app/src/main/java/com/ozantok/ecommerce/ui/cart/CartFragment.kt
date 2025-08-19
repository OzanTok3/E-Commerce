package com.ozantok.ecommerce.ui.cart

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ozantok.ecommerce.R
import com.ozantok.ecommerce.data.local.db.CartItemEntity
import com.ozantok.ecommerce.databinding.FragmentCartBinding
import com.ozantok.ecommerce.utils.toTurkishCurrency
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartFragment : Fragment(R.layout.fragment_cart) {

    private val viewModel: CartViewModel by viewModels()
    private lateinit var binding: FragmentCartBinding
    private lateinit var adapter: CartAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentCartBinding.bind(view)

        binding.toolbar.setTitle("E-Market")
        binding.toolbar.setShowBack(false)

        adapter = CartAdapter(
            onMinusClicked = { item -> handleMinus(item) },
            onPlusClicked = { item -> viewModel.setQuantity(item.productId, item.quantity + 1) }
        )

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.items.collect { list ->
                            adapter.submitList(list)
                            if (list.isEmpty()) {
                                binding.emptyMessage.visibility = View.VISIBLE
                                binding.recycler.visibility = View.GONE
                                binding.bottomBar.visibility = View.GONE
                            } else {
                                binding.emptyMessage.visibility = View.GONE
                                binding.recycler.visibility = View.VISIBLE
                                binding.bottomBar.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.total.collect { total ->
                    binding.totalValue.text = total.toTurkishCurrency()
                }
            }
        }

        binding.btnComplete.setOnClickListener {
            Toast.makeText(requireContext(), "Checkout flow TODO", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleMinus(item: CartItemEntity) {
        if (item.quantity == 1) {
            AlertDialog.Builder(requireContext())
                .setTitle("Remove Product")
                .setMessage("${item.name} remove from cart. Are you sure?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.setQuantity(item.productId, 0)
                }
                .setNegativeButton("No", null)
                .show()
        } else {
            viewModel.setQuantity(item.productId, item.quantity - 1)
        }
    }
}
