package app.ministrylogbook.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart

class DatabaseChangeNotifier {
    private val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val databaseChanges = changes.onStart { emit(Unit) }

    fun notifyDatabaseChanged() {
        changes.tryEmit(Unit)
    }
}
