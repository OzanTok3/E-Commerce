package com.ozantok.ecommerce.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.ozantok.ecommerce.R
import com.ozantok.ecommerce.databinding.ViewEcommerceToolbarBinding

class ECommerceToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding: ViewEcommerceToolbarBinding =
        ViewEcommerceToolbarBinding.inflate(LayoutInflater.from(context), this)

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ECommerceToolbar)
            val title = a.getString(R.styleable.ECommerceToolbar_titleText) ?: ""
            val titleDetail = a.getString(R.styleable.ECommerceToolbar_titleTextDetail) ?: ""
            val showBack = a.getBoolean(R.styleable.ECommerceToolbar_showBack, false)
            a.recycle()

            setTitle(title)
            setTitleDetail(titleDetail)
            setShowBack(showBack)
        }
    }

    fun setTitle(text: String) {
        binding.textTitle.apply {
            visibility = if (text.isNullOrEmpty()) GONE else VISIBLE
            binding.textTitle.text = text
        }
    }

    fun setTitleDetail(text: String) {
        binding.textTitleDetail.apply {
            visibility = if (text.isNullOrEmpty()) GONE else VISIBLE
            binding.textTitleDetail.text = text
        }
    }

    fun setShowBack(visible: Boolean, onClick: (() -> Unit)? = null) {
        binding.buttonBack.apply {
            visibility = if (visible) VISIBLE else GONE
            setOnClickListener { onClick?.invoke() }
        }
    }
}