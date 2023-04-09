package com.github.danieldaeschle.ministrynotes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.github.danieldaeschle.ministrynotes.ui.AppNavHost
import com.github.danieldaeschle.ministrynotes.ui.theme.MinistryNotesTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MinistryNotesTheme {
                AppNavHost()
            }
        }
    }
}
