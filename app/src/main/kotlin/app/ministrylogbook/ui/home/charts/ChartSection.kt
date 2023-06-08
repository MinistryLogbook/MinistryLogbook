package app.ministrylogbook.ui.home.charts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ministrylogbook.ui.theme.MinistryLogbookTheme

@Composable
fun ChartSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
//            verticalAlignment = Alignment.CenterVertically,
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(36.dp)
//                    .clip(CircleShape)
//                    .clickable { },
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    Icons.Outlined.KeyboardArrowLeft,
//                    contentDescription = "Previous site of chart",
//                )
//            }
//            Text("2022", modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
//            Box(
//                modifier = Modifier
//                    .size(36.dp)
//                    .clip(CircleShape)
//                    .clickable { },
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    Icons.Outlined.KeyboardArrowRight,
//                    contentDescription = "Next site of chart",
//                )
//            }
//
//        }
        val barColor = MaterialTheme.colorScheme.secondary
        Chart(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            horizontalPadding = 16.dp,
            bars = arrayOf(
                Bar(20f, "Jan", color = barColor),
                Bar(30f, "Feb", color = barColor),
                Bar(25f, "Mar", color = barColor),
                Bar(40f, "Apr", color = barColor),
                Bar(51f, "May", color = barColor),
                Bar(74f, "Jun", color = barColor),
                Bar(14f, "Jul", color = barColor),
                Bar(62f, "Aug", color = barColor),
                Bar(72f, "Sep", color = barColor),
                Bar(12f, "Oct", color = barColor),
                Bar(55f, "Nov", color = barColor),
                Bar(72f, "Dec", color = barColor)
            ),
            markers = arrayOf(
                VerticalMarker(40f, MaterialTheme.colorScheme.onSurface.copy(0.6f), width = 0.5.dp),
                VerticalMarker(
                    70f,
                    MaterialTheme.colorScheme.primary,
                    style = VerticalMarkerStyle.Dashed
                )
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier
                    .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                    .height(28.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                elevation = null,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(
                        0.2f
                    ),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                onClick = {}
            ) {
                Text("Year", style = TextStyle(fontSize = 14.sp))
            }
            Spacer(Modifier.width(8.dp))
            Button(
                modifier = Modifier
                    .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                    .height(28.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                elevation = null,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(
                        0.2f
                    )
                ),
                onClick = {}
            ) {
                Text("Month", style = TextStyle(fontSize = 14.sp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChartSectionPreview() = MinistryLogbookTheme {
    ChartSection()
}
