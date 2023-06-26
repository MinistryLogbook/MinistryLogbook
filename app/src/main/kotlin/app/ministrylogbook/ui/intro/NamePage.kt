package app.ministrylogbook.ui.intro

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ministrylogbook.R
import app.ministrylogbook.ui.intro.viewmodel.IntroState
import app.ministrylogbook.ui.settings.NameTextField

@Composable
fun NamePage(
    state: IntroState,
    onChange: (value: String) -> Unit,
    onDone: () -> Unit = {},
    scrollState: ScrollState = rememberScrollState()
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(32.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            stringResource(R.string.whats_your_name),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            stringResource(R.string.intro_name_description),
            color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        NameTextField(
            state.name ?: "",
            onChange = { onChange(it) },
            autoFocus = state.name?.isEmpty() ?: false,
            onDone = onDone
        )
    }
}