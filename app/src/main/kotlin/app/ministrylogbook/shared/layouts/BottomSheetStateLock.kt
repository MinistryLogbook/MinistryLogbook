package app.ministrylogbook.shared.layouts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class UnlockRequestState {
    Initial,
    Requested,
    Declined,
    Approved
}

class BottomSheetStateLock {
    private var _isLocked: Boolean = false
    private val _unlockRequest = MutableStateFlow(UnlockRequestState.Initial)

    val unlockRequest = _unlockRequest.asStateFlow()

    val isLocked: Boolean
        get() = _isLocked

    fun lock() {
        _isLocked = true
    }

    fun unlock() {
        _isLocked = false
    }

    fun approveRequest() {
        _isLocked = false
        _unlockRequest.value = UnlockRequestState.Approved
    }

    fun declineRequest() {
        _unlockRequest.value = UnlockRequestState.Declined
    }

    fun requestUnlocked(): Boolean {
        if (isLocked) {
            _unlockRequest.value = UnlockRequestState.Requested
            return false
        }
        return true
    }
}

@Composable
fun rememberBottomSheetStateLock(): BottomSheetStateLock {
    return remember { BottomSheetStateLock() }
}

val LocalBottomSheetStateLock = compositionLocalOf { BottomSheetStateLock() }
