package de.place2be.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.place2be.ui.theme.DarkInk
import de.place2be.ui.theme.DisabledSurface
import de.place2be.ui.theme.Moss
import java.util.UUID

/**
 * Kleiner Integrations-Wrapper für Issue #6.
 *
 * Morris' Dashboard-Implementierung bleibt unverändert. Der Wrapper ergänzt für
 * die Demo einen klaren Einstieg ins Bewertungsformular, sobald ein Ort in der
 * Mini-Preview ausgewählt ist. Die fachliche Freigabe kommt weiterhin aus
 * [MapPlaceUiState.canRate] und damit aus der Location-/Eligibility-Logik.
 */
@Composable
fun MapScreenWithRatingEntry(
    places: List<MapPlaceUiState>,
    selectedPlaceUuid: UUID?,
    onPlaceSelected: (UUID) -> Unit,
    onSelectionCleared: () -> Unit,
    onRatePlace: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedPlace = places.firstOrNull { it.uuid == selectedPlaceUuid }

    Box(modifier = modifier.fillMaxSize()) {
        MapScreen(
            places = places,
            selectedPlaceUuid = selectedPlaceUuid,
            onPlaceSelected = onPlaceSelected,
            onSelectionCleared = onSelectionCleared,
            modifier = Modifier.fillMaxSize(),
        )

        if (selectedPlace != null) {
            RatingEntryOverlay(
                place = selectedPlace,
                onRatePlace = onRatePlace,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, bottom = 22.dp),
            )
        }
    }
}

@Composable
private fun RatingEntryOverlay(
    place: MapPlaceUiState,
    onRatePlace: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (place.canRate) {
        Button(
            onClick = { onRatePlace(place.uuid) },
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Moss),
        ) {
            Text(
                text = "Bewertungsformular öffnen",
                fontWeight = FontWeight.SemiBold,
            )
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DisabledSurface,
            tonalElevation = 2.dp,
        ) {
            Text(
                text = place.ratingEligibilityMessage,
                color = DarkInk.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 13.dp, bottom = 13.dp),
            )
        }
    }
}
