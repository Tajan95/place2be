package de.place2be.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.place2be.feature.map.MapScreen
import de.place2be.feature.map.MapViewModel

/**
 * Zentrale App-Composable.
 *
 * Aktuell zeigt sie als MVP-Einstieg die Mock-Map. Später kann hier Navigation
 * zwischen Onboarding, Map, Detailansicht, Rating, Profil und Einstellungen
 * ergänzt werden.
 */
@Composable
fun Place2BeApp() {
    val mapViewModel = remember { MapViewModel() }
    MapScreen(
        places = mapViewModel.getMapItems(),
        onPlaceSelected = { /* Navigation zur Detailansicht folgt */ },
    )
}
