package com.aniruddhasonawane.calburn

import android.app.Application
import com.aniruddhasonawane.calburn.database.NutritionDatabase
import com.aniruddhasonawane.calburn.repository.NutritionRepository
import com.aniruddhasonawane.calburn.worker.MidnightRefreshScheduler

class CalBurnApplication : Application() {
    val repository by lazy { NutritionRepository(NutritionDatabase.get(this)) }
    override fun onCreate() { super.onCreate(); MidnightRefreshScheduler.schedule(this) }
}
