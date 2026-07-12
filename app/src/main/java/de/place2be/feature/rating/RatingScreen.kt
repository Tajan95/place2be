package de.place2be.feature.rating

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
            value = rating.vibe,
            onValueChanged = onVibeChanged,
        )
        RatingSlider(
            label = "Sicherheit",
            value = rating.safety,
            onValueChanged = onSafetyChanged,
        )
        RatingSlider(
            label = "Erreichbarkeit",
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
    value: Int,
    onValueChanged: (Int) -> Unit,
) {
    Text(text = "$label: $value")
    Slider(
        value = value.toFloat(),
        onValueChange = { onValueChanged(it.toInt().coerceIn(MIN_RATING, MAX_RATING)) },
        valueRange = MIN_RATING.toFloat()..MAX_RATING.toFloat(),
        steps = 3,
    )
}

private const val MIN_RATING = 1
private const val MAX_RATING = 5
