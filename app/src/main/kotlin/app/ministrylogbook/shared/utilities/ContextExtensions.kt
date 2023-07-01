package app.ministrylogbook.shared.utilities

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

val Context.activity: Activity?
    get() = findActivity()

private fun Context.findActivity() = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity
    else -> null
}
