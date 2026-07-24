package com.aniruddhasonawane.calburn

import android.app.Application
import com.aniruddhasonawane.calburn.data.local.DatabaseProvider
import com.aniruddhasonawane.calburn.data.local.FoodSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CalBurnApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {

            val database = DatabaseProvider.getDatabase(this@CalBurnApplication)

            FoodSeeder(
                database.foodLibraryDao()
            ).seedIfEmpty()
        }
    }
}