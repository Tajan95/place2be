package de.place2be.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import de.place2be.data.mock.MockPlaceDataSource
import de.place2be.data.repository.MockPlaceRepository
import de.place2be.data.repository.MockUserRepository
import de.place2be.feature.map.MapScreen
import de.place2be.feature.map.MapViewModel
import java.util.UUID

/**
 * Zentrale App-Composable.
 *
 * Aktuell zeigt sie als MVP-Einstieg die Mock-Map. Später kann hier Navigation
 * zwischen Onboarding, Map, Detailansicht, Rating, Profil und Einstellungen
 * ergänzt werden.
 */
@Composable
fun Place2BeApp() {
    val appContext = LocalContext.current.applicationContext
    val mockDataSource = remember(appContext) { MockPlaceDataSource.create(appContext) }
    val placeRepository = remember(mockDataSource) { MockPlaceRepository(mockDataSource) }
    val userRepository = remember(mockDataSource) { MockUserRepository(mockDataSource) }
    val mapViewModel = remember(placeRepository, userRepository) {
        MapViewModel(
            placeRepository = placeRepository,
            userRepository = userRepository,
        )
    }
    val places = remember(mapViewModel) { mapViewModel.getMapItems() }
    var selectedPlaceUuidString by rememberSaveable { mutableStateOf<String?>(null) }
    MapScreen(
        places = places,
        selectedPlaceUuid = selectedPlaceUuidString?.let(UUID::fromString),
        onPlaceSelected = { selectedPlaceUuidString = it.toString() },
        onSelectionCleared = { selectedPlaceUuidString = null },
    )
}
