package com.aniruddhasonawane.calburn.screens.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aniruddhasonawane.calburn.data.local.StarterFoods

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabTwoScreen() {

    var search by remember { mutableStateOf("") }

    val foods = remember(search) {
        StarterFoods.foods.filter {
            it.name.contains(search, ignoreCase = true)
        }
    }

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

            LazyColumn {

                items(foods) { food ->

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            )
                    ) {

                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "${food.servingSize} ${food.servingUnit}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                    }

                    Divider()

                }

            }

        }

    }

}