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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ministrylogbook.R
import app.ministrylogbook.ui.intro.viewmodel.IntroState
import app.ministrylogbook.ui.settings.GoalTextField

@Composable
fun GoalPage(state: IntroState, onChange: (goal: Int?) -> Unit, scrollState: ScrollState = rememberScrollState()) {
    var goal by remember { mutableStateOf(state.goal) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(32.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.intro_goal_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 40.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            stringResource(R.string.intro_goal_description),
            color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        GoalTextField(value = goal, onChange = {
            goal = it
            onChange(it)
        }, requestFocus = false)
    }
}
