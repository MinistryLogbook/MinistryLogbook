package com.github.danieldaeschle.ministrynotes.ui.home.detailssection

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.data.rememberSettingsDataStore
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.home.HomeGraph
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.HomeViewModel
import com.github.danieldaeschle.ministrynotes.ui.theme.ProgressPositive
import org.koin.androidx.compose.koinViewModel
import java.time.format.TextStyle
import java.util.Locale

enum class ShareOption {
    Text, Image
}

@Composable
fun DetailsSection(homeViewModel: HomeViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current
    val context = LocalContext.current
    val entries = homeViewModel.entries.collectAsState()
    val studies = homeViewModel.studies.collectAsState(0)
    var isShareDialogOpen by remember { mutableStateOf(false) }
    val settingsDataStore = rememberSettingsDataStore()
    val goal = settingsDataStore.goal.collectAsState(50)

    val handleShare = {
        isShareDialogOpen = false
        val hours = entries.value.sumOf { it.hours }
        val minutes = entries.value.sumOf { it.minutes }
        val allHours = hours + minutes / 60
        val placements = entries.value.sumOf { it.placements }
        val videoShowings = entries.value.sumOf { it.videoShowings }
        val returnVisits = entries.value.sumOf { it.returnVisits }
        val monthName = homeViewModel.selectedMonth.value.month.getDisplayName(
            TextStyle.FULL, Locale.ENGLISH
        )
        val text = """
            My field service report for the month: $monthName
            
            Hours: $allHours
            Placements: $placements
            Video showings: $videoShowings
            Return visits: $returnVisits
            Studies: ${studies.value}
        """.trimIndent()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    val handleCreate = {
        navController.navigate(HomeGraph.EntryDetails.createRoute())
    }

    Column(modifier = Modifier.padding(16.dp)) {
        BoxWithConstraints {
            val widthDp = LocalDensity.current.run {
                min(constraints.maxWidth.toDp(), LocalConfiguration.current.screenHeightDp.dp)
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(widthDp / 2)
                        .width(widthDp / 2)
                ) {
                    val accumulatedHours = entries.value.sumOf { it.hours }
                    val accumulatedMinutes = entries.value.sumOf { it.minutes }
                    val hoursWithMinutes = accumulatedHours + accumulatedMinutes / 60
                    val accumulatedCreditHours = entries.value.sumOf { it.creditHours }
                    val accumulatedCreditMinutes = entries.value.sumOf { it.creditMinutes }
                    val creditHoursWithMinutes = accumulatedCreditHours + accumulatedCreditMinutes / 60
                    val hoursWithCredit = hoursWithMinutes + creditHoursWithMinutes

                    CircleProgress(
                        modifier = Modifier.size(widthDp / 2, widthDp / 2),
                        baseLineColor = ProgressPositive,
                        progresses = listOf(
                            Progress(percent = (100 / (goal.value ?: 50) * hoursWithCredit), color = ProgressPositive),
                        ),
                    )
                    Column(modifier = Modifier.fillMaxSize().padding(vertical = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Counter(modifier = Modifier.weight(1f).fillMaxWidth(), entries = entries.value)

                        if (accumulatedCreditHours > 0 || accumulatedCreditMinutes > 0) {
                            Row(
                                Modifier
                                    .clip(CircleShape)
                                    .defaultMinSize(minWidth = 48.dp)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val restCreditMinutes = accumulatedCreditMinutes % 60
                                val creditMinutes =
                                    if (restCreditMinutes > 0) ":${restCreditMinutes.toString().padStart(2, '0')}" else ""

                                Icon(painterResource(R.drawable.ic_volunteer_activism), contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                val text = "${creditHoursWithMinutes}${creditMinutes} hrs"
                                Text(text, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        OtherDetails()
//        Spacer(modifier = Modifier.height(16.dp))
//        Row {
//            Button(
//                elevation = null,
//                modifier = Modifier
//                    .weight(1f)
//                    .height(46.dp)
//                    .indication(
//                        remember { MutableInteractionSource() },
//                        rememberRipple(color = MaterialTheme.colorScheme.primary)
//                    ),
//                colors = ButtonDefaults.textButtonColors(
//                    containerColor = MaterialTheme.colorScheme.primary.copy(
//                        0.15f
//                    ), contentColor = MaterialTheme.colorScheme.primary
//                ),
//                onClick = { isShareDialogOpen = true },
//                shape = RoundedCornerShape(10.dp),
//            ) {
//                Box {
//                    Icon(painterResource(R.drawable.ic_share), contentDescription = "Share icon")
//                    Text(
//                        "Share",
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .align(Alignment.Center),
//                    )
//                }
//            }
//            Spacer(modifier = Modifier.width(16.dp))
//            Button(
//                modifier = Modifier
//                    .weight(1f)
//                    .height(46.dp),
//                colors = ButtonDefaults.textButtonColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    contentColor = contentColorFor(MaterialTheme.colorScheme.primary)
//                ),
//                onClick = handleCreate,
//                shape = RoundedCornerShape(10.dp)
//            ) {
//                Text("Add to report")
//            }
//        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
