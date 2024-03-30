package app.ministrylogbook.ui.home.biblestudies

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import app.ministrylogbook.R
import app.ministrylogbook.shared.layouts.ExpandAnimatedVisibility
import app.ministrylogbook.shared.layouts.ExtendableFloatingActionButton
import app.ministrylogbook.ui.home.viewmodel.HomeIntent
import app.ministrylogbook.ui.home.viewmodel.HomeState
import app.ministrylogbook.ui.shared.Tile

@Composable
fun BibleStudiesPage(
    state: HomeState,
    dispatch: (intent: HomeIntent) -> Unit = {},
    scrollState: ScrollState = rememberScrollState()
) {
    var fabExtended by remember { mutableStateOf(true) }
    var newBibleStudyName by rememberSaveable { mutableStateOf("") }
    var isDialogOpen by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(scrollState) {
        var prev = 0
        snapshotFlow { scrollState.value }.collect {
            fabExtended = it <= prev
            prev = it
        }
    }

    if (isDialogOpen) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        AlertDialog(
            onDismissRequest = { isDialogOpen = false },
            dismissButton = {
                TextButton(onClick = {
                    isDialogOpen = false
                    newBibleStudyName = ""
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dispatch(HomeIntent.CreateBibleStudy(newBibleStudyName.trim()))
                        isDialogOpen = false
                        newBibleStudyName = ""
                    },
                    enabled = newBibleStudyName.isNotBlank()
                ) {
                    Text(stringResource(R.string.add))
                }
            },
            title = {
                Text(stringResource(R.string.add_new_bible_study))
            },
            text = {
                OutlinedTextField(
                    modifier = Modifier.focusRequester(focusRequester),
                    value = newBibleStudyName,
                    onValueChange = { newBibleStudyName = it },
                    placeholder = {
                        Text(stringResource(R.string.name))
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            }
        )
    }

    Box {
        AnimatedVisibility(
            visible = state.bibleStudies.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut(spring(stiffness = Spring.StiffnessHigh))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 82.dp, top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(painterResource(R.drawable.studying), contentDescription = null)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 82.dp, top = 16.dp)
        ) {
            ExpandAnimatedVisibility(
                show = !state.monthlyInformation.dismissedBibleStudiesHint &&
                    state.bibleStudies.isNotEmpty() &&
                    !state.bibleStudies.any { it.checked }
            ) {
                Tile(title = { Text(stringResource(R.string.check_conducted_bible_studies)) }, onDismiss = {
                    dispatch(HomeIntent.DismissBibleStudyHint)
                }) {
                    Text(stringResource(R.string.check_conducted_bible_studies_description))
                }
                Spacer(Modifier.height(16.dp))
            }

            state.bibleStudies.forEach {
                BibleStudyItem(
                    it.name,
                    checked = it.checked,
                    onDelete = {
                        dispatch(HomeIntent.DeleteBibleStudy(it))
                    },
                    onCheckedChange = { newChecked ->
                        if (newChecked) {
                            dispatch(HomeIntent.CheckBibleStudy(it))
                        } else {
                            dispatch(HomeIntent.UncheckBibleStudy(it))
                        }
                    }
                )
            }
        }

        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            ExtendableFloatingActionButton(
                onClick = { isDialogOpen = true },
                extended = fabExtended,
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_add),
                        contentDescription = null // TODO: contentDescription
                    )
                },
                text = {
                    Text(stringResource(R.string.add_bible_study))
                }
            )
        }
    }
}
