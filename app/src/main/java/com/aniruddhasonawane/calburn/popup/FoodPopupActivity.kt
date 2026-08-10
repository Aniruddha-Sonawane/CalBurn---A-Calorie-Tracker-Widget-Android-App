package com.aniruddhasonawane.calburn.popup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.aniruddhasonawane.calburn.CalBurnApplication
import com.aniruddhasonawane.calburn.entity.FoodEntity
import com.aniruddhasonawane.calburn.ui.theme.CalBurnTheme
import com.aniruddhasonawane.calburn.viewmodel.FoodPopupViewModel
import com.aniruddhasonawane.calburn.viewmodel.FoodPopupViewModelFactory
import com.aniruddhasonawane.calburn.viewmodel.TAB_ALL
import com.aniruddhasonawane.calburn.viewmodel.TAB_RECENT
import com.aniruddhasonawane.calburn.widget.WidgetUpdater
import kotlinx.coroutines.launch
import java.util.Locale

class FoodPopupActivity : ComponentActivity() {

    private val viewModel: FoodPopupViewModel by viewModels {
        FoodPopupViewModelFactory(
            (application as CalBurnApplication).repository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CalBurnTheme(
                darkTheme = true,
                dynamicColor = false
            ) {
                MaterialTheme(
                    colorScheme = darkColorScheme(
                        primary = Color(0xFFFF8A3D),
                        background = Color(0xFF121212),
                        surface = Color(0xFF202020),
                        onSurface = Color.White,
                        onBackground = Color.White,
                        secondary = Color(0xFF85D9A7)
                    )
                ) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        var visible by remember {
                            mutableStateOf(false)
                        }

                        LaunchedEffect(Unit) {
                            visible = true
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn() +
                                    scaleIn(initialScale = 0.92f)
                        ) {
                            Surface(
                                Modifier
                                    .fillMaxWidth(0.9f)
                                    .widthIn(max = 640.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = Color(0xFF121212),
                                tonalElevation = 8.dp,
                                shadowElevation = 18.dp
                            ) {
                                PopupRoot(
                                    viewModel,
                                    ::finishAndRefresh
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun finishAndRefresh() {
        lifecycleScope.launch {
            WidgetUpdater.updateAll(this@FoodPopupActivity)
            finish()
        }
    }
}

private enum class PopupMode {
    LIST,
    NEW,
    ADD,
    EDIT
}

@Composable
private fun PopupRoot(
    viewModel: FoodPopupViewModel,
    complete: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var mode by remember {
        mutableStateOf(PopupMode.LIST)
    }

    var selected by remember {
        mutableStateOf<FoodEntity?>(null)
    }

    var searchActive by remember {
        mutableStateOf(false)
    }

    when (mode) {

        PopupMode.LIST -> FoodList(
            query = state.query,
            searchActive = searchActive,
            categories = state.categories,
            selectedTab = state.selectedTab,
            displayedFoods = state.displayedFoods,

            onQuery = viewModel::search,

            onToggleSearch = {
                searchActive = !searchActive

                if (!searchActive) {
                    viewModel.search("")
                }
            },

            onSelectTab = viewModel::selectTab,

            newFood = {
                mode = PopupMode.NEW
            },

            add = {
                selected = it
                mode = PopupMode.ADD
            },

            edit = {
                selected = it
                mode = PopupMode.EDIT
            },

            delete = viewModel::delete
        )

        PopupMode.NEW -> FoodEditor(
            existing = null,
            action = "Save & Add",
            categories = state.categories,

            save = { food, qty ->
                viewModel.saveAndAdd(
                    food,
                    qty,
                    complete
                )
            },

            cancel = {
                mode = PopupMode.LIST
            }
        )

        PopupMode.ADD -> selected?.let { food ->

            FoodAmount(
                food = food,

                add = { grams ->
                    viewModel.add(
                        food,
                        grams,
                        complete
                    )
                },

                cancel = {
                    mode = PopupMode.LIST
                }
            )
        }

        PopupMode.EDIT -> selected?.let { food ->

            FoodEditor(
                existing = food,
                action = "Save",
                categories = state.categories,

                save = { updated, _ ->
                    viewModel.update(updated)
                    mode = PopupMode.LIST
                },

                cancel = {
                    mode = PopupMode.LIST
                }
            )
        }
    }
}

@Composable
private fun PopupTopBar(
    searchActive: Boolean,
    query: String,
    onQuery: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onNewFood: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = 10.dp,
                top = 18.dp,
                bottom = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (searchActive) {

            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Search foods")
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null
                    )
                }
            )

            IconButton(
                onClick = {
                    onQuery("")
                    onToggleSearch()
                }
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close search"
                )
            }

        } else {

            Text(
                "CalBurn",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onToggleSearch
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search"
                )
            }

            IconButton(
                onClick = onNewFood
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "New food"
                )
            }
        }
    }
}

