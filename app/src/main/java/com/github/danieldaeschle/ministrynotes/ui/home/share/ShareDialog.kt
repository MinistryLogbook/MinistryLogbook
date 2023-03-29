package com.github.danieldaeschle.ministrynotes.ui.home.share

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.ui.home.detailssection.ShareOption


@Composable
fun ShareDialog(open: Boolean = false, cancel: () -> Unit = {}, share: () -> Unit = {}) {
    var selectedShareOption by remember { mutableStateOf(ShareOption.Text) }

    if (open) {
        AlertDialog(onDismissRequest = cancel, title = { Text("Share as") }, text = {
            Column {
                Row {
                    Column(
                        Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ShareAsOption(
                            selected = selectedShareOption == ShareOption.Text,
                            onClick = {
                                selectedShareOption = ShareOption.Text
                            }) {
                            Icon(
                                Icons.Rounded.Description,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(112.dp)
                                    .padding(16.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Text")
                    }
                    Column(
                        Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ShareAsOption(
                            selected = selectedShareOption == ShareOption.Image,
                            onClick = {
                                selectedShareOption = ShareOption.Image
                            }) {
                            Icon(
                                Icons.Rounded.Image,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(112.dp)
                                    .padding(16.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Image")
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(value = "", onValueChange = {}, placeholder = {
                    Text("E.g., sickness, LDC, pioneer school etc.")
                }, label = {
                    Text("Comment")
                })
            }
        }, confirmButton = {
            TextButton(onClick = share) {
                Text("Share")
            }
        }, dismissButton = {
            TextButton(onClick = cancel) {
                Text("Cancel")
            }
        })
    }
}

@Composable
fun ShareDialogContent() {
    var selectedShareOption by remember { mutableStateOf(ShareOption.Text) }

    Column {
        Row {
            Column(
                Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShareAsOption(
                    selected = selectedShareOption == ShareOption.Text,
                    onClick = {
                        selectedShareOption = ShareOption.Text
                    }) {
                    Icon(
                        Icons.Rounded.Description,
                        contentDescription = null,
                        modifier = Modifier
                            .size(112.dp)
                            .padding(16.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text("Text")
            }
            Column(
                Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShareAsOption(
                    selected = selectedShareOption == ShareOption.Image,
                    onClick = {
                        selectedShareOption = ShareOption.Image
                    }) {
                    Icon(
                        Icons.Rounded.Image,
                        contentDescription = null,
                        modifier = Modifier
                            .size(112.dp)
                            .padding(16.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text("Image")
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = "", onValueChange = {}, placeholder = {
            Text("E.g., sickness, LDC, pioneer school etc.")
        }, label = {
            Text("Comment")
        })
    }
}

@Composable
fun ShareAsOption(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary.copy(0.6f) else MaterialTheme.colorScheme.onSurface.copy(
            0.6f
        )

    Box(
        Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .border(
                BorderStroke(2.5.dp, borderColor), CircleShape
            )
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}