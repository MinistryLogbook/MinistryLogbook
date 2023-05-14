package com.github.danieldaeschle.ministrylogbook.lib

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
    title: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    confirmButton: (@Composable () -> Unit)? = null,
    onClose: () -> Unit = {},
    paddingValues: PaddingValues = PaddingValues(vertical = 24.dp),
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
                Column(Modifier.padding(paddingValues)) {
                    title?.let {
                        ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                            Box(
                                Modifier
                                    .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
                                    .align(Alignment.Start)
                            ) {
                                title()
                            }
                        }
                    }

                    Column(Modifier.condition(dismissButton != null) {
                        padding(bottom = 16.dp)
                    }) {
                        content()
                    }

                    if (dismissButton != null || confirmButton != null) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 24.dp),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            dismissButton?.let { dismissButton() }
                            confirmButton?.let { confirmButton() }
                        }
                    }
                }
            }
        }
    }
}