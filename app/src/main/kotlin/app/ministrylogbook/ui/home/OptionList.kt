package app.ministrylogbook.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ministrylogbook.lib.condition

interface OptionListScope {
    @Composable
    fun Option(text: String)

    @Composable
    fun Option(text: String, selected: Boolean, onClick: () -> Unit)

    @Composable
    fun Option(text: String, onClick: () -> Unit)
}

internal class OptionListScopeImpl(private val bullets: Boolean) : OptionListScope {
    @Composable
    override fun Option(text: String, selected: Boolean, onClick: () -> Unit) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .condition(bullets) {
                    padding(horizontal = 12.dp)
                }
                .condition(!bullets) {
                    padding(horizontal = 24.dp, vertical = 12.dp)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (bullets) {
                RadioButton(selected = selected, onClick = { onClick() })
                Spacer(Modifier.width(8.dp))
            }
            Text(text)
        }
    }

    @Composable
    override fun Option(text: String) = Option(text, selected = false, onClick = {})

    @Composable
    override fun Option(text: String, onClick: () -> Unit) =
        Option(text, selected = false, onClick = onClick)
}

@Composable
fun OptionList(bullets: Boolean = false, content: @Composable OptionListScope.() -> Unit) {
    Column {
        OptionListScopeImpl(bullets).content()
    }
}
