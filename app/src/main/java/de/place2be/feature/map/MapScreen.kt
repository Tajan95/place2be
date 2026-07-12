package de.place2be.feature.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID

/**
 * Mock-Map-Ansicht für den MVP.
 *
 * Die Darstellung ist bewusst noch keine echte Live-Karte. Sie bildet den
 * geplanten Einstiegspunkt der App ab und kann später durch eine echte
 * Kartenintegration ersetzt werden.
 */
@Composable
fun MapScreen(
    places: List<MapPlaceUiState>,
    onPlaceSelected: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "place2be Map",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Mock-Karte mit empfohlenen öffentlichen Orten",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(places) { place ->
                MapPlaceCard(
                    place = place,
                    onClick = { onPlaceSelected(place.uuid) },
                )
            }
        }
    }
}

@Composable
private fun MapPlaceCard(
    place: MapPlaceUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(text = place.locationHint)
            Text(text = "Kategorie: ${place.categoryLabel}")
            Text(text = "Score: ${"%.1f".format(place.currentScore)}")
        }
    }
}
