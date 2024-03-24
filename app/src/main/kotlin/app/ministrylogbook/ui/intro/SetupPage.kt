package app.ministrylogbook.ui.intro

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.ministrylogbook.R
import app.ministrylogbook.data.Role
import app.ministrylogbook.shared.layouts.DeferredAnimatedVisibility
import app.ministrylogbook.shared.layouts.ToolbarLayout
import app.ministrylogbook.shared.layouts.progress.LinearProgressIndicator
import app.ministrylogbook.shared.layouts.progress.Progress
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.home.navigateToHome
import app.ministrylogbook.ui.intro.viewmodel.IntroIntent
import app.ministrylogbook.ui.intro.viewmodel.IntroViewModel
import app.ministrylogbook.ui.shared.ToolbarAction
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

val LocalIntroNavController = compositionLocalOf<NavHostController> { error("LocalIntroNavController error") }

@Composable
fun SetupPage() {
    val viewModel = koinViewModel<IntroViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navController = LocalAppNavController.current
    val introNavController = rememberNavController()
    val currentBackStackEntry by introNavController.currentBackStackEntryAsState()
    val isLastPage by remember {
        derivedStateOf { currentBackStackEntry?.destination?.route == InnerIntroGraph.Reminders.route }
    }
    val coroutineScope = rememberCoroutineScope()
    var tempName by remember(state.name) {
        mutableStateOf(state.name ?: "")
    }
    var tempGoal by remember(state.goal) {
        mutableStateOf(state.goal)
    }
    val scrollState = rememberScrollState()
    val progress by remember {
        derivedStateOf {
            when (currentBackStackEntry?.destination?.route) {
                InnerIntroGraph.Name.route -> 0.2f
                InnerIntroGraph.Role.route -> 0.6f
                InnerIntroGraph.Goal.route -> 0.8f
                InnerIntroGraph.Reminders.route -> 1f
                else -> 0f
            }
        }
    }
    val isNextButtonEnabled by remember(tempName) {
        derivedStateOf {
            when (currentBackStackEntry?.destination?.route) {
                InnerIntroGraph.Name.route -> tempName.isNotBlank()
                InnerIntroGraph.Role.route -> {
                    if (state.role == Role.RegularPioneer || state.role == Role.SpecialPioneer) {
                        state.pioneerSince != null
                    } else {
                        true
                    }
                }

                else -> true
            }
        }
    }

    val navigateToNext = {
        coroutineScope.launch {
            scrollState.scrollTo(0)
        }
        hideKeyboard(context as Activity)
        when (introNavController.currentBackStackEntry?.destination?.route) {
            InnerIntroGraph.Name.route -> {
                viewModel.dispatch(IntroIntent.NameChange(tempName))
                introNavController.navigate(InnerIntroGraph.Role.route)
            }
            InnerIntroGraph.Role.route -> {
                if (state.role == Role.Publisher) {
                    introNavController.navigate(InnerIntroGraph.Goal.route)
                } else {
                    introNavController.navigate(InnerIntroGraph.Reminders.route)
                }
            }
            InnerIntroGraph.Goal.route -> {
                viewModel.dispatch(IntroIntent.GoalChange(tempGoal))
                introNavController.navigate(InnerIntroGraph.Reminders.route)
            }
            else -> {}
        }
    }

    val navigateBack = {
        coroutineScope.launch {
            scrollState.scrollTo(0)
        }
        hideKeyboard(context as Activity)
        when (introNavController.currentBackStackEntry?.destination?.route) {
            InnerIntroGraph.Role.route -> introNavController.navigateUp()
            InnerIntroGraph.Reminders.route -> introNavController.navigateUp()
            InnerIntroGraph.Goal.route -> introNavController.navigateUp()
            else -> navController.navigateUp()
        }
    }

    Box(Modifier.imePadding().navigationBarsPadding()) {
        ToolbarLayout(
            elevation = scrollState.canScrollBackward,
            toolbarContent = {
                ToolbarAction(onClick = { navigateBack() }) {
                    Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = null)
                }

                Box(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LinearProgressIndicator(
                        progresses = listOf(
                            Progress(progress, color = MaterialTheme.colorScheme.primary)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        strokeCap = StrokeCap.Round
                    )
                }

                Spacer(Modifier.size(40.dp))
            }
        ) {
            Box(Modifier.fillMaxSize()) {
                CompositionLocalProvider(LocalIntroNavController provides introNavController) {
                    NavHost(
                        navController = introNavController,
                        startDestination = InnerIntroGraph.Name.route
                    ) {
                        composable(InnerIntroGraph.Name.route) {
                            NamePage(state, onChange = {
                                tempName = it
                            }, onDone = {
                                navigateToNext()
                            }, scrollState = scrollState)
                        }
                        composable(InnerIntroGraph.Role.route) {
                            RolePage(
                                state,
                                onChange = {
                                    viewModel.dispatch(IntroIntent.RoleChange(it))
                                },
                                onPioneerSinceSet = {
                                    viewModel.dispatch(IntroIntent.PioneerSinceSet(it))
                                },
                                scrollState = scrollState
                            )
                        }
                        composable(InnerIntroGraph.Goal.route) {
                            GoalPage(state, onChange = {
                                tempGoal = it
                            }, scrollState = scrollState)
                        }
                        composable(InnerIntroGraph.Reminders.route) {
                            RemindersPage(reminders = state.reminders, onChange = {
                                viewModel.dispatch(IntroIntent.RemindersToggle(it))
                            }, scrollState = scrollState)
                        }
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(32.dp)
                        .height(80.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = if (isLastPage) Arrangement.Center else Arrangement.End
                ) {
                    if (isLastPage) {
                        DeferredAnimatedVisibility(
                            1000,
                            transition = slideInVertically(tween(800)) { it / 3 } + fadeIn(tween(800))
                        ) {
                            Button(onClick = {
                                viewModel.dispatch(IntroIntent.Ready)
                                introNavController.clearBackStack(introNavController.graph.findStartDestination().id)
                                navController.navigateToHome()
                            }) {
                                Text(stringResource(R.string.ready))
                            }
                        }
                    } else {
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .size(64.dp)
                        ) {
                            Button(
                                onClick = { navigateToNext() },
                                modifier = Modifier.fillMaxSize(),
                                enabled = isNextButtonEnabled,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = activity.currentFocus ?: View(activity)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}
