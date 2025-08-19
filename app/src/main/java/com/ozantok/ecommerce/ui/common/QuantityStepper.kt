package com.ozantok.ecommerce.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.ozantok.ecommerce.R

class QuantityStepper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val btnMinus: TextView
    private val btnPlus: TextView
    private val tvValue: TextView

    var min: Int = 1
        set(value) {
            field = value; ensureBounds()
        }
    var max: Int = 99
        set(value) {
            field = value; ensureBounds()
        }

    var value: Int = 1
        set(v) {
            field = v
            tvValue.text = v.toString()
            updateEnabled()
            onValueChanged?.invoke(v)
        }

    private var onValueChanged: ((Int) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.view_amount_stepper, this, true)
        btnMinus = findViewById(R.id.btnMinus)
        btnPlus = findViewById(R.id.btnPlus)
        tvValue = findViewById(R.id.tvValue)

        context.obtainStyledAttributes(attrs, R.styleable.QuantityStepper).apply {
            min = getInt(R.styleable.QuantityStepper_qs_min, min)
            max = getInt(R.styleable.QuantityStepper_qs_max, max)
            value = getInt(R.styleable.QuantityStepper_qs_value, value)
            recycle()
        }

        btnMinus.setOnClickListener { if (value > min) value -= 1 }
        btnPlus.setOnClickListener { if (value < max) value += 1 }

        updateEnabled()
    }

    private fun ensureBounds() {
        if (value < min) value = min
        if (value > max) value = max
        updateEnabled()
    }

    private fun updateEnabled() {
        btnMinus.isEnabled = value > min
        btnPlus.isEnabled = value < max
        btnMinus.alpha = if (btnMinus.isEnabled) 1f else 0.4f
        btnPlus.alpha = if (btnPlus.isEnabled) 1f else 0.4f
    }

    fun setOnValueChangeListener(block: (Int) -> Unit) {
        onValueChanged = block
    }
}
