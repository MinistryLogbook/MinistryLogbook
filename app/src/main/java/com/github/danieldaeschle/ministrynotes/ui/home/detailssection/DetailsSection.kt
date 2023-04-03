package com.github.danieldaeschle.ministrynotes.ui.home.detailssection

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.data.Time
import com.github.danieldaeschle.ministrynotes.data.ministryTimeSum
import com.github.danieldaeschle.ministrynotes.data.rememberSettingsDataStore
import com.github.danieldaeschle.ministrynotes.data.theocraticAssignmentTimeSum
import com.github.danieldaeschle.ministrynotes.data.theocraticSchoolTimeSum
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
    val context = LocalContext.current
    val entries = homeViewModel.entries.collectAsState()
    val studies = homeViewModel.studies.collectAsState(0)
    var isShareDialogOpen by remember { mutableStateOf(false) }
    val settingsDataStore = rememberSettingsDataStore()
    val goal = settingsDataStore.goal.collectAsState(50)
    // credit will be added until 55 hours are reached
    val maxHoursWithCredit = Time(55, 0)

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
                    val ministryTime = entries.value.ministryTimeSum()
                    val theocraticAssignmentsTime = entries.value.theocraticAssignmentTimeSum()
                    val theocraticSchoolTime = entries.value.theocraticSchoolTimeSum()
                    val credit = minOf(
                        maxHoursWithCredit - ministryTime,
                        theocraticAssignmentsTime
                    ) + theocraticSchoolTime
                    val accumulatedTime = ministryTime.let {
                        if (it.hours < maxHoursWithCredit.hours) {
                            minOf(ministryTime + theocraticAssignmentsTime, maxHoursWithCredit)
                        } else {
                            it
                        }
                    } + theocraticSchoolTime

                    CircleProgress(
                        modifier = Modifier.size(widthDp / 2, widthDp / 2),
                        baseLineColor = ProgressPositive.copy(0.15f),
                        progresses = listOf(
                            Progress(
                                percent = (100 / (goal.value ?: 50) * accumulatedTime.hours),
                                color = ProgressPositive.copy(0.6f)
                            ),
                            Progress(
                                percent = (100 / (goal.value ?: 50) * ministryTime.hours),
                                color = ProgressPositive
                            ),
                        ),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Counter(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            entries = entries.value
                        )

                        if (credit > Time(0, 0)) {
                            Row(
                                Modifier
                                    .clip(CircleShape)
                                    .defaultMinSize(minWidth = 48.dp)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val creditMinutes =
                                    if (credit.minutes > 0) ":${
                                        credit.minutes.toString().padStart(2, '0')
                                    }" else ""

                                Icon(
                                    painterResource(R.drawable.ic_volunteer_activism),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                val text = "${credit.hours}${creditMinutes} hrs"
                                Text(
                                    text,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OtherDetails()
    }
}
