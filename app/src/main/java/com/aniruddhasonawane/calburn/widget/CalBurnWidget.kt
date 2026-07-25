package com.aniruddhasonawane.calburn.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.aniruddhasonawane.calburn.CalBurnApplication
import com.aniruddhasonawane.calburn.model.NutritionTotals
import com.aniruddhasonawane.calburn.popup.FoodPopupActivity
import kotlin.math.roundToInt
import java.util.Locale

private val WidgetBackground = ColorProvider(Color(0xFF121212))
private val WidgetCard = ColorProvider(Color(0xFF202020))
private val WidgetText = ColorProvider(Color(0xFFF7F7F7))
private val WidgetMuted = ColorProvider(Color(0xFFB8B8B8))
private val WidgetAccent = ColorProvider(Color(0xFFFF8A3D))

class CalBurnWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as CalBurnApplication).repository
        provideContent { WidgetContent(repository.observeTodayTotals().collectAsState(NutritionTotals()).value) }
    }
}

@androidx.compose.runtime.Composable
private fun WidgetContent(totals: NutritionTotals) {
    val size = LocalSize.current
    val expanded = size.width >= 340.dp || size.height >= 160.dp
    val padding = if (expanded) 18.dp else 12.dp
    val valueSize = if (expanded) 30.sp else 24.sp
    val emojiSize = if (expanded) 21.sp else 18.sp
    Box(GlanceModifier.fillMaxSize().background(WidgetBackground).cornerRadius(28.dp).padding(padding)) {
        Column(GlanceModifier.fillMaxSize()) {
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Vertical.Top, horizontalAlignment = Alignment.Horizontal.End) {                Text(
                    text = "+",
                    modifier = GlanceModifier.background(WidgetAccent).cornerRadius(28.dp).padding(horizontal = 17.dp, vertical = 10.dp).clickable(actionStartActivity(Intent(LocalContext.current, FoodPopupActivity::class.java))),
                    style = TextStyle(color = ColorProvider(Color(0xFF17120E)), fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                )
            }
            Row(GlanceModifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.Vertical.CenterVertically, horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
                NutritionSection("??", fixedNumber(totals.calories, 4), valueSize, emojiSize)
                NutritionSection("??", fixedNumber(totals.protein, 3), valueSize, emojiSize)
                NutritionSection("??", fixedNumber(totals.fiber, 3), valueSize, emojiSize)
                NutritionSection("??", fixedNumber(totals.fat, 3), valueSize, emojiSize)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun NutritionSection(emoji: String, value: String, valueSize: androidx.compose.ui.unit.TextUnit, emojiSize: androidx.compose.ui.unit.TextUnit) {
    Column(GlanceModifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.Vertical.CenterVertically, horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
        Text(emoji, style = TextStyle(fontSize = emojiSize, textAlign = TextAlign.Center))
        Text(value, style = TextStyle(color = WidgetText, fontSize = valueSize, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center))
    }
}

private fun fixedNumber(value: Double, width: Int): String = String.format(Locale.US, "%0${width}d", value.roundToInt().coerceAtLeast(0))
object WidgetUpdater { suspend fun updateAll(context: Context) { GlanceAppWidgetManager(context).getGlanceIds(CalBurnWidget::class.java).forEach { CalBurnWidget().update(context, it) } } }



