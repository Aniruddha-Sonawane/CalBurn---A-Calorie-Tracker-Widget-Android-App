
package com.aniruddhasonawane.calburn.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aniruddhasonawane.calburn.screens.ModalScreen
import com.aniruddhasonawane.calburn.screens.NotFoundScreen

object Routes {
    const val TABS = "tabs"
    const val MODAL = "modal"
    const val NOT_FOUND = "not_found"
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

        composable(Routes.MODAL) {
            ModalScreen(navController = navController)
        }

        composable(Routes.NOT_FOUND) {
            NotFoundScreen(navController)
        }
    }
}
