package com.github.danieldaeschle.ministrynotes.lib

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialog(
    isOpen: Boolean = false,
    title: @Composable () -> Unit = {},
    negativeButton: @Composable () -> Unit = {},
    onClose: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    if (isOpen) {
        androidx.compose.material3.AlertDialog(onDismissRequest = onClose) {
            Surface(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {
                Column(Modifier.padding(vertical = 24.dp)) {
                    ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                        Box(
                            Modifier
                                .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
                                .align(Alignment.Start)
                        ) {
                            title()
                        }
                    }

                    Column(Modifier.padding(bottom = 16.dp)) {
                        content()
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        negativeButton()
                    }
                }
            }
        }
    }
}