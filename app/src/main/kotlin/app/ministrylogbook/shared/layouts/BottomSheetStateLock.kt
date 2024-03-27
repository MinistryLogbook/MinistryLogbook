package app.ministrylogbook.shared.layouts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BottomSheetStateLock {
    private var _isLocked: Boolean = false
    private val _unlockRequest = MutableStateFlow(false)

    val unlockRequest = _unlockRequest.asStateFlow()

    val isLocked: Boolean
        get() = _isLocked

    fun lock() {
        _isLocked = true
    }

    fun unlock() {
        _isLocked = false
        _unlockRequest.value = false
    }

    fun declineRequest() {
        _unlockRequest.value = false
    }

    fun requestUnlocked(): Boolean {
        if (isLocked) {
            _unlockRequest.value = true
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
