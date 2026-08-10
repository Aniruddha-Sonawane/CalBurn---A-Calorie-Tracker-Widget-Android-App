package com.aniruddhasonawane.calburn.screens.tabs

import android.content.Intent
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aniruddhasonawane.calburn.CalBurnApplication
import com.aniruddhasonawane.calburn.entity.DailyEntryEntity
import com.aniruddhasonawane.calburn.model.NutritionTotals
import com.aniruddhasonawane.calburn.popup.FoodPopupActivity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabOneScreen(onSettingsClick: () -> Unit) {

    val context = LocalContext.current
    val app = context.applicationContext as CalBurnApplication

    /*
     * Every date that actually contains at least one food entry.
     *
     * The database query returns these dates in descending order:
     *
     * Today
     * Yesterday / latest previous recorded day
     * older recorded day
     * ...
     *
     * Dates with zero entries never appear here, so swiping skips them.
     */
    val history by app.repository
        .observeAllDailyTotals()
        .collectAsState(emptyList())

    val recordedDates = remember(history) {
        history
            .map { it.date }
            .distinct()
            .sortedDescending()
    }

    /*
     * Today is always the initial screen.
     */
    var selectedDate by remember {
        mutableStateOf(LocalDate.now())
    }

    var showCalendar by remember {
        mutableStateOf(false)
    }

    /*
     * The entries are observed dynamically for whichever date
     * is currently selected.
     */
    val entries by remember(selectedDate) {
        app.repository.observeEntriesForDate(selectedDate)
    }.collectAsState(emptyList())

    /*
     * Calculate totals directly from the entries for the selected date.
     *
     * This means the totals shown on Home always correspond to
     * the date currently being viewed.
     */
    val totals = remember(entries) {
        NutritionTotals(
            calories = entries.sumOf { it.calories },
            protein = entries.sumOf { it.protein },
            fiber = entries.sumOf { it.fiber },
            fat = entries.sumOf { it.fat }
        )
    }

    /*
     * Move one step backward through RECORDED dates only.
     *
     * Left -> right swipe means going backward in time.
     *
     * If selectedDate itself is not a recorded date because the
     * user selected an empty date from the calendar, this finds
     * the nearest older recorded date.
     */
    fun goToPreviousRecordedDate() {

        val previous = recordedDates
            .firstOrNull { it.isBefore(selectedDate) }

        if (previous != null) {
            selectedDate = previous
        }
    }

    /*
     * Move one step forward through recorded dates.
     *
     * Right -> left swipe means moving toward newer dates.
     */
    fun goToNextRecordedDate() {

        val next = recordedDates
            .filter { it.isAfter(selectedDate) }
            .maxOrNull()

        if (next != null) {
            selectedDate = next
        }
    }

    /*
     * Swipe threshold.
     *
     * The finger must move roughly 80dp horizontally before
     * changing the selected date.
     */
    val swipeModifier = Modifier.pointerInput(
        recordedDates,
        selectedDate
    ) {

        var totalDrag by mutableFloatStateOf(0f)

        detectHorizontalDragGestures(

            onHorizontalDrag = { _, dragAmount ->
                totalDrag += dragAmount
            },

            onDragEnd = {

                val threshold = 80.dp.toPx()

                if (totalDrag > threshold) {

                    /*
                     * Left -> right:
                     * older recorded date.
                     */
                    goToPreviousRecordedDate()

                } else if (totalDrag < -threshold) {

                    /*
                     * Right -> left:
                     * newer recorded date.
                     */
                    goToNextRecordedDate()
                }

                totalDrag = 0f
            },

            onDragCancel = {
                totalDrag = 0f
            }
        )
    }

    Scaffold(

        topBar = {

            TopAppBar(

                modifier = Modifier.shadow(4.dp),

                title = {

                    /*
                     * The date/title itself is clickable.
                     *
                     * Today -> "Today"
                     * Yesterday -> "Yesterday"
                     * Older -> "9 Aug 2026"
                     */
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Column {

                            Text(
                                text = dateTitle(selectedDate),
                                fontWeight = FontWeight.Bold
                            )

                            /*
                             * Small secondary date text for
                             * Today / Yesterday so the exact
                             * date is still obvious.
                             */
                            if (
                                selectedDate == LocalDate.now() ||
                                selectedDate == LocalDate.now().minusDays(1)
                            ) {
                                Text(
                                    text = selectedDate.format(
                                        DateTimeFormatter.ofPattern(
                                            "d MMM yyyy"
                                        )
                                    ),
                                    style = MaterialTheme
                                        .typography
                                        .labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                },

                navigationIcon = {

                    /*
                     * Tapping the date/title opens the calendar.
                     */
                    IconButton(
                        onClick = {
                            showCalendar = true
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.CalendarMonth,
                            contentDescription =
                                "Choose date"
                        )
                    }
                },

                actions = {

                    IconButton(
                        onClick = onSettingsClick
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.Settings,
                            contentDescription =
                                "Settings"
                        )
                    }
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface,
                        titleContentColor =
                            MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor =
                            MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor =
                            MaterialTheme.colorScheme.onSurface
                    )
            )
        },

        floatingActionButton = {

            FloatingActionButton(
                onClick = {

                    /*
                     * Food additions continue to be made through
                     * the existing popup.
                     */
                    context.startActivity(
                        Intent(
                            context,
                            FoodPopupActivity::class.java
                        )
                    )
                }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add food"
                )
            }
        }

    ) { paddingValues ->

        /*
         * The whole Home content is horizontally swipeable.
         */
        LazyColumn(

            modifier = swipeModifier
                .padding(paddingValues)
                .fillMaxSize(),

            contentPadding =
                PaddingValues(16.dp)
        ) {

            item {

                TotalsSummary(totals)

                Spacer(
                    Modifier.height(20.dp)
                )

                Text(
                    text = if (
                        selectedDate == LocalDate.now()
                    ) {
                        "Logged today"
                    } else {
                        "Logged on ${
                            selectedDate.format(
                                DateTimeFormatter.ofPattern(
                                    "d MMM yyyy"
                                )
                            )
                        }"
                    },

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight =
                        FontWeight.SemiBold
                )

                Spacer(
                    Modifier.height(8.dp)
                )
            }

            if (entries.isEmpty()) {

                item {

                    Text(
                        text = "Nothing logged for this date.",

                        color = Color.Gray,

                        modifier = Modifier.padding(
                            vertical = 20.dp
                        )
                    )
                }

            } else {

                items(
                    entries,
                    key = {
                        it.id
                    }
                ) { entry ->

                    DailyEntryCard(entry)
                }
            }

            item {

                Spacer(
                    Modifier.height(80.dp)
                )
            }
        }
    }

    /*
     * Calendar dialog.
     */
    if (showCalendar) {

        val initialMillis =
            selectedDate
                .atStartOfDay(
                    ZoneId.systemDefault()
                )
                .toInstant()
                .toEpochMilli()

        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    initialMillis
            )

        DatePickerDialog(

            onDismissRequest = {
                showCalendar = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        val millis =
                            datePickerState
                                .selectedDateMillis

                        if (millis != null) {

                            selectedDate =
                                Instant
                                    .ofEpochMilli(millis)
                                    .atZone(
                                        ZoneId.systemDefault()
                                    )
                                    .toLocalDate()
                        }

                        showCalendar = false
                    }
                ) {
                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showCalendar = false
                    }
                ) {
                    Text("Cancel")
                }
            }

        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }
}

