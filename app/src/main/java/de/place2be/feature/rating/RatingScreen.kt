package de.place2be.feature.rating

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import de.place2be.ui.component.RatingCriterion
import de.place2be.ui.component.RatingCriterionIcon
import de.place2be.ui.theme.Moss

@Composable
fun RatingScreen(
    rating: RatingUiState,
    onVibeChanged: (Int) -> Unit,
    onSafetyChanged: (Int) -> Unit,
    onAccessibilityChanged: (Int) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Ort bewerten",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        RatingSlider(
            label = "Vibes",
            criterion = RatingCriterion.VIBE,
            value = rating.vibe,
            onValueChanged = onVibeChanged,
        )
        RatingSlider(
            label = "Sicherheit",
            criterion = RatingCriterion.SAFETY,
            value = rating.safety,
            onValueChanged = onSafetyChanged,
        )
        RatingSlider(
            label = "Erreichbarkeit",
            criterion = RatingCriterion.ACCESSIBILITY,
            value = rating.accessibility,
            onValueChanged = onAccessibilityChanged,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSubmit) {
            Text("Bewertung speichern")
        }
    }
}

@Composable
private fun RatingSlider(
    label: String,
    criterion: RatingCriterion,
    value: Int,
    onValueChanged: (Int) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RatingCriterionIcon(
            criterion = criterion,
            color = Moss,
        )
        Spacer(Modifier.width(8.dp))
        Text(text = "$label: $value")
    }
    Slider(
        value = value.toFloat(),
        onValueChange = { onValueChanged(it.toInt().coerceIn(MIN_RATING, MAX_RATING)) },
        valueRange = MIN_RATING.toFloat()..MAX_RATING.toFloat(),
        steps = 3,
    )
}

private const val MIN_RATING = 1
private const val MAX_RATING = 5
