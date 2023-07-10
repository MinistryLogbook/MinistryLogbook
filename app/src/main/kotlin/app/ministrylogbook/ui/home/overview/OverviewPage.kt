package app.ministrylogbook.ui.home.overview

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.R
import app.ministrylogbook.shared.layouts.ExtendableFloatingActionButton
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.home.navigateToEntryDetails
import app.ministrylogbook.ui.home.viewmodel.OverviewViewModel
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OverviewPage(onScroll: (position: Int) -> Unit = {}, viewModel: OverviewViewModel = koinViewModel()) {
    val scrollState = rememberScrollState()
    var fabExtended by remember { mutableStateOf(true) }
    val navController = LocalAppNavController.current
    val entries by viewModel.entries.collectAsStateWithLifecycle()

    LaunchedEffect(scrollState) {
        var prev = 0
        snapshotFlow { scrollState.value }.collect {
            onScroll(it)
            fabExtended = it <= prev
            prev = it
        }
    }

    Scaffold(contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp), floatingActionButton = {
        ExtendableFloatingActionButton(onClick = {
            navController.navigateToEntryDetails(viewModel.month)
        }, extended = fabExtended, icon = {
            Icon(
                painterResource(R.drawable.ic_add),
                contentDescription = null // TODO: contentDescription
            )
        }, text = {
            Text(stringResource(R.string.create_entry))
        })
    }) {
        Column(modifier = Modifier.verticalScroll(scrollState).padding(bottom = 82.dp, top = 24.dp)) {
            DetailsSection()

            YearlyProgress()
            TransferHint()
            RestHint()

            if (entries.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onBackground.copy(0.05f))
            }

            HistorySection()
        }
    }
}
