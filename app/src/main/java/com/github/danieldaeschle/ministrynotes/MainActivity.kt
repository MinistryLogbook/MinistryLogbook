package com.github.danieldaeschle.ministrynotes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.github.danieldaeschle.ministrynotes.ui.AppNavHost
import com.github.danieldaeschle.ministrynotes.ui.theme.MinistryNotesTheme
import dagger.hilt.android.AndroidEntryPoint

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            MinistryNotesTheme {
                AppNavHost()
            }
        }
    }
}
