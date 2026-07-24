package com.aniruddhasonawane.calburn.data.local

object StarterFoods {

    val foods = listOf(

        FoodLibraryEntity(
            name = "Milk",
            category = FoodCategory.DAIRY,
            servingSize = 100.0,
            servingUnit = "ml",
            calories = 66.0,
            protein = 3.2,
            fat = 4.0,
            carbohydrates = 5.0,
            fiber = 0.0
        ),

        FoodLibraryEntity(
            name = "Egg",
            category = FoodCategory.EGGS,
            servingSize = 1.0,
            servingUnit = "piece",
            calories = 70.0,
            protein = 6.0,
            fat = 5.0,
            carbohydrates = 0.6,
            fiber = 0.0
        ),

        FoodLibraryEntity(
            name = "Chicken Breast",
            category = FoodCategory.MEAT,
            servingSize = 100.0,
            servingUnit = "g",
            calories = 165.0,
            protein = 31.0,
            fat = 3.6,
            carbohydrates = 0.0,
            fiber = 0.0
        ),

        FoodLibraryEntity(
            name = "Curd",
            category = FoodCategory.DAIRY,
            servingSize = 100.0,
            servingUnit = "g",
            calories = 61.0,
            protein = 3.5,
            fat = 3.3,
            carbohydrates = 4.7,
            fiber = 0.0
        ),

        FoodLibraryEntity(
            name = "Paneer",
            category = FoodCategory.DAIRY,
            servingSize = 100.0,
            servingUnit = "g",
            calories = 265.0,
            protein = 18.0,
            fat = 20.8,
            carbohydrates = 1.2,
            fiber = 0.0
        ),

        FoodLibraryEntity(
            name = "Rice (Cooked)",
            category = FoodCategory.GRAINS,
            servingSize = 100.0,
            servingUnit = "g",
            calories = 130.0,
            protein = 2.7,
            fat = 0.3,
            carbohydrates = 28.0,
            fiber = 0.4
        ),

        FoodLibraryEntity(
            name = "Banana",
            category = FoodCategory.FRUITS,
            servingSize = 100.0,
            servingUnit = "g",
            calories = 89.0,
            protein = 1.1,
            fat = 0.3,
            carbohydrates = 22.8,
            fiber = 2.6
        )
    )
}