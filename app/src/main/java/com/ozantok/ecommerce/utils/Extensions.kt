package com.ozantok.ecommerce.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Currency
import java.util.Locale

private val trLocale = Locale("tr", "TR")
private val trSymbols = DecimalFormatSymbols(trLocale).apply {
}

fun Number.toTurkishCurrency(): String {
    val df = DecimalFormat("#,##0.00 ¤", trSymbols).apply {
        currency = Currency.getInstance("TRY")
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return df.format(this)
}

fun String?.toTurkishCurrencyOrRaw(): String {
    if (this.isNullOrBlank()) return "—"
    val normalized = this.replace(',', '.')
    val num = normalized.toDoubleOrNull()
    return num?.toTurkishCurrency() ?: this
}
