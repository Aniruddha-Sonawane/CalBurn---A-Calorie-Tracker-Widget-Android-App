package com.aniruddhasonawane.calburn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.aniruddhasonawane.calburn.navigation.RootNav
import com.aniruddhasonawane.calburn.ui.theme.CalBurnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            CalBurnTheme {
                RootNav()
            }
        }
    }
}