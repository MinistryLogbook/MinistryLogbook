package app.ministrylogbook.ui.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.R
import app.ministrylogbook.shared.layouts.SegmentedButtons
import app.ministrylogbook.shared.layouts.ToolbarLayout
import app.ministrylogbook.shared.utilities.shareBitmap
import app.ministrylogbook.shared.utilities.shareText
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.home.navigateToHome
import app.ministrylogbook.ui.share.viewmodel.ShareViewModel
import app.ministrylogbook.ui.shared.ToolbarAction
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import org.koin.androidx.compose.koinViewModel

enum class ShareAs {
    Text, Image
}

@OptIn(FlowPreview::class)
@Composable
fun SharePage(viewModel: ShareViewModel = koinViewModel()) {
    val context = LocalContext.current
    val navController = LocalAppNavController.current
    val scrollState = rememberScrollState()
    var selectedShareAs by remember { mutableStateOf(ShareAs.Image) }
    val fieldServiceReport by viewModel.fieldServiceReport.collectAsStateWithLifecycle()
    val initialComments by viewModel.initialComments.collectAsStateWithLifecycle()
    var comments by remember(initialComments) { mutableStateOf(initialComments) }
    val commentsFlow = snapshotFlow { comments }
    val fieldServiceReportWithComments by remember(fieldServiceReport, comments) {
        derivedStateOf {
            val concatenatedComments = if (fieldServiceReport.comments.isBlank()) {
                comments
            } else if (comments.isNotBlank()) {
                "${fieldServiceReport.comments}\n$comments"
            } else {
                fieldServiceReport.comments
            }
            fieldServiceReport.copy(comments = concatenatedComments)
        }
    }
    val fieldServiceReportText by remember(fieldServiceReportWithComments) {
        derivedStateOf {
            fieldServiceReportWithComments.toText(context)
        }
    }
    val bitmap by remember(fieldServiceReportWithComments) {
        derivedStateOf {
            context.createFieldServiceReportImage(fieldServiceReportWithComments)
        }
    }

    val handleBack: () -> Unit = {
        navController.navigateToHome()
    }

    val handleShare: () -> Unit = {
        if (selectedShareAs == ShareAs.Text) {
            context.shareText(
                fieldServiceReportText,
                context.getString(R.string.field_service_report_subject, fieldServiceReportWithComments.month)
            )
        } else if (selectedShareAs == ShareAs.Image) {
            bitmap.let {
                val reportShareFileName = context.getString(
                    R.string.report_share_file_name,
                    fieldServiceReport.month,
                    viewModel.month.year
                )

                val fieldServiceReportSubject =
                    context.getString(R.string.field_service_report_subject, fieldServiceReport.month)
                context.shareBitmap(
                    it,
                    reportShareFileName,
                    subject = fieldServiceReportSubject
                )
            }
        }
    }

    LaunchedEffect(commentsFlow) {
        commentsFlow.debounce(250).collectLatest {
            viewModel.updateComments(it)
        }
    }

    ToolbarLayout(elevation = scrollState.value > 0, toolbarContent = {
        ToolbarAction(onClick = handleBack) {
            Icon(
                painterResource(R.drawable.ic_arrow_back),
                contentDescription = null // TODO: contentDescription
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(R.string.share),
            fontSize = MaterialTheme.typography.titleLarge.fontSize
        )
    }) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Box(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        bottom = maxOf(
                            WindowInsets.ime
                                .asPaddingValues()
                                .calculateBottomPadding(), 64.dp
                        )
                    )
            ) {
                Column(Modifier.padding(vertical = 20.dp, horizontal = 20.dp)) {
                    SegmentedButtons {
                        it.SegmentedButton(onClick = { selectedShareAs = ShareAs.Image }) {
                            Text(stringResource(R.string.image))
                        }
                        it.SegmentedButton(onClick = { selectedShareAs = ShareAs.Text }) {
                            Text(stringResource(R.string.text))
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Box {
                        when (selectedShareAs) {
                            ShareAs.Image -> {
                                Image(
                                    bitmap.asImageBitmap(),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .fillMaxSize(),
                                    contentScale = ContentScale.FillWidth,
                                    contentDescription = null // TODO: contentDescription
                                )
                            }

                            ShareAs.Text -> {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(16.dp)
                                ) {
                                    Text(fieldServiceReportText, fontSize = 13.sp, lineHeight = 18.sp)
                                }
                            }
                        }

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            val containerColor = if (selectedShareAs == ShareAs.Image) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                            val textColor = if (selectedShareAs == ShareAs.Image) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            }
                            Box(
                                Modifier
                                    .clip(CircleShape)
                                    .background(containerColor)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(stringResource(R.string.preview), fontSize = 11.sp, color = textColor)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = comments,
                        onValueChange = { comments = it },
                        placeholder = {
                            Text(
                                stringResource(
                                    R.string.field_service_report_comments_placeholder
                                )
                            )
                        },
                        label = { Text(stringResource(R.string.comments)) }
                    )
                }
            }

            Surface(tonalElevation = 4.dp, shadowElevation = 4.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(64.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = handleShare) {
                        Text(stringResource(R.string.share))
                    }
                }
            }
        }
    }
}
