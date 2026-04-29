package app.ministrylogbook.ui.home

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ministrylogbook.MainActivity
import app.ministrylogbook.R
import app.ministrylogbook.shared.utilities.restartApp
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.home.backup.BackupImportDialog
import app.ministrylogbook.ui.home.biblestudies.BibleStudiesPage
import app.ministrylogbook.ui.home.time.TimePage
import app.ministrylogbook.ui.home.viewmodel.HomeIntent
import app.ministrylogbook.ui.home.viewmodel.HomeState
import app.ministrylogbook.ui.shared.Toolbar
import kotlinx.coroutines.launch

enum class PagerPage {
    Time,
    BibleStudies
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePage(state: HomeState, dispatch: (intent: HomeIntent) -> Unit = {}) {
    val navController = LocalAppNavController.current
    val pagerState = rememberPagerState(pageCount = { PagerPage.entries.size })
    val currentPage by remember(pagerState.currentPage) {
        derivedStateOf {
            PagerPage.entries.first { it.ordinal == pagerState.currentPage }
        }
    }
    val timeScrollState = rememberScrollState()
    val studiesScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var isMonthPickerExpanded by remember { mutableStateOf(false) }
    val backgroundBlur by animateDpAsState(
        targetValue = if (isMonthPickerExpanded) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = 180),
        label = "homeBackgroundBlur"
    )
    val context = LocalContext.current
    val scrollPosition by remember(currentPage) {
        derivedStateOf {
            when (currentPage) {
                PagerPage.Time -> timeScrollState.value
                else -> 0
            }
        }
    }

    BackHandler(currentPage == PagerPage.BibleStudies) {
        coroutineScope.launch {
            pagerState.scrollToPage(0)
        }
    }

    LaunchedEffect(state.importFinished) {
        if (state.importFinished) {
            context.restartApp(Intent(context, MainActivity::class.java))
        }
    }

    if (state.selectedBackupFile != null && state.isBackupValid) {
        BackupImportDialog(
            state.selectedBackupFile,
            state.latestEntry,
            onImport = {
                dispatch(HomeIntent.ImportBackup)
            },
            onDismiss = {
                dispatch(HomeIntent.DismissImportBackup)
            }
        )
    }

    Scaffold(
        modifier = Modifier.blur(backgroundBlur),
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            Toolbar(
                padding = PaddingValues(horizontal = 12.dp),
                elevation = if (scrollPosition > 0) 4.dp else 0.dp
            ) {
                ToolbarMonthSelect(
                    selectedMonth = state.month,
                    onExpandedChange = { },
                    onSelect = {
                        navController.navigateToMonth(it.year, it.month.ordinal + 1)
                    }
                )
                Spacer(Modifier.weight(1f))
                ToolbarActions(state.month)
            }
        },
        bottomBar = {
            NavigationBar {
                val isTimeSelected = pagerState.currentPage == 0
                NavigationBarItem(
                    selected = isTimeSelected,
                    onClick = {
                        if (!isTimeSelected) {
                            coroutineScope.launch {
                                pagerState.scrollToPage(0)
                            }
                        } else {
                            coroutineScope.launch {
                                timeScrollState.animateScrollTo(0)
                            }
                        }
                    },
                    label = {
                        Text(stringResource(R.string.time))
                    },
                    icon = {
                        Icon(
                            painterResource(R.drawable.ic_schedule),
                            contentDescription = stringResource(R.string.time)
                        )
                    }
                )

                val isStudiesSelected = pagerState.currentPage == 1
                NavigationBarItem(
                    selected = isStudiesSelected,
                    onClick = {
                        if (!isStudiesSelected) {
                            coroutineScope.launch {
                                pagerState.scrollToPage(1)
                            }
                        } else {
                            coroutineScope.launch {
                                studiesScrollState.animateScrollTo(0)
                            }
                        }
                    },
                    label = {
                        Text(stringResource(R.string.bible_studies_short))
                    },
                    icon = {
                        BadgedBox(badge = {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = !state.monthlyInformation.dismissedBibleStudiesHint &&
                                    state.bibleStudies.isNotEmpty() &&
                                    state.bibleStudies.all { !it.checked },
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Badge { Text("!") }
                            }
                        }) {
                            Icon(
                                painterResource(R.drawable.ic_group),
                                contentDescription = stringResource(R.string.bible_studies_short)
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            HorizontalPager(state = pagerState) { pageIndex ->
                val page = PagerPage.entries.first { it.ordinal == pageIndex }
                when (page) {
                    PagerPage.Time -> TimePage(state, dispatch, timeScrollState)
                    PagerPage.BibleStudies -> BibleStudiesPage(state, dispatch, studiesScrollState)
                }
            }
        }
    }
}
