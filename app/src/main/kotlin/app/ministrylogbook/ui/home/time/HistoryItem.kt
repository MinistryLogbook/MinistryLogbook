package app.ministrylogbook.ui.home.time

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ministrylogbook.R
import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.shared.utilities.condition
import java.time.format.DateTimeFormatter
import kotlinx.datetime.toJavaLocalDateTime

@Composable
fun HistoryItem(entry: Entry, subtract: Boolean = false, onClick: (() -> Unit)? = null) {
    val pattern = stringResource(R.string.history_entry_datetime_pattern)
    val formatter = DateTimeFormatter.ofPattern(pattern)
    val dateText = formatter.format(entry.datetime.toJavaLocalDateTime())
    val formattedTime by remember(entry) {
        derivedStateOf {
            "${entry.hours}" + if (entry.minutes > 0) {
                ":${entry.minutes.toString().padStart(2, '0')}"
            } else {
                ""
            }
        }
    }
    val isTransfer = entry.type == EntryType.Transfer

    Row(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .condition(onClick != null) {
                clickable(onClick = onClick!!)
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tint = if (isTransfer && subtract) Color(0xFFE0E0E0) else entry.type.color()
        Box(
            Modifier
                .clip(CircleShape)
                .background(tint.copy(0.2f))
                .padding(8.dp)
        ) {
            val iconResource =
                if (isTransfer && subtract) painterResource(R.drawable.ic_output) else entry.type.icon()
            Icon(
                painter = iconResource,
                contentDescription = null, // TODO: contentDescription
                modifier = Modifier.size(20.dp),
                tint = tint.copy(0.8f)
            )
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val text = if (!isTransfer) {
                    entry.type.translate()
                } else if (subtract) {
                    stringResource(R.string.min_transferred_to_next_month, entry.minutes)
                } else {
                    stringResource(R.string.min_transferred_from_last_month, entry.minutes)
                }
                Text(
                    text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                if (!isTransfer) {
                    Row {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            dateText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                        )
                    }
                }
            }

            if (!isTransfer) {
                Spacer(Modifier.height(4.dp))
                Row {
                    if (entry.hours > 0 || entry.minutes > 0) {
                        HistoryItemChip(
                            icon = painterResource(R.drawable.ic_schedule),
                            text = stringResource(R.string.hours_short_unit, formattedTime)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemChip(icon: Painter? = null, text: String) {
    val color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
    Row(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(0.1f))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null, // TODO: contentDescription
                modifier = Modifier.size(16.dp),
                tint = color
            )
        }
        Text(
            text,
            style = TextStyle(color = color),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
