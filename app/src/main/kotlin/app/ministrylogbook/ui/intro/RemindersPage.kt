package app.ministrylogbook.ui.intro

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ministrylogbook.ui.settings.SendReportReminderSetting

@Composable
fun RemindersPage(
    reminders: Boolean,
    onChange: (Boolean) -> Unit = {},
    scrollState: ScrollState = rememberScrollState()
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(32.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Lastly, do you want to get reminders?",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 40.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Never forget to send your Field Service Report\nagain.",
            color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        SendReportReminderSetting(
            sendReportReminder = reminders,
            onChange = onChange,
            paddingValues = PaddingValues(vertical = 12.dp)
        )
    }
}
