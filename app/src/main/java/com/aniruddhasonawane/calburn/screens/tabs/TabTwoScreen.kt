package com.aniruddhasonawane.calburn.screens.tabs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aniruddhasonawane.calburn.CalBurnApplication
import com.aniruddhasonawane.calburn.entity.DailyEntryEntity
import com.aniruddhasonawane.calburn.model.DailyTotalRow
import com.aniruddhasonawane.calburn.viewmodel.HistoryViewModel
import com.aniruddhasonawane.calburn.viewmodel.HistoryViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabTwoScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as CalBurnApplication

    val viewModel: HistoryViewModel =
        viewModel(
            factory = HistoryViewModelFactory(app.repository)
        )

    val state by viewModel.state.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingCsv by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    val exportLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/csv")
        ) { uri: Uri? ->

            val csv = pendingCsv
            pendingCsv = null

            if (uri == null || csv == null) {
                isExporting = false
                return@rememberLauncherForActivityResult
            }

            scope.launch(Dispatchers.IO) {
                try {
                    val outputStream =
                        context.contentResolver.openOutputStream(uri)
                            ?: throw IllegalStateException(
                                "Unable to open the selected file."
                            )

                    outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                        writer.write(csv)
                    }

                    withContext(Dispatchers.Main) {
                        isExporting = false

                        snackbarHostState.showSnackbar(
                            "All CalBurn data exported successfully."
                        )
                    }
                } catch (error: Exception) {
                    withContext(Dispatchers.Main) {
                        isExporting = false

                        snackbarHostState.showSnackbar(
                            "Export failed: ${error.message ?: "Unknown error"}"
                        )
                    }
                }
            }
        }

    fun exportAllData() {
        if (isExporting) return

        isExporting = true

        scope.launch {
            try {
                val csv = withContext(Dispatchers.IO) {
                    app.repository.buildCsvExport()
                }

                pendingCsv = csv

                val timestamp =
                    LocalDateTime.now()
                        .format(
                            DateTimeFormatter.ofPattern(
                                "yyyyMMdd_HHmmss"
                            )
                        )

                exportLauncher.launch(
                    "calburn_export_$timestamp.csv"
                )
            } catch (error: Exception) {
                isExporting = false

                snackbarHostState.showSnackbar(
                    "Export failed: ${error.message ?: "Unknown error"}"
                )
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(4.dp),
                title = {
                    Text(
                        text = "History",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(
                        onClick = ::exportAllData,
                        enabled = !isExporting
                    ) {
                        Text(
                            text =
                                if (isExporting) {
                                    "Exporting..."
                                } else {
                                    "Export Data"
                                }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor =
                        MaterialTheme.colorScheme.surface,
                    titleContentColor =
                        MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = "Calories, last 7 days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(12.dp))

                CalorieTrendChart(
                    state.history
                        .take(7)
                        .reversed()
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Daily log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))
            }

            if (state.history.isEmpty()) {
                item {
                    Text(
                        text =
                            "No history yet — start logging foods!",
                        color = Color.Gray,
                        modifier =
                            Modifier.padding(
                                vertical = 24.dp
                            )
                    )
                }
            }

            items(
                state.history,
                key = { it.date.toString() }
            ) { day ->

                DayCard(
                    day = day,
                    expanded =
                        day.date == state.selectedDate,
                    entries =
                        if (day.date == state.selectedDate) {
                            state.selectedEntries
                        } else {
                            emptyList()
                        },
                    onClick = {
                        viewModel.selectDate(day.date)
                    }
                )

                Spacer(Modifier.height(8.dp))
            }

            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CalorieTrendChart(
    days: List<DailyTotalRow>
) {
    if (days.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Log a few days to see your trend",
                color = Color.Gray
            )
        }

        return
    }

    val maxCal =
        days.maxOf { it.calories }
            .coerceAtLeast(1.0)

    val barColor =
        MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { day ->

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.Bottom
            ) {
                Text(
                    text =
                        day.calories
                            .roundToInt()
                            .toString(),
                    style =
                        MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )

                Spacer(Modifier.height(4.dp))

                val fraction =
                    (day.calories / maxCal)
                        .toFloat()
                        .coerceIn(0.03f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .fillMaxHeight(fraction)
                        .background(
                            barColor,
                            RoundedCornerShape(
                                topStart = 6.dp,
                                topEnd = 6.dp
                            )
                        )
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text =
                        day.date.format(
                            DateTimeFormatter.ofPattern(
                                "EEE"
                            )
                        ),
                    style =
                        MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun DayCard(
    day: DailyTotalRow,
    expanded: Boolean,
    entries: List<DailyEntryEntity>,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text =
                        day.date.format(
                            DateTimeFormatter.ofPattern(
                                "EEE, MMM d"
                            )
                        ),
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text =
                        "${day.calories.roundToInt()} kcal",
                    color =
                        MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text =
                    "P ${day.protein.roundToInt()}g · Fiber ${day.fiber.roundToInt()}g · Fat ${day.fat.roundToInt()}g",
                style =
                    MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            if (expanded) {
                Spacer(Modifier.height(10.dp))

                entries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text(
                            text =
                                "${entry.foodName} (${entry.grams.roundToInt()}g)",
                            style =
                                MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text =
                                "${entry.calories.roundToInt()} kcal",
                            style =
                                MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}