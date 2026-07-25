package com.aniruddhasonawane.calburn.screens.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aniruddhasonawane.calburn.data.local.DatabaseProvider
import com.aniruddhasonawane.calburn.data.local.FoodCategory
import com.aniruddhasonawane.calburn.data.local.FoodLibraryEntity
import com.aniruddhasonawane.calburn.data.repository.FoodLibraryRepository
import com.aniruddhasonawane.calburn.viewmodel.FoodLibraryViewModel
import com.aniruddhasonawane.calburn.viewmodel.FoodLibraryViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabTwoScreen(
    onFoodClick: (Long) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<FoodCategory?>(null) }
    var favoritesOnly by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val factory = remember {
        FoodLibraryViewModelFactory(
            FoodLibraryRepository(DatabaseProvider.getDatabase(context).foodLibraryDao())
        )
    }
    val viewModel: FoodLibraryViewModel = viewModel(factory = factory)
    val foods by viewModel.foods.collectAsStateWithLifecycle()
    val filteredFoods = remember(foods, search, selectedCategory, favoritesOnly) {
        foods.filter { food ->
            food.name.contains(search, ignoreCase = true) &&
                (selectedCategory == null || food.category == selectedCategory) &&
                (!favoritesOnly || food.isFavorite)
        }
    }
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Food Library",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true,
                label = {
                    Text("Search food")
                }
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            AssistChip(
                                onClick = { favoritesOnly = !favoritesOnly },
                                label = { Text("Favorites") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (favoritesOnly) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                        items(FoodCategory.entries) { category ->
                            AssistChip(
                                onClick = {
                                    selectedCategory = if (selectedCategory == category) null else category
                                },
                                label = { Text(category.name.lowercase().replaceFirstChar { it.titlecase() }) }
                            )
                        }
                    }
                }

                if (filteredFoods.isEmpty()) {
                    item {
                        Text(
                            text = "No foods found.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }

                items(filteredFoods, key = { it.id }) { food ->
                    FoodLibraryCard(
                        food = food,
                        onClick = { onFoodClick(food.id) },
                        onFavoriteClick = { viewModel.setFavorite(food, !food.isFavorite) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodLibraryCard(
    food: FoodLibraryEntity,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${food.servingSize} ${food.servingUnit} - ${food.calories} kcal",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "P ${food.protein}g - F ${food.fat}g - C ${food.carbohydrates}g",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (food.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (food.isFavorite) "Remove ${food.name} from favorites" else "Add ${food.name} to favorites",
                    tint = if (food.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
