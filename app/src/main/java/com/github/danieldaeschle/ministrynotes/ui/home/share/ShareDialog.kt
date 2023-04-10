package com.github.danieldaeschle.ministrynotes.ui.home.share

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.lib.AlertDialog
import com.github.danieldaeschle.ministrynotes.lib.shareBitmap
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.HomeViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun ShareDialog(
    isOpen: Boolean,
    onClose: () -> Unit,
    homeViewModel: HomeViewModel = koinViewModel()
) {
    val context = LocalContext.current
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
    val bitmap by remember(fieldServiceReportWithComments) {
        derivedStateOf {
            fieldServiceReportWithComments?.let {
                context.createFieldServiceReportImage(it)
            }
        }
    }

    AlertDialog(isOpen, onClose = {
        comments = ""
        onClose()
    }, paddingValues = PaddingValues(top = 24.dp), title = {
        Text(stringResource(R.string.share_field_service_report))
    }) {
        Column {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f, false)
            ) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    bitmap?.let {
                        Image(
                            it.asImageBitmap(),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .fillMaxWidth(),
                            contentDescription = null // TODO: contentDescription
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = comments,
                        onValueChange = { comments = it },
                        placeholder = {
                            Text(stringResource(R.string.field_service_report_comments_placeholder))
                        },
                        label = { Text(stringResource(R.string.comments)) })
                }
            }

            Divider(Modifier.fillMaxWidth())

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        bitmap?.let {
                            context.shareBitmap(it)
                        }
                    }
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.share_as_image),
                    color = MaterialTheme.colorScheme.primary
                )
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
                Text(
                    stringResource(R.string.share_as_text),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun Context.shareFieldServiceReport(report: FieldServiceReport) {
    val text = """${getString(R.string.field_service_report).uppercase()}
        
        |${getString(R.string.name_colon)} ${report.name}
        |${getString(R.string.month_colon)} ${report.month}
        
        |${getString(R.string.placements_long_colon)} ${report.placements}
        |${getString(R.string.video_showings_colon)} ${report.videoShowings}
        |${getString(R.string.hours_colon)} ${report.hours}
        |${getString(R.string.return_visits_colon)} ${report.returnVisits}
        |${getString(R.string.bible_studies_long_colon)} ${report.bibleStudies}
        
        |${getString(R.string.comments_colon)}
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