@Composable
private fun CategoryTabs(
    categories: List<String>,
    selectedTab: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        item {
            TabChip(
                selected = selectedTab == TAB_RECENT,
                onClick = {
                    onSelect(TAB_RECENT)
                }
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "Recently used",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        item {
            TabChip(
                selected = selectedTab == TAB_ALL,
                onClick = {
                    onSelect(TAB_ALL)
                }
            ) {
                Text("All")
            }
        }

        items(categories) { category ->

            TabChip(
                selected = selectedTab.equals(
                    category,
                    ignoreCase = true
                ),
                onClick = {
                    onSelect(category)
                }
            ) {
                Text(category)
            }
        }
    }
}

@Composable
private fun TabChip(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color(0xFF202020)
        },
        contentColor = if (selected) {
            Color.Black
        } else {
            Color(0xFFE0E0E0)
        }
    ) {
        Box(
            Modifier.padding(
                horizontal = 14.dp,
                vertical = 8.dp
            )
        ) {
            content()
        }
    }
}

@Composable
private fun FoodList(
    query: String,
    searchActive: Boolean,
    categories: List<String>,
    selectedTab: String,
    displayedFoods: List<FoodEntity>,
    onQuery: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onSelectTab: (String) -> Unit,
    newFood: () -> Unit,
    add: (FoodEntity) -> Unit,
    edit: (FoodEntity) -> Unit,
    delete: (FoodEntity) -> Unit
) {
    var pendingDelete by remember {
        mutableStateOf<FoodEntity?>(null)
    }

    Column(
        Modifier.widthIn(max = 600.dp)
    ) {

        PopupTopBar(
            searchActive = searchActive,
            query = query,
            onQuery = onQuery,
            onToggleSearch = onToggleSearch,
            onNewFood = newFood
        )

        if (!searchActive) {

            CategoryTabs(
                categories = categories,
                selectedTab = selectedTab,
                onSelect = onSelectTab
            )

            Spacer(
                Modifier.height(10.dp)
            )

        } else {

            Spacer(
                Modifier.height(6.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .heightIn(max = 380.dp)
                .padding(horizontal = 20.dp),

            contentPadding = PaddingValues(
                vertical = 4.dp
            )
        ) {

            if (displayedFoods.isEmpty()) {

                item {
                    Text(
                        "No foods here yet",
                        Modifier.padding(
                            vertical = 24.dp
                        ),
                        color = Color.Gray
                    )
                }
            }

            items(
                displayedFoods,
                key = {
                    it.id
                }
            ) { food ->

                FoodRow(
                    food = food,

                    add = {
                        add(food)
                    },

                    edit = {
                        edit(food)
                    },

                    delete = {
                        pendingDelete = food
                    }
                )
            }

            item {
                Spacer(
                    Modifier.height(16.dp)
                )
            }
        }
    }

    pendingDelete?.let { food ->

        AlertDialog(
            onDismissRequest = {
                pendingDelete = null
            },

            title = {
                Text("Delete ${food.name}?")
            },

            text = {
                Text(
                    "Saved food will be deleted. Existing history remains."
                )
            },

            confirmButton = {

                Button(
                    onClick = {
                        delete(food)
                        pendingDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },

            dismissButton = {

                Button(
                    onClick = {
                        pendingDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FoodRow(
    food: FoodEntity,
    add: () -> Unit,
    edit: () -> Unit,
    delete: () -> Unit
) {
    var menu by remember {
        mutableStateOf(false)
    }

    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .combinedClickable(
                onClick = add,
                onLongClick = edit
            ),

        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF202020)
        )
    ) {

        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                food.name,
                Modifier.weight(1f)
            )

            Box {

                IconButton(
                    onClick = {
                        menu = true
                    }
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        "Food menu"
                    )
                }

                DropdownMenu(
                    expanded = menu,
                    onDismissRequest = {
                        menu = false
                    }
                ) {

                    DropdownMenuItem(
                        text = {
                            Text("Edit")
                        },

                        onClick = {
                            menu = false
                            edit()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text("Delete")
                        },

                        onClick = {
                            menu = false
                            delete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodAmount(
    food: FoodEntity,
    add: (Double) -> Unit,
    cancel: () -> Unit
) {

    /*
     * multiplier:
     *
     * X1 = one quantity
     * X2 = two quantities
     * X3 = three quantities
     *
     * The important part is that baseGrams is NOT permanently tied
     * to food.defaultGrams after the user edits the quantity.
     */
    var multiplier by remember {
        mutableStateOf(1)
    }

    /*
     * The quantity currently displayed to the user.
     */
    var grams by remember {
        mutableStateOf(
            format(food.defaultGrams)
        )
    }

    /*
     * This is the quantity represented by X1 for THIS popup only.
     *
     * Example:
     *
     * Saved default = 200 g
     * X1 = 200 g
     *
     * User manually enters 350 g
     * baseGrams becomes 350 g
     *
     * X2 = 700 g
     * X3 = 1050 g
     *
     * The saved FoodEntity is NEVER changed.
     */
    var baseGrams by remember {
        mutableStateOf(food.defaultGrams)
    }

    val amount =
        grams.toDoubleOrNull() ?: 0.0

    val factor =
        amount / 100.0

    Column(
        Modifier.padding(20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = food.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    text = "X",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF8A3D)
                )

                Spacer(
                    Modifier.width(4.dp)
                )

                Text(
                    text = multiplier.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                /*
                 * MINUS
                 *
                 * Important:
                 * It uses baseGrams, which represents the user's
                 * current custom quantity, rather than the saved
                 * FoodEntity.defaultGrams.
                 */
                IconButton(
                    onClick = {

                        if (multiplier > 1) {

                            multiplier--

                            val newGrams =
                                baseGrams * multiplier

                            grams =
                                format(newGrams)
                        }
                    },

                    enabled = multiplier > 1,

                    modifier = Modifier.size(52.dp)
                ) {

                    Text(
                        text = "−",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color =
                            if (multiplier > 1) {
                                Color.White
                            } else {
                                Color(0xFF666666)
                            }
                    )
                }

                /*
                 * PLUS
                 *
                 * Uses the CURRENT popup quantity basis.
                 * It does not go back to the saved food data.
                 */
                IconButton(
                    onClick = {

                        multiplier++

                        val newGrams =
                            baseGrams * multiplier

                        grams =
                            format(newGrams)
                    },

                    modifier = Modifier.size(52.dp)
                ) {

                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(
            Modifier.height(10.dp)
        )

        /*
         * Editable grams.
         *
         * When the user changes this manually, we update
         * baseGrams so future +/- operations use the user's
         * custom quantity rather than the saved food quantity.
         *
         * Example:
         *
         * X2 / 400g
         * user changes to 350g
         *
         * baseGrams = 350 / 2 = 175g
         *
         * + -> X3 / 525g
         * - -> X1 / 175g
         *
         * This keeps the multiplier mathematically proportional
         * to the current custom quantity.
         */
        OutlinedTextField(
            value = grams,

            onValueChange = { newValue ->

                if (
                    newValue.isEmpty() ||
                    newValue.matches(
                        Regex("^\\d*(\\.\\d*)?$")
                    )
                ) {

                    grams = newValue

                    val editedGrams =
                        newValue.toDoubleOrNull()

                    if (
                        editedGrams != null &&
                        editedGrams > 0 &&
                        multiplier > 0
                    ) {

                        baseGrams =
                            editedGrams / multiplier
                    }
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp),

            label = {
                Text("Grams")
            },

            suffix = {
                Text("g")
            },

            singleLine = true,

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        Column(
            Modifier.padding(top = 16.dp)
        ) {

            MacroRow(
                "Calories",
                format(
                    food.caloriesPer100g * factor
                )
            )

            MacroRow(
                "Protein",
                "${format(
                    food.proteinPer100g * factor
                )} g"
            )

            MacroRow(
                "Fiber",
                "${format(
                    food.fiberPer100g * factor
                )} g"
            )

            MacroRow(
                "Fat",
                "${format(
                    food.fatPer100g * factor
                )} g"
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),

            horizontalArrangement = Arrangement.End
        ) {

            Button(
                onClick = cancel
            ) {
                Text("Cancel")
            }

            Spacer(
                Modifier.width(8.dp)
            )

            Button(
                onClick = {

                    if (amount > 0) {
                        add(amount)
                    }
                },

                enabled = amount > 0
            ) {
                Text("Add")
            }
        }
    }
}

@Composable
private fun MacroRow(
    label: String,
    value: String
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            label,
            color = Color.Gray
        )

        Text(
            value,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FoodEditor(
    existing: FoodEntity?,
    action: String,
    categories: List<String>,
    save: (FoodEntity, Double) -> Unit,
    cancel: () -> Unit
) {

    var name by remember {
        mutableStateOf(
            existing?.name.orEmpty()
        )
    }

    var category by remember {
        mutableStateOf(
            existing?.category.orEmpty()
        )
    }

    var quantity by remember {
        mutableStateOf(
            existing?.defaultGrams?.let(::format)
                ?: "100"
        )
    }

    var calories by remember {
        mutableStateOf(
            existing?.caloriesPer100g
                ?.toString()
                .orEmpty()
        )
    }

    var protein by remember {
        mutableStateOf(
            existing?.proteinPer100g
                ?.toString()
                .orEmpty()
        )
    }

    var fiber by remember {
        mutableStateOf(
            existing?.fiberPer100g
                ?.toString()
                .orEmpty()
        )
    }

    var fat by remember {
        mutableStateOf(
            existing?.fatPer100g
                ?.toString()
                .orEmpty()
        )

    }

    val qty =
        quantity.toDoubleOrNull()

    val values = listOf(
        calories,
        protein,
        fiber,
        fat
    ).map {
        it.toDoubleOrNull()
    }

    val valid =
        name.isNotBlank() &&
        qty != null &&
        qty > 0 &&
        values.all {
            it != null && it >= 0
        }

    Column(
        Modifier
            .padding(20.dp)
            .verticalScroll(
                rememberScrollState()
            )
    ) {

        Text(
            if (existing == null) {
                "New Food"
            } else {
                "Edit Food"
            },

            style =
                MaterialTheme.typography.headlineSmall
        )

        Field(
            "Food Name",
            name
        ) {
            name = it
        }

        Field(
            "Category",
            category
        ) {
            category = it
        }

        if (categories.isNotEmpty()) {

            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),

                horizontalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {

                items(categories) { c ->

                    AssistChip(
                        onClick = {
                            category = c
                        },

                        label = {
                            Text(c)
                        }
                    )
                }
            }
        }

        Field(
            "Quantity (g)",
            quantity
        ) {
            quantity = it
        }

        Text(
            "Enter the nutrition values for the quantity above. CalBurn will work out the per-100g values automatically.",

            style =
                MaterialTheme.typography.bodySmall,

            color = Color.Gray,

            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 4.dp
            )
        )

        Field(
            "Calories",
            calories
        ) {
            calories = it
        }

        Field(
            "Protein (g)",
            protein
        ) {
            protein = it
        }

        Field(
            "Fiber (g)",
            fiber
        ) {
            fiber = it
        }

        Field(
            "Fat (g)",
            fat
        ) {
            fat = it
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),

            horizontalArrangement =
                Arrangement.End
        ) {

            Button(
                onClick = cancel
            ) {
                Text("Cancel")
            }

            Spacer(
                Modifier.width(8.dp)
            )

            Button(
                onClick = {

                    val factor =
                        100.0 / qty!!

                    save(

                        FoodEntity(
                            id = existing?.id ?: 0,

                            name = name.trim(),

                            category =
                                category.trim().ifBlank {
                                    "General"
                                },

                            caloriesPer100g =
                                values[0]!! * factor,

                            proteinPer100g =
                                values[1]!! * factor,

                            fiberPer100g =
                                values[2]!! * factor,

                            fatPer100g =
                                values[3]!! * factor,

                            defaultGrams = qty!!,

                            lastUsedAt =
                                existing?.lastUsedAt ?: 0
                        ),

                        qty!!
                    )
                },

                enabled = valid
            ) {
                Text(action)
            }
        }
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    change: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = change,

        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 7.dp),

        label = {
            Text(label)
        },

        singleLine = true
    )
}

private fun format(
    value: Double
): String =
    String.format(
        Locale.US,
        "%.1f",
        value
    ).removeSuffix(".0")
