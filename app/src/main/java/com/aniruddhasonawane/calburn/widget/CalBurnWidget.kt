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
import androidx.glance.layout.width
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

private val WidgetBackground = ColorProvider(Color(0xFF121212))
private val WidgetText = ColorProvider(Color(0xFFF7F7F7))
private val WidgetAccent = ColorProvider(Color(0xFFFF8A3D))

class CalBurnWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as CalBurnApplication).repository
        provideContent { WidgetContent(repository.observeTodayTotals().collectAsState(NutritionTotals()).value) }
    }
}

@androidx.compose.runtime.Composable
private fun WidgetContent(totals: NutritionTotals) {
    val expanded = LocalSize.current.width >= 340.dp || LocalSize.current.height >= 130.dp
    val padding = if (expanded) 18.dp else 12.dp
    val columnWidth = if (expanded) 72.dp else 58.dp
    val caloriesSize = if (expanded) 31.sp else 27.sp
    val nutrientSize = if (expanded) 27.sp else 23.sp
    val emojiSize = if (expanded) 22.sp else 19.sp
    Box(GlanceModifier.fillMaxSize().background(WidgetBackground).cornerRadius(28.dp).padding(padding)) {
        Text("CalBurn", modifier = GlanceModifier.padding(top = 5.dp), style = TextStyle(color = WidgetText, fontSize = 14.sp, fontWeight = FontWeight.Bold))
        Column(GlanceModifier.fillMaxSize()) {
            Row(GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.Horizontal.End) {
                Text(
                    text = "+",
                    modifier = GlanceModifier.background(WidgetAccent).cornerRadius(28.dp).padding(horizontal = 17.dp, vertical = 10.dp).clickable(actionStartActivity(Intent(LocalContext.current, FoodPopupActivity::class.java))),
                    style = TextStyle(color = ColorProvider(Color(0xFF17120E)), fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                )
            }
            Row(GlanceModifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.Vertical.CenterVertically, horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
                NutritionSection("\uD83D\uDD25", wholeNumber(totals.calories), columnWidth, caloriesSize, emojiSize)
                NutritionSection("\uD83C\uDF57", wholeNumber(totals.protein), columnWidth, nutrientSize, emojiSize)
                NutritionSection("\uD83C\uDF3E", wholeNumber(totals.fiber), columnWidth, nutrientSize, emojiSize)
                NutritionSection("\uD83E\uDD51", wholeNumber(totals.fat), columnWidth, nutrientSize, emojiSize)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun NutritionSection(emoji: String, value: String, width: androidx.compose.ui.unit.Dp, valueSize: androidx.compose.ui.unit.TextUnit, emojiSize: androidx.compose.ui.unit.TextUnit) {
    Column(GlanceModifier.width(width), verticalAlignment = Alignment.Vertical.CenterVertically, horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
        Text(emoji, style = TextStyle(fontSize = emojiSize, textAlign = TextAlign.Center))
        Text(value, style = TextStyle(color = WidgetText, fontSize = valueSize, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center))
    }
}

private fun wholeNumber(value: Double): String = value.roundToInt().coerceAtLeast(0).toString()
object WidgetUpdater { suspend fun updateAll(context: Context) { GlanceAppWidgetManager(context).getGlanceIds(CalBurnWidget::class.java).forEach { CalBurnWidget().update(context, it) } } }

