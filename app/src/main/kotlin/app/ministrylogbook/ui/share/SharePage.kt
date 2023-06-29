package app.ministrylogbook.ui.share

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.R
import app.ministrylogbook.shared.layouts.ToolbarLayout
import app.ministrylogbook.shared.utilities.shareBitmap
import app.ministrylogbook.ui.LocalAppNavController
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
    var selectedShareAs by remember { mutableStateOf<ShareAs?>(null) }
    val fieldServiceReport by viewModel.fieldServiceReport.collectAsStateWithLifecycle()
    val fieldServiceReportSubject = stringResource(R.string.field_service_report_subject, fieldServiceReport.month)
    val reportShareFileName = stringResource(
        R.string.report_share_file_name,
        fieldServiceReport.month,
        viewModel.month.year
    )
    val initialComments by viewModel.comments.collectAsStateWithLifecycle()
    var comments by remember(initialComments) { mutableStateOf(initialComments) }
    val commentsFlow = snapshotFlow { comments }
    val fieldServiceReportWithComments = fieldServiceReport.run {
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
            fieldServiceReportWithComments.let {
                context.createFieldServiceReportImage(it)
            }
        }
    }

    val handleBack: () -> Unit = {
        navController.popBackStack()
    }

    val handleShare: () -> Unit = {
        if (selectedShareAs == ShareAs.Text) {
            fieldServiceReportWithComments.let {
                context.shareFieldServiceReport(it)
            }
        } else if (selectedShareAs == ShareAs.Image) {
            bitmap.let {
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
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .verticalScroll(scrollState)
                    .weight(1f)
            ) {
                Column(Modifier.padding(vertical = 10.dp, horizontal = 20.dp)) {
                    Image(
                        bitmap.asImageBitmap(),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .fillMaxSize(),
                        contentScale = ContentScale.FillWidth,
                        contentDescription = null // TODO: contentDescription
                    )

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

                    Spacer(Modifier.height(32.dp))

                    Text(stringResource(R.string.share_as))

                    Spacer(Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth()) {
                        val outlinedColors = ButtonDefaults.outlinedButtonColors()
                        val selectedColors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primary.copy(
                                    0.2f
                                )
                            )
                        val enterAnimation =
                            expandHorizontally(tween(durationMillis = 100)) + fadeIn(
                                tween(delayMillis = 120)
                            )
                        val exitAnimation =
                            shrinkHorizontally(
                                tween(durationMillis = 100, delayMillis = 120)
                            ) + fadeOut(tween())

                        OutlinedButton(
                            modifier = Modifier
                                .weight(1f)
                                .offset(0.5.dp, 0.dp),
                            onClick = { selectedShareAs = ShareAs.Text },
                            shape = CircleShape.copy(
                                topEnd = CornerSize(0.dp),
                                bottomEnd = CornerSize(0.dp)
                            ),
                            colors = if (selectedShareAs == ShareAs.Text) {
                                selectedColors
                            } else {
                                outlinedColors
                            }
                        ) {
                            AnimatedVisibility(
                                visible = selectedShareAs == ShareAs.Text,
                                enter = enterAnimation,
                                exit = exitAnimation
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_check),
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(stringResource(R.string.text))
                        }
                        OutlinedButton(
                            modifier = Modifier
                                .weight(1f)
                                .offset((-0.5).dp, 0.dp),
                            onClick = { selectedShareAs = ShareAs.Image },
                            shape = CircleShape.copy(
                                topStart = CornerSize(0.dp),
                                bottomStart = CornerSize(0.dp)
                            ),
                            colors = if (selectedShareAs == ShareAs.Image) {
                                selectedColors
                            } else {
                                outlinedColors
                            }
                        ) {
                            AnimatedVisibility(
                                visible = selectedShareAs == ShareAs.Image,
                                enter = enterAnimation,
                                exit = exitAnimation
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_check),
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(stringResource(R.string.image))
                        }
                    }
                }
            }

            Surface(tonalElevation = 4.dp, shadowElevation = 4.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = handleShare,
                        enabled = selectedShareAs != null
                    ) {
                        Text(stringResource(R.string.share))
                    }
                }
            }
        }
    }
}
