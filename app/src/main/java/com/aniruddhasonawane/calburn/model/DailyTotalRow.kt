package com.aniruddhasonawane.calburn.model

import java.time.LocalDate

data class DailyTotalRow(
    val date: LocalDate,
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val fiber: Double = 0.0,
    val fat: Double = 0.0
)