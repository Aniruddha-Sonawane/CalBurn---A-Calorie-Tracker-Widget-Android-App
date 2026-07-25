package com.aniruddhasonawane.calburn.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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

private val WidgetBackground = ColorProvider(Color(0xFF121212))
private val WidgetText = ColorProvider(Color(0xFFF7F7F7))
private val WidgetAccent = ColorProvider(Color(0xFFFF8A3D))

class CalBurnWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as CalBurnApplication).repository
        provideContent { WidgetContent(repository.observeTodayTotals().collectAsState(NutritionTotals()).value) }
    }
}

private data class WidgetSizing(
    val outerPadding: Dp,
    val valueSize: TextUnit,
    val emojiSize: TextUnit,
    val buttonHorizontalPadding: Dp,
    val buttonVerticalPadding: Dp,
    val buttonTextSize: TextUnit
)

// Continuous-ish tiers so the widget scales smoothly between grid sizes
// (e.g. 4x1 vs 5x1 cells on the home screen) instead of jumping between
// just two hardcoded states.
private fun sizingFor(widthDp: Float, heightDp: Float): WidgetSizing = when {
    widthDp >= 340f && heightDp >= 90f -> WidgetSizing(
        outerPadding = 18.dp,
        valueSize = 26.sp,
        emojiSize = 20.sp,
        buttonHorizontalPadding = 16.dp,
        buttonVerticalPadding = 11.dp,
        buttonTextSize = 28.sp
    )
    widthDp >= 260f -> WidgetSizing(
        outerPadding = 14.dp,
        valueSize = 22.sp,
        emojiSize = 18.dp.value.sp,
        buttonHorizontalPadding = 13.dp,
        buttonVerticalPadding = 9.dp,
        buttonTextSize = 24.sp
    )
    widthDp >= 200f -> WidgetSizing(
        outerPadding = 11.dp,
        valueSize = 18.sp,
        emojiSize = 15.sp,
        buttonHorizontalPadding = 10.dp,
        buttonVerticalPadding = 7.dp,
        buttonTextSize = 20.sp
    )
    else -> WidgetSizing(
        outerPadding = 8.dp,
        valueSize = 14.sp,
        emojiSize = 12.sp,
        buttonHorizontalPadding = 8.dp,
        buttonVerticalPadding = 5.dp,
        buttonTextSize = 16.sp
    )
}

@androidx.compose.runtime.Composable
private fun WidgetContent(totals: NutritionTotals) {
    val size = LocalSize.current
    val sizing = sizingFor(size.width.value, size.height.value)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBackground)
            .cornerRadius(28.dp)
            .padding(horizontal = sizing.outerPadding)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            NutritionSection("🔥", wholeNumber(totals.calories), sizing, GlanceModifier.defaultWeight())
            NutritionSection("🍗", wholeNumber(totals.protein), sizing, GlanceModifier.defaultWeight())
            NutritionSection("🌾", wholeNumber(totals.fiber), sizing, GlanceModifier.defaultWeight())
            NutritionSection("🥑", wholeNumber(totals.fat), sizing, GlanceModifier.defaultWeight())

            Box(
                modifier = GlanceModifier.defaultWeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    modifier = GlanceModifier
                        .background(WidgetAccent)
                        .cornerRadius(40.dp)
                        .padding(horizontal = sizing.buttonHorizontalPadding, vertical = sizing.buttonVerticalPadding)
                        .clickable(
                            actionStartActivity(
                                Intent(LocalContext.current, FoodPopupActivity::class.java)
                            )
                        ),
                    style = TextStyle(
                        color = ColorProvider(Color.Black),
                        fontSize = sizing.buttonTextSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun NutritionSection(
    emoji: String,
    value: String,
    sizing: WidgetSizing,
    modifier: GlanceModifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = emoji,
                style = TextStyle(fontSize = sizing.emojiSize, textAlign = TextAlign.Center),
                maxLines = 1
            )
            Text(
                text = value,
                style = TextStyle(
                    color = WidgetText,
                    fontSize = sizing.valueSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
        }
    }
}

private fun wholeNumber(value: Double): String = value.roundToInt().coerceAtLeast(0).toString()

object WidgetUpdater {
    suspend fun updateAll(context: Context) {
        GlanceAppWidgetManager(context).getGlanceIds(CalBurnWidget::class.java).forEach {
            CalBurnWidget().update(context, it)
        }
    }
}