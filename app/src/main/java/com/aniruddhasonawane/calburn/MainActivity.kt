package com.aniruddhasonawane.calburn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { installSplashScreen(); super.onCreate(savedInstanceState); finish() }
}
