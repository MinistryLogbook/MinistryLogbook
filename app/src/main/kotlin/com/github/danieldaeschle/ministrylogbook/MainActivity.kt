package com.github.danieldaeschle.ministrylogbook

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.github.danieldaeschle.ministrylogbook.ui.AppNavHost
import com.github.danieldaeschle.ministrylogbook.ui.theme.MinistryLogbookTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MinistryLogbookTheme {
                AppNavHost()
            }
        }
    }
}
