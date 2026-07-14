package de.place2be.feature.rating

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onReviewTextChanged: (String) -> Unit,
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
        Spacer(modifier = Modifier.height(8.dp))
        RatingGuidance()
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
        OutlinedTextField(
            value = rating.reviewText,
            onValueChange = { newText ->
                if (newText.length <= RatingUiState.MAX_REVIEW_TEXT_LENGTH) {
                    onReviewTextChanged(newText)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 128.dp),
            label = { Text("Kurze Rezension") },
            placeholder = { Text("Was sollten andere über diesen Ort wissen?") },
            supportingText = {
                Text(
                    text = "${rating.reviewText.length}/${RatingUiState.MAX_REVIEW_TEXT_LENGTH} Zeichen · optional, aber hilfreich",
                )
            },
            minLines = 3,
            maxLines = 5,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSubmit,
            enabled = rating.isSubmitEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Bewertung speichern")
        }
    }
}

@Composable
private fun RatingGuidance(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Vibes meint die Atmosphäre, Stimmung und ob man dort gerne Zeit verbringt.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Bewertungen sind nur vor Ort möglich. So bleiben die Eindrücke aktuell und glaubwürdig.",
                style = MaterialTheme.typography.bodySmall,
            )
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
