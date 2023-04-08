package com.github.danieldaeschle.ministrynotes.ui.home.share

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.lib.AlertDialog
import com.github.danieldaeschle.ministrynotes.lib.shareBitmap
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.HomeViewModel
import dev.shreyaspatil.capturable.Capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import org.koin.androidx.compose.koinViewModel


@Composable
fun ShareDialog(
    isOpen: Boolean,
    onClose: () -> Unit,
    homeViewModel: HomeViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val navController = LocalAppNavController.current
    val captureController = rememberCaptureController()
    val fieldServiceReport by homeViewModel.fieldServiceReport.collectAsState(null)
    var comments by remember { mutableStateOf("") }
    val fieldServiceReportWithComments = fieldServiceReport?.run {
        val concatenatedComments = if (this.comments.isBlank()) {
            comments
        } else if (comments.isNotBlank()) {
            "${this.comments}\n$comments"
        } else {
            this.comments
        }
        copy(comments = concatenatedComments)
    }

    AlertDialog(isOpen, onClose = {
        comments = ""
        onClose()
    }, paddingValues = PaddingValues(top = 24.dp), title = {
        Text("Share Field Service Report")
    }) {
        Column {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f, false)
            ) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Capturable(
                        controller = captureController,
                        onCaptured = { bitmap, error ->
                            if (bitmap != null) {
                                navController.popBackStack()
                                context.shareBitmap(bitmap.asAndroidBitmap())
                            } else if (error != null) {
                                throw error
                            }
                        }
                    ) {
                        fieldServiceReportWithComments?.let {
                            FieldServiceReportImage(it)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = comments,
                        onValueChange = { comments = it },
                        placeholder = {
                            Text("E.g., sickness, LDC, pioneer school etc.")
                        },
                        label = {
                            Text("Comments")
                        })
                }
            }

            Divider(Modifier.fillMaxWidth())

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { captureController.capture() }
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Share as image", color = MaterialTheme.colorScheme.primary)
            }

            Divider(Modifier.fillMaxWidth())

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        fieldServiceReportWithComments?.let {
                            context.shareFieldServiceReport(it)
                        }
                    }
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Share as text", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

fun Context.shareFieldServiceReport(report: FieldServiceReport) {
    val text = """FIELD SERVICE REPORT
        
        |Name: ${report.name}
        |Month: ${report.month}
        
        |Placements (Printed and Electronic): ${report.placements}
        |Video showings: ${report.videoShowings}
        |Hours: ${report.hours}
        |Return visits: ${report.returnVisits}
        |Number of Different Bible Studies Conducted: ${report.bibleStudies}
        
        |Comments:
        |${report.comments}
    """.trimMargin()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}
