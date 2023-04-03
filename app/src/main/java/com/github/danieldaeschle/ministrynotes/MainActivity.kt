package com.github.danieldaeschle.ministrynotes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.github.danieldaeschle.ministrynotes.ui.AppNavHost
import com.github.danieldaeschle.ministrynotes.ui.theme.MinistryNotesTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinistryNotesTheme {
                AppNavHost()
            }
        }
    }
}
