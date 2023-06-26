package app.ministrylogbook.shared

import kotlinx.coroutines.flow.StateFlow

interface IntentViewModel<S, I> {
    val state: StateFlow<S>
    fun dispatch(intent: I)
}