package app.ministrylogbook.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.R
import app.ministrylogbook.lib.ExtendableFloatingActionButton
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.home.detailssection.DetailsSection
import app.ministrylogbook.ui.home.historysection.HistorySection
import app.ministrylogbook.ui.home.viewmodel.HomeViewModel
import app.ministrylogbook.ui.shared.Toolbar
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomePage(viewModel: HomeViewModel = koinViewModel()) {
    var fabExtended by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    val navController = LocalAppNavController.current
    val entries by viewModel.entries.collectAsStateWithLifecycle()

    LaunchedEffect(scrollState) {
        var prev = 0
        snapshotFlow { scrollState.value }.collect {
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
            Text(stringResource(R.string.add_to_report))
        })
    }) {
        Box {
            Toolbar(
                modifier = Modifier.zIndex(1f),
                elevation = if (scrollState.value > 0) 4.dp else 0.dp
            ) {
                ToolbarMonthSelect()
                Spacer(Modifier.weight(1f))
                ToolbarActions()
            }
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(bottom = 82.dp)
            ) {
                Box(modifier = Modifier.statusBarsPadding()) {
                    Spacer(modifier = Modifier.height(56.dp))
                }

                Spacer(Modifier.height(16.dp))

                Box(Modifier.padding(horizontal = 16.dp)) {
                    DetailsSection()
                }

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
}