private fun dateTitle(
    date: LocalDate
): String {

    val today = LocalDate.now()

    return when {

        date == today ->
            "Today"

        date == today.minusDays(1) ->
            "Yesterday"

        else ->
            date.format(
                DateTimeFormatter.ofPattern(
                    "d MMM yyyy"
                )
            )
    }
}

@Composable
private fun DailyEntryCard(
    entry: DailyEntryEntity
) {

    androidx.compose.material3.Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column {

                Text(
                    entry.foodName,
                    fontWeight =
                        FontWeight.Medium
                )

                Text(
                    text =
                        "${entry.grams.roundToInt()} g",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color = Color.Gray
                )
            }

            Text(

                text =
                    "${entry.calories.roundToInt()} kcal",

                color =
                    MaterialTheme
                        .colorScheme
                        .primary,

                fontWeight =
                    FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TotalsSummary(
    totals: NutritionTotals
) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        SummaryStat(
            "Calories",
            totals.calories.roundToInt().toString(),
            "kcal"
        )

        SummaryStat(
            "Protein",
            totals.protein.roundToInt().toString(),
            "g"
        )

        SummaryStat(
            "Fiber",
            totals.fiber.roundToInt().toString(),
            "g"
        )

        SummaryStat(
            "Fat",
            totals.fat.roundToInt().toString(),
            "g"
        )
    }
}

@Composable
private fun SummaryStat(
    label: String,
    value: String,
    unit: String
) {

    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            value,
            style =
                MaterialTheme
                    .typography
                    .headlineSmall,

            fontWeight =
                FontWeight.Bold
        )

        Text(
            unit,
            style =
                MaterialTheme
                    .typography
                    .labelSmall,

            color = Color.Gray
        )

        Spacer(
            Modifier.height(2.dp)
        )

        Text(
            label,
            style =
                MaterialTheme
                    .typography
                    .labelSmall,

            color = Color.Gray
        )
    }
}
