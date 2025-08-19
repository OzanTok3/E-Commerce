package com.ozantok.ecommerce.ui.home.filter


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ozantok.ecommerce.R
import com.ozantok.ecommerce.data.repository.ProductRepository
import com.ozantok.ecommerce.databinding.FilterBottomSheetLayoutBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FilterBottomSheetDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var repo: ProductRepository
    private var _binding: FilterBottomSheetLayoutBinding? = null
    private val binding get() = _binding!!

    private val selectedBrands = linkedSetOf<String>()
    private val selectedModels = linkedSetOf<String>()

    private var allBrands: List<String> = emptyList()
    private var allModels: List<String> = emptyList()

    private lateinit var brandAdapter: FilterOptionAdapter
    private lateinit var modelAdapter: FilterOptionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FilterBottomSheetLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        arguments?.getStringArrayList(ARG_BRANDS)?.let { selectedBrands.addAll(it) }
        arguments?.getStringArrayList(ARG_MODELS)?.let { selectedModels.addAll(it) }
        val preSort = arguments?.getString(ARG_SORT)?.let { SortOption.valueOf(it) }
            ?: SortOption.DATE_OLD_TO_NEW

        when (preSort) {
            SortOption.DATE_OLD_TO_NEW -> binding.groupSort.check(R.id.rbOldToNew)
            SortOption.DATE_NEW_TO_OLD -> binding.groupSort.check(R.id.rbNewToOld)
            SortOption.PRICE_HIGH_TO_LOW -> binding.groupSort.check(R.id.rbPriceHighToLow)
            SortOption.PRICE_LOW_TO_HIGH -> binding.groupSort.check(R.id.rbPriceLowToHigh)
        }

        brandAdapter = FilterOptionAdapter(selectedBrands) { _, _ -> }
        modelAdapter = FilterOptionAdapter(selectedModels) { _, _ -> }

        binding.rvBrands.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBrands.adapter = brandAdapter
        binding.rvModels.layoutManager = LinearLayoutManager(requireContext())
        binding.rvModels.adapter = modelAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            allBrands = repo.getDistinctBrands()
            allModels = repo.getDistinctModels()
            brandAdapter.submitList(allBrands)
            modelAdapter.submitList(allModels)
        }

        binding.editTextBrandSearch.doAfterTextChanged { q ->
            val term = q?.toString().orEmpty().trim().lowercase()
            brandAdapter.submitList(
                if (term.isEmpty()) allBrands else allBrands.filter {
                    it.lowercase().contains(term)
                }
            )
        }

        binding.editTextModelSearch.doAfterTextChanged { q ->
            val term = q?.toString().orEmpty().trim().lowercase()
            modelAdapter.submitList(
                if (term.isEmpty()) allModels else allModels.filter {
                    it.lowercase().contains(term)
                }
            )
        }

        binding.imageViewClose.setOnClickListener { dismiss() }

        binding.btnApply.setOnClickListener {
            val sort = when (binding.groupSort.checkedRadioButtonId) {
                R.id.rbNewToOld -> SortOption.DATE_NEW_TO_OLD
                R.id.rbPriceHighToLow -> SortOption.PRICE_HIGH_TO_LOW
                R.id.rbPriceLowToHigh -> SortOption.PRICE_LOW_TO_HIGH
                else -> SortOption.DATE_OLD_TO_NEW
            }
            setFragmentResult(
                REQUEST_KEY,
                bundleOf(
                    ARG_SORT to sort.name,
                    ARG_BRANDS to ArrayList(selectedBrands),
                    ARG_MODELS to ArrayList(selectedModels)
                )
            )
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        (super.onCreateDialog(savedInstanceState) as com.google.android.material.bottomsheet.BottomSheetDialog).apply {
            setOnShowListener {
                val sheet = findViewById<android.widget.FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet
                ) ?: return@setOnShowListener

                sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

                val behavior =
                    com.google.android.material.bottomsheet.BottomSheetBehavior.from(sheet)
                behavior.apply {

                    state =
                        com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                    isDraggable = true
                    isFitToContents = false
                    expandedOffset = 0
                    peekHeight = requireContext().resources.displayMetrics.heightPixels
                }
            }
        }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY = "filters_result"
        const val ARG_SORT = "arg_sort"
        const val ARG_BRANDS = "arg_brands"
        const val ARG_MODELS = "arg_models"

        fun newInstance(
            sort: SortOption,
            brands: ArrayList<String>,
            models: ArrayList<String>
        ) = FilterBottomSheetDialogFragment().apply {
            arguments = bundleOf(
                ARG_SORT to sort.name,
                ARG_BRANDS to brands,
                ARG_MODELS to models
            )
        }
    }
}