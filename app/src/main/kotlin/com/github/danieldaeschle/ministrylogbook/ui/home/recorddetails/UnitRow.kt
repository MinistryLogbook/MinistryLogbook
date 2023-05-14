package com.github.danieldaeschle.ministrylogbook.ui.home.recorddetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun UnitRow(
    text: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: Painter? = null,
    rightSide: @Composable () -> Unit = {},
) {
    Row(
        Modifier
            .height(48.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null, // TODO: contentDescription
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.width(20.dp))
        } else {
            Spacer(Modifier.width(44.dp))
        }
        Column(
            Modifier
                .padding(end = 16.dp)
                .weight(1f)
        ) {
            Text(text)
            if (description != null) {
                Text(
                    description,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.End) {
            rightSide()
        }
    }
}