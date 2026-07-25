package com.aniruddhasonawane.calburn.worker

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aniruddhasonawane.calburn.widget.WidgetUpdater
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

private val Context.widgetDataStore by preferencesDataStore("widget_refresh")
object MidnightRefreshScheduler {
    private const val WORK_NAME = "calburn_midnight_refresh"
    fun schedule(context: Context) {
        val now = ZonedDateTime.now(); val next = now.plusDays(1).truncatedTo(ChronoUnit.DAYS)
        val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>().setInitialDelay(Duration.between(now, next)).build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }
}
class WidgetRefreshWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result { applicationContext.widgetDataStore.edit { it[longPreferencesKey("last_refresh")] = System.currentTimeMillis() }; WidgetUpdater.updateAll(applicationContext); MidnightRefreshScheduler.schedule(applicationContext); return Result.success() }
}
