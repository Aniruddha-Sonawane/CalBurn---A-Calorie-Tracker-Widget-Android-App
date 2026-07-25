
package com.aniruddhasonawane.calburn.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.aniruddhasonawane.calburn.screens.FoodDetailsScreen
import com.aniruddhasonawane.calburn.screens.ModalScreen
import com.aniruddhasonawane.calburn.screens.NotFoundScreen

object Routes {
    const val TABS = "tabs"
    const val MODAL = "modal"
    const val NOT_FOUND = "not_found"
    const val FOOD_DETAILS = "food_details/{foodId}"

    fun foodDetails(foodId: Long) = "food_details/$foodId"
}

@Composable
fun RootNav() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.TABS,
        enterTransition = {
            EnterTransition.None
        },
        exitTransition = {
            ExitTransition.None
        },
        popEnterTransition = {
            EnterTransition.None
        },
        popExitTransition = {
            ExitTransition.None
        }
    ) {
        composable(Routes.TABS) {
            TabsNav(rootNav = navController)
        }

        composable(
            route = Routes.FOOD_DETAILS,
            arguments = listOf(navArgument("foodId") { type = NavType.LongType })
        ) { backStackEntry ->
            FoodDetailsScreen(
                foodId = backStackEntry.arguments?.getLong("foodId") ?: return@composable,
                onBackClick = navController::popBackStack
            )
        }

        composable(Routes.MODAL) {
            ModalScreen(navController = navController)
        }

        composable(Routes.NOT_FOUND) {
            NotFoundScreen(navController)
        }
    }
}
