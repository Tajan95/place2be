package de.place2be.feature.placeDetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun PlaceDetailScreen(
    place: PlaceDetailUiState,
    onRatePlace: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = place.name,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(text = place.locationHint)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = place.description)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Score: ${"%.1f".format(place.currentScore)}")
        Text(text = "Bewertungen: ${place.reviewCount}")
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Ortseigenschaften",
            style = MaterialTheme.typography.titleMedium,
        )
        place.attributes.forEach { attribute ->
            AssistChip(
                onClick = { },
                label = { Text(attribute.name) },
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onRatePlace(place.uuid) }) {
            Text("Ort bewerten")
        }
    }
}
