package com.aniruddhasonawane.calburn.popup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.aniruddhasonawane.calburn.CalBurnApplication
import com.aniruddhasonawane.calburn.entity.FoodEntity
import com.aniruddhasonawane.calburn.ui.theme.CalBurnTheme
import com.aniruddhasonawane.calburn.viewmodel.FoodPopupViewModel
import com.aniruddhasonawane.calburn.viewmodel.FoodPopupViewModelFactory
import com.aniruddhasonawane.calburn.widget.WidgetUpdater
import kotlinx.coroutines.launch
import java.util.Locale

class FoodPopupActivity : ComponentActivity() {
    private val viewModel: FoodPopupViewModel by viewModels { FoodPopupViewModelFactory((application as CalBurnApplication).repository) }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { CalBurnTheme { Surface(Modifier.fillMaxWidth().padding(horizontal = 18.dp), shape = MaterialTheme.shapes.extraLarge) { PopupRoot(viewModel, ::finishAndRefresh) } } } }
    private fun finishAndRefresh() { lifecycleScope.launch { WidgetUpdater.updateAll(this@FoodPopupActivity); finish() } }
}
private enum class PopupMode { LIST, NEW, ADD, EDIT }
@Composable
private fun PopupRoot(viewModel: FoodPopupViewModel, complete: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var mode by remember { mutableStateOf(PopupMode.LIST) }
    var selected by remember { mutableStateOf<FoodEntity?>(null) }
    when (mode) {
        PopupMode.LIST -> FoodList(state.query, state.recentFoods, state.filteredFoods, viewModel::search, { mode = PopupMode.NEW }, { selected = it; mode = PopupMode.ADD }, { selected = it; mode = PopupMode.EDIT }, viewModel::delete)
        PopupMode.NEW -> FoodEditor(null, "Save & Add", { food -> viewModel.saveAndAdd(food, 100.0, complete) }, { mode = PopupMode.LIST })
        PopupMode.ADD -> selected?.let { FoodAmount(it, { grams -> viewModel.add(it, grams, complete) }, { mode = PopupMode.LIST }) }
        PopupMode.EDIT -> selected?.let { FoodEditor(it, "Save", { food -> viewModel.update(food); mode = PopupMode.LIST }, { mode = PopupMode.LIST }) }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FoodList(query: String, recent: List<FoodEntity>, foods: List<FoodEntity>, onQuery: (String) -> Unit, newFood: () -> Unit, add: (FoodEntity) -> Unit, edit: (FoodEntity) -> Unit, delete: (FoodEntity) -> Unit) {
    var pendingDelete by remember { mutableStateOf<FoodEntity?>(null) }
    Column(Modifier.padding(20.dp).widthIn(max = 600.dp)) {
        Text("Add food", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(query, onQuery, Modifier.fillMaxWidth().padding(top = 12.dp), label = { Text("Search foods") }, singleLine = true)
        Button(newFood, Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("New Food") }
        if (query.isBlank() && recent.isNotEmpty()) { Text("Recently used", Modifier.padding(top = 14.dp), style = MaterialTheme.typography.titleMedium); FoodRows(recent, add, edit, { pendingDelete = it }) }
        Text(if (query.isBlank()) "Saved foods" else "Results", Modifier.padding(top = 14.dp), style = MaterialTheme.typography.titleMedium)
        FoodRows(foods, add, edit, { pendingDelete = it })
    }
    pendingDelete?.let { food -> AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Delete ${food.name}?") }, text = { Text("Saved food will be deleted. Existing history remains.") }, confirmButton = { Button({ delete(food); pendingDelete = null }) { Text("Delete") } }, dismissButton = { Button({ pendingDelete = null }) { Text("Cancel") } }) }
}
@Composable
private fun FoodRows(foods: List<FoodEntity>, add: (FoodEntity) -> Unit, edit: (FoodEntity) -> Unit, delete: (FoodEntity) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(vertical = 4.dp), modifier = Modifier.height((foods.size.coerceAtMost(4) * 62).dp)) { items(foods, key = { it.id }) { food -> FoodRow(food, { add(food) }, { edit(food) }, { delete(food) }) } }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FoodRow(food: FoodEntity, add: () -> Unit, edit: () -> Unit, delete: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth().padding(vertical = 3.dp).combinedClickable(onClick = add, onLongClick = edit)) { Row(Modifier.fillMaxWidth().padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f).padding(vertical = 8.dp)) { Text(food.name); Text("${format(food.caloriesPer100g)} kcal · ${format(food.proteinPer100g)}g protein / 100g", style = MaterialTheme.typography.bodySmall) }; Box { IconButton({ menu = true }) { Icon(Icons.Default.MoreVert, "Food menu") }; DropdownMenu(menu, { menu = false }) { DropdownMenuItem({ Text("Edit") }, { menu = false; edit() }); DropdownMenuItem({ Text("Delete") }, { menu = false; delete() }) } } } }
}
@Composable
private fun FoodAmount(food: FoodEntity, add: (Double) -> Unit, cancel: () -> Unit) {
    var grams by remember { mutableStateOf("100") }
    val amount = grams.toDoubleOrNull()
    Column(Modifier.padding(20.dp)) { Text(food.name, style = MaterialTheme.typography.headlineSmall); OutlinedTextField(grams, { grams = it }, Modifier.fillMaxWidth().padding(top = 12.dp), label = { Text("Grams") }, singleLine = true); Text("${format((amount ?: 0.0) / 100 * food.caloriesPer100g)} kcal", Modifier.padding(top = 8.dp)); Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) { Button(cancel) { Text("Cancel") }; Spacer(Modifier.padding(4.dp)); Button({ amount?.takeIf { it > 0 }?.let(add) }, enabled = amount != null && amount > 0) { Text("Add") } } }
}
@Composable
private fun FoodEditor(existing: FoodEntity?, action: String, save: (FoodEntity) -> Unit, cancel: () -> Unit) {
    var name by remember { mutableStateOf(existing?.name.orEmpty()) }; var calories by remember { mutableStateOf(existing?.caloriesPer100g?.toString().orEmpty()) }; var protein by remember { mutableStateOf(existing?.proteinPer100g?.toString().orEmpty()) }; var fiber by remember { mutableStateOf(existing?.fiberPer100g?.toString().orEmpty()) }; var fat by remember { mutableStateOf(existing?.fatPer100g?.toString().orEmpty()) }
    val values = listOf(calories, protein, fiber, fat).map { it.toDoubleOrNull() }; val valid = name.isNotBlank() && values.all { it != null && it >= 0 }
    Column(Modifier.padding(20.dp)) { Text(if (existing == null) "New Food" else "Edit Food", style = MaterialTheme.typography.headlineSmall); Field("Food Name", name) { name = it }; Field("Calories per 100 g", calories) { calories = it }; Field("Protein per 100 g", protein) { protein = it }; Field("Fiber per 100 g", fiber) { fiber = it }; Field("Fat per 100 g", fat) { fat = it }; Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) { Button(cancel) { Text("Cancel") }; Spacer(Modifier.padding(4.dp)); Button({ save(FoodEntity(existing?.id ?: 0, name.trim(), values[0]!!, values[1]!!, values[2]!!, values[3]!!, existing?.lastUsedAt ?: 0)) }, enabled = valid) { Text(action) } } }
}
@Composable private fun Field(label: String, value: String, change: (String) -> Unit) { OutlinedTextField(value, change, Modifier.fillMaxWidth().padding(top = 7.dp), label = { Text(label) }, singleLine = true) }
private fun format(value: Double): String = String.format(Locale.US, "%.1f", value).removeSuffix(".0")

