package com.github.danieldaeschle.ministrynotes.lib

import androidx.compose.animation.core.AnimationSpec
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
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

@Composable
fun PopupLayout(
    popupState: PopupState,
    popupContent: @Composable (ColumnScope.() -> Unit),
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val isOpen = popupState.isOpen.collectAsState()

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
                    popupState.hide()
                }
            },
            visible = isOpen.value,
        )

        Box(
            Modifier
                .fillMaxSize()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)), tonalElevation = 8.dp
            ) {
                Column(content = popupContent)
            }
        }
    }
}

@Composable
fun PopupLayout(
    popupNavigator: PopupNavigator,
    content: @Composable () -> Unit,
) {
    PopupLayout(
        popupState = popupNavigator.popupState,
        popupContent = popupNavigator.content,
        content = content,
    )
}

class PopupState(initialValue: Boolean = false) {
    private val _isOpen = MutableStateFlow(initialValue)

    val isOpen = this._isOpen.asStateFlow()

    suspend fun show() {
        _isOpen.emit(true)
    }

    suspend fun hide() {
        _isOpen.emit(false)
    }

    companion object {
        fun Saver(): Saver<PopupState, *> =
            Saver(save = { it.isOpen.value }, restore = { PopupState(it) })
    }
}

@Composable
fun rememberPopupState(): PopupState {
    return rememberSaveable(saver = PopupState.Saver()) {
        PopupState()
    }
}

@Composable
fun rememberPopupNavigator(
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec
): PopupNavigator {
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

    override fun createDestination() = Destination(navigator = this, content = {})

    override fun onAttach(state: NavigatorState) {
        super.onAttach(state)
        attached = true
    }

    val content: @Composable ColumnScope.() -> Unit = {
        val retainedEntry by produceState<NavBackStackEntry?>(
            initialValue = null, key1 = backStack
        ) {
            backStack.transform { backStackEntries ->
                try {
                    popupState.hide()
                } finally {
                    emit(backStackEntries.lastOrNull())
                }
            }.collectLatest { value = it }
        }

        if (retainedEntry != null) {
            val backStackEntry = retainedEntry!!
            LaunchedEffect(backStackEntry) {
                popupState.show()
            }

            LaunchedEffect(popupState, backStackEntry) {
                popupState.isOpen
                    // distinctUntilChanged emits the initial value which we don't need
                    .drop(1).collect { visible ->
                        if (!visible) {
                            state.pop(popUpTo = backStackEntry, saveState = false)
                        }
                    }
            }

            val destinationContent = (retainedEntry!!.destination as Destination).content
            destinationContent(retainedEntry!!)
        }
    }

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        state.popWithTransition(popUpTo, savedState)
    }

    override fun navigate(
        entries: List<NavBackStackEntry>, navOptions: NavOptions?, navigatorExtras: Extras?
    ) {
        entries.forEach { entry ->
            state.pushWithTransition(entry)
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
    addDestination(PopupNavigator.Destination(
        provider[PopupNavigator::class], content
    ).apply {
        this.route = route
        arguments.forEach { (argumentName, argument) ->
            addArgument(argumentName, argument)
        }
        deepLinks.forEach { deepLink ->
            addDeepLink(deepLink)
        }
    })
}