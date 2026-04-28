package app.ministrylogbook.data

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.ministrylogbook.R
import app.ministrylogbook.ui.theme.ProgressPositive

enum class EntryType {
    Ministry,
    TheocraticAssignment,
    TheocraticSchool,
    Transfer;

    @Composable
    @ReadOnlyComposable
    fun translate(): String = when (this) {
        Ministry -> stringResource(R.string.ministry)
        TheocraticAssignment -> stringResource(R.string.theocratic_assignment)
        TheocraticSchool -> stringResource(R.string.theocratic_school_or_class)
        Transfer -> stringResource(R.string.transfer)
    }

    @Composable
    fun icon(): Painter = when (this) {
        Ministry -> painterResource(R.drawable.ic_work)
        TheocraticAssignment -> painterResource(R.drawable.ic_build)
        TheocraticSchool -> painterResource(R.drawable.ic_school)
        Transfer -> painterResource(R.drawable.ic_exit_to_app)
    }

    @Composable
    @ReadOnlyComposable
    fun color(): Color = when (this) {
        Ministry -> MaterialTheme.colorScheme.primary
        TheocraticAssignment -> Color(0xFFFF9800)
        TheocraticSchool -> Color(0xFFFF9800)
        Transfer -> ProgressPositive
    }
}
