package app.ministrylogbook.ui.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ministrylogbook.R
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.shared.Toolbar
import app.ministrylogbook.ui.shared.ToolbarAction

@Composable
fun WelcomePage() {
    val navController = LocalAppNavController.current

    Surface(Modifier.fillMaxSize()) {
        Column {
            Toolbar {
                Spacer(modifier = Modifier.weight(1f))
                ToolbarAction {
                    Icon(painterResource(R.drawable.ic_language), contentDescription = null)
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painterResource(R.drawable.logo),
                    modifier = Modifier.size(160.dp),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Welcome to\nMinistry Logbook",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Ministry tracking for Jehovah's Witnesses",
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = {
                    navController.navigateToSetup()
                }) {
                    Text("Start now")
                }
            }
        }
    }
}