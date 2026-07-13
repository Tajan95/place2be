package de.place2be.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RatingCriterion {
    VIBE,
    SAFETY,
    ACCESSIBILITY,
}

/** Einheitliche Symbole fuer die drei Bewertungskriterien der App. */
@Composable
fun RatingCriterionIcon(
    criterion: RatingCriterion,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (criterion) {
            RatingCriterion.VIBE -> Text(
                text = "☺",
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            RatingCriterion.SAFETY -> Canvas(Modifier.fillMaxSize()) {
                val shield = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.06f)
                    lineTo(size.width * 0.86f, size.height * 0.2f)
                    lineTo(size.width * 0.82f, size.height * 0.59f)
                    quadraticTo(size.width * 0.76f, size.height * 0.83f, size.width * 0.5f, size.height * 0.96f)
                    quadraticTo(size.width * 0.24f, size.height * 0.83f, size.width * 0.18f, size.height * 0.59f)
                    lineTo(size.width * 0.14f, size.height * 0.2f)
                    close()
                }
                drawPath(shield, color, style = Stroke(width = size.width * 0.11f))
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.5f, size.height * 0.22f),
                    end = Offset(size.width * 0.5f, size.height * 0.72f),
                    strokeWidth = size.width * 0.08f,
                )
            }

            RatingCriterion.ACCESSIBILITY -> Canvas(Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.12f, size.height * 0.08f),
                    size = Size(size.width * 0.76f, size.height * 0.72f),
                    cornerRadius = CornerRadius(size.width * 0.13f),
                    style = Stroke(width = size.width * 0.1f),
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.25f, size.height * 0.38f),
                    end = Offset(size.width * 0.75f, size.height * 0.38f),
                    strokeWidth = size.width * 0.08f,
                )
                drawCircle(color, radius = size.width * 0.09f, center = Offset(size.width * 0.29f, size.height * 0.86f))
                drawCircle(color, radius = size.width * 0.09f, center = Offset(size.width * 0.71f, size.height * 0.86f))
            }
        }
    }
}
