package com.aniruddhasonawane.calburn.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.aniruddhasonawane.calburn.CalBurnApplication
import com.aniruddhasonawane.calburn.model.NutritionTotals
import com.aniruddhasonawane.calburn.popup.FoodPopupActivity
import java.util.Locale

class CalBurnWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as CalBurnApplication).repository
        provideContent { WidgetContent(repository.observeTodayTotals().collectAsState(NutritionTotals()).value) }
    }
}
private fun number(value: Double) = String.format(Locale.US, "%.1f", value).removeSuffix(".0")
@androidx.compose.runtime.Composable
private fun WidgetContent(totals: NutritionTotals) {
    Column(GlanceModifier.fillMaxSize().background(ColorProvider(Color(0xFF1D232B))).padding(16.dp), verticalAlignment = Alignment.Vertical.CenterVertically, horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
        Text("TODAY", style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold))
        Text("${number(totals.calories)} kcal", style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold))
        Row(GlanceModifier.fillMaxWidth().padding(top = 12.dp), horizontalAlignment = Alignment.Horizontal.CenterHorizontally) { Metric("Protein", totals.protein); Metric("Fiber", totals.fiber); Metric("Fat", totals.fat) }
        Text("+", GlanceModifier.padding(top = 12.dp).clickable(actionStartActivity(Intent(LocalContext.current, FoodPopupActivity::class.java))), style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold))
    }
}
@androidx.compose.runtime.Composable
private fun Metric(label: String, value: Double) { Column(GlanceModifier.padding(horizontal = 7.dp), horizontalAlignment = Alignment.Horizontal.CenterHorizontally) { Text(number(value) + "g", style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)); Text(label, style = TextStyle(color = ColorProvider(Color.LightGray), textAlign = TextAlign.Center)) } }
object WidgetUpdater { suspend fun updateAll(context: Context) { GlanceAppWidgetManager(context).getGlanceIds(CalBurnWidget::class.java).forEach { CalBurnWidget().update(context, it) } }
}

