package app.ministrylogbook.shared.utilities

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent

val Context.activity: Activity?
    get() = findActivity()

private fun Context.findActivity() = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity
    else -> null
}

fun Context.shareText(text: String, subject: String? = null) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        subject?.let {
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

fun Context.restartApp(restartIntent: Intent) {
    restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
    startActivity(restartIntent)
    if (this is Activity) {
        finish()
    }
}
