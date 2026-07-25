package com.aniruddhasonawane.calburn.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aniruddhasonawane.calburn.data.local.DatabaseProvider
import com.aniruddhasonawane.calburn.data.local.FoodLibraryEntity
import com.aniruddhasonawane.calburn.data.repository.FoodLibraryRepository
import com.aniruddhasonawane.calburn.viewmodel.FoodLibraryViewModel
import com.aniruddhasonawane.calburn.viewmodel.FoodLibraryViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailsScreen(
    foodId: Long,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val factory = remember {
        FoodLibraryViewModelFactory(
            FoodLibraryRepository(DatabaseProvider.getDatabase(context).foodLibraryDao())
        )
    }
    val viewModel: FoodLibraryViewModel = viewModel(factory = factory)
    val foods by viewModel.foods.collectAsStateWithLifecycle()
    val food = foods.firstOrNull { it.id == foodId }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(food?.name ?: "Food details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    food?.let { selectedFood ->
                        IconButton(
                            onClick = {
                                viewModel.setFavorite(selectedFood, !selectedFood.isFavorite)
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedFood.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (selectedFood.isFavorite) "Remove from favorites" else "Add to favorites"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            food != null -> FoodDetailsContent(
                food = food,
                modifier = Modifier.padding(paddingValues),
                onAddClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Quantity selection will be available next.")
                    }
                }
            )

            foods.isEmpty() -> Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }

            else -> Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Food not found.")
            }
        }
    }
}

@Composable
private fun FoodDetailsContent(
    food: FoodLibraryEntity,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(food.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "Serving: ${food.servingSize} ${food.servingUnit}",
                style = MaterialTheme.typography.titleMedium
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = "${food.calories} kcal",
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            NutritionSection(
                title = "Macronutrients",
                values = listOf(
                    "Protein" to "${food.protein} g",
                    "Fat" to "${food.fat} g",
                    "Carbohydrates" to "${food.carbohydrates} g",
                    "Fiber" to "${food.fiber} g",
                    "Sugar" to "${food.sugar} g"
                )
            )
        }
        item {
            NutritionSection(
                title = "Micronutrients",
                values = listOf(
                    "Sodium" to "${food.sodium} mg",
                    "Potassium" to "${food.potassium} mg",
                    "Calcium" to "${food.calcium} mg",
                    "Iron" to "${food.iron} mg",
                    "Vitamin C" to "${food.vitaminC} mg",
                    "Vitamin D" to "${food.vitaminD} mcg",
                    "Vitamin B12" to "${food.vitaminB12} mcg"
                )
            )
        }
        item {
            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Add Food")
            }
        }
    }
}

@Composable
private fun NutritionSection(
    title: String,
    values: List<Pair<String, String>>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            values.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label)
                    Text(value, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
