package app.ministrylogbook.ui.settings.license

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ministrylogbook.R
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.settings.BaseSettingsPage
import app.ministrylogbook.ui.settings.navigateToLicenseDetail
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.util.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LicensesPage() {
    val context = LocalContext.current
    val navController = LocalAppNavController.current
    val libs by produceState<Libs?>(null) {
        value = withContext(Dispatchers.IO) {
            Libs.Builder().withContext(context).build()
        }
    }
    val lazyListState = rememberLazyListState()
    val libraries by remember(libs) {
        derivedStateOf { libs?.libraries ?: listOf() }
    }

    BaseSettingsPage(
        title = stringResource(R.string.open_source_licenses),
        toolbarElevation = lazyListState.canScrollBackward
    ) {
        LazyColumn(state = lazyListState) {
            items(libraries) { library ->
                Library(library) {
                    navController.navigateToLicenseDetail(library.uniqueId)
                }
            }
        }
    }
}

@Composable
internal fun Library(library: Library, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = library.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp
            )
        }
    }
}
