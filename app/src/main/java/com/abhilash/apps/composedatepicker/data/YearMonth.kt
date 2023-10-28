package com.abhilash.apps.composedatepicker.data

import androidx.compose.runtime.Stable

@Stable
data class YearMonth(
    val year: Int,
    val month: Int
)

infix fun Int.on(year: Int): YearMonth = YearMonth(year, this)