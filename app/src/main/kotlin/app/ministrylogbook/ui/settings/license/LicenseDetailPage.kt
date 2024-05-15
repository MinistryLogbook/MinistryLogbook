package app.ministrylogbook.ui.settings.license

import android.graphics.Typeface
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import app.ministrylogbook.ui.settings.BaseSettingsPage
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.aboutlibraries.util.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LicenseDetailPage(id: String) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val library by produceState<Library?>(null) {
        value = withContext(Dispatchers.IO) {
            Libs.Builder().withContext(context).build().libraries.find { it.uniqueId == id }
        }
    }
    val license by remember(library) {
        derivedStateOf { library?.licenses?.firstOrNull() }
    }

    BaseSettingsPage(library?.name.orEmpty(), toolbarElevation = scrollState.canScrollBackward) {
        Box(
            Modifier
                .verticalScroll(scrollState)
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            HtmlText(html = license?.htmlReadyLicenseContent.orEmpty())
        }
    }
}

val License.htmlReadyLicenseContent: String?
    get() = licenseContent?.replace("\n", "<br />")

@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context -> TextView(context) },
        update = {
            it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
            it.typeface = Typeface.MONOSPACE
        }
    )
}
