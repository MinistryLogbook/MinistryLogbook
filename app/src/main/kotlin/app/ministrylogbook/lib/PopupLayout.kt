package app.ministrylogbook.lib

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.FloatingWindow
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.NavigatorState
import androidx.navigation.get
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

@Composable
fun PopupLayout(popupNavigator: PopupNavigator, content: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()
    val visibility by popupNavigator.popupState.visibility.collectAsStateWithLifecycle()
    val visibleBackStackEntry by popupNavigator.currentVisibleBackStackEntryAsState()
    val backStackEntry by popupNavigator.currentBackStackEntryAsState()
    val animVisibleState = remember { MutableTransitionState(false) }

    LaunchedEffect(popupNavigator, backStackEntry) {
        popupNavigator.popupState.visibility.drop(1).collect { visibility ->
            val isVisible = visibility == PopupVisibility.Visible
            animVisibleState.targetState = visibility == PopupVisibility.Visible
            if (!isVisible) {
                if (backStackEntry != null) {
                    scope.launch {
                        popupNavigator.popBackStack(backStackEntry!!, false)
                    }
                }
            }
        }
    }

    LaunchedEffect(visibleBackStackEntry) {
        if (visibleBackStackEntry != null) {
            popupNavigator.popupState.show()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        content()

        Scrim(
            color = MaterialTheme.colorScheme.surface.copy(0.5f),
            onDismiss = {
                scope.launch {
                    popupNavigator.popupState.hide()
                }
            },
            visible = visibility == PopupVisibility.Visible
        )

        if (visibleBackStackEntry != null && !animVisibleState.targetState && !animVisibleState.currentState) {
            popupNavigator.markTransitionComplete(visibleBackStackEntry!!)
        }

        Box(
            Modifier
                .fillMaxSize()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
        ) {
            AnimatedVisibility(
                visibleState = animVisibleState,
                enter = scaleIn(
                    tween(durationMillis = 200),
                    transformOrigin = TransformOrigin(1f, 0f)
                ),
                exit = fadeOut(tween(durationMillis = 100))
            ) {
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    tonalElevation = 8.dp
                ) {
                    Column {
                        (visibleBackStackEntry?.destination as PopupNavigator.Destination?)?.let {
                            it.content(this, visibleBackStackEntry!!)
                        }
                    }
                }
            }
        }
    }
}

enum class PopupVisibility {
    Visible,
    Hidden
}

class PopupState(initialValue: PopupVisibility = PopupVisibility.Hidden) {
    private val _visibility = MutableStateFlow(initialValue)

    val visibility = this._visibility.asStateFlow()

    suspend fun show() {
        _visibility.emit(PopupVisibility.Visible)
    }

    suspend fun hide() {
        _visibility.emit(PopupVisibility.Hidden)
    }

    companion object {
        fun Saver(): Saver<PopupState, *> =
            Saver(save = { it.visibility.value }, restore = { PopupState(it) })
    }
}

@Composable
fun rememberPopupState(): PopupState {
    return rememberSaveable(saver = PopupState.Saver()) { PopupState() }
}

@Composable
fun rememberPopupNavigator(): PopupNavigator {
    val state = rememberPopupState()
    return remember { PopupNavigator(state) }
}

@Navigator.Name("PopupNavigator")
class PopupNavigator(val popupState: PopupState) : Navigator<PopupNavigator.Destination>() {

    private var attached by mutableStateOf(false)

    private val backStack: StateFlow<List<NavBackStackEntry>>
        get() = if (attached) {
            state.backStack
        } else {
            MutableStateFlow(emptyList())
        }

    private val transitionsInProgress: StateFlow<Set<NavBackStackEntry>>
        get() = if (attached) {
            state.transitionsInProgress
        } else {
            MutableStateFlow(emptySet())
        }

    @Composable
    fun currentVisibleBackStackEntryAsState(): State<NavBackStackEntry?> {
        return produceState<NavBackStackEntry?>(null, backStack) {
            backStack.combine(transitionsInProgress) { backStack, transitionsInProgress ->
                val currentTransitionInProgress = transitionsInProgress.lastOrNull()
                if (currentTransitionInProgress != null) {
                    return@combine currentTransitionInProgress
                }
                return@combine backStack.lastOrNull()
            }.collectLatest {
                value = it
            }
        }
    }

    @Composable
    fun currentBackStackEntryAsState(): State<NavBackStackEntry?> {
        return produceState<NavBackStackEntry?>(null, backStack) {
            backStack.transform {
                try {
                    popupState.hide()
                } finally {
                    emit(it.lastOrNull())
                }
            }.collectLatest {
                value = it
            }
        }
    }

    override fun createDestination() = Destination(navigator = this, content = {})

    override fun onAttach(state: NavigatorState) {
        super.onAttach(state)
        attached = true
    }

    fun markTransitionComplete(popUpTo: NavBackStackEntry) {
        state.markTransitionComplete(popUpTo)
    }

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        state.popWithTransition(popUpTo, savedState)
    }

    override fun navigate(
        entries: List<NavBackStackEntry>,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ) {
        entries.forEach { entry ->
            state.push(entry)
        }
    }

    @NavDestination.ClassType(Composable::class)
    class Destination(
        navigator: PopupNavigator,
        internal val content: @Composable ColumnScope.(NavBackStackEntry) -> Unit
    ) : NavDestination(navigator), FloatingWindow
}

fun NavGraphBuilder.popup(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable ColumnScope.(backstackEntry: NavBackStackEntry) -> Unit
) {
    addDestination(
        PopupNavigator.Destination(
            provider[PopupNavigator::class],
            content
        ).apply {
            this.route = route
            arguments.forEach { (argumentName, argument) ->
                addArgument(argumentName, argument)
            }
            deepLinks.forEach { deepLink ->
                addDeepLink(deepLink)
            }
        }
    )
}
