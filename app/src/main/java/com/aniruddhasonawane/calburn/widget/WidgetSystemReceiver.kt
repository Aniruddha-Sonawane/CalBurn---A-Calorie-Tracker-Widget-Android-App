package com.aniruddhasonawane.calburn.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aniruddhasonawane.calburn.worker.MidnightRefreshScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetSystemReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch { try { MidnightRefreshScheduler.schedule(context); WidgetUpdater.updateAll(context) } finally { pending.finish() } }
    }
}
