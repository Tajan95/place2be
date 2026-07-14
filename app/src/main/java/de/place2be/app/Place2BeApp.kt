package de.place2be.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import de.place2be.core.location.LocationConfirmationState
import de.place2be.data.mock.MockPlaceDataSource
import de.place2be.data.repository.MockPlaceRepository
import de.place2be.data.repository.MockUserRepository
import de.place2be.feature.map.MapScreenWithRatingEntry
import de.place2be.feature.map.MapViewModel
import de.place2be.feature.rating.RatingScreen
import de.place2be.feature.rating.RatingUiState
import de.place2be.feature.rating.RatingViewModel
import java.util.UUID

/**
 * Zentrale App-Composable.
 *
 * Aktuell verbindet sie als MVP-Demo die Mock-Map mit dem Bewertungsformular.
 * Die Vor-Ort-Bedingung wird hier bewusst simuliert, damit Issue #6 in der IDE
 * testbar ist. Die fachliche Mindestaufenthalts-/Standortlogik bleibt in
 * core.location vorbereitet und kann später durch echte Standortdaten ersetzt
 * werden.
 */
@Composable
fun Place2BeApp() {
    val appContext = LocalContext.current.applicationContext
    val mockDataSource = remember(appContext) { MockPlaceDataSource.create(appContext) }
    val placeRepository = remember(mockDataSource) { MockPlaceRepository(mockDataSource) }
    val userRepository = remember(mockDataSource) { MockUserRepository(mockDataSource) }
    var dataRevision by rememberSaveable { mutableStateOf(0) }
    var selectedPlaceUuidString by rememberSaveable { mutableStateOf<String?>(null) }
    var routeName by rememberSaveable { mutableStateOf(Place2BeRoute.MAP.name) }
    // RatingUiState is a plain Kotlin data class and currently has no Saver/Parcelable.
    // Keeping this draft state non-saveable avoids a runtime crash during app start.
    var ratingState by remember { mutableStateOf(RatingUiState()) }

    val mapViewModel = remember(placeRepository, userRepository, dataRevision) {
        MapViewModel(
            placeRepository = placeRepository,
            userRepository = userRepository,
            // Demo-Hinweis: dadurch ist der Rating-Einstieg sichtbar und testbar.
            // Eine echte App würde hier erst nach Standort- und Aufenthaltsprüfung
            // CONFIRMED_ON_SITE liefern.
            locationConfirmationState = LocationConfirmationState.SIMULATED_CONFIRMED,
        )
    }
    val ratingViewModel = remember(placeRepository) { RatingViewModel(placeRepository) }
    val places = remember(mapViewModel, dataRevision) { mapViewModel.getMapItems() }
    val selectedPlaceUuid = selectedPlaceUuidString?.let(UUID::fromString)

    when (Place2BeRoute.valueOf(routeName)) {
        Place2BeRoute.MAP -> MapScreenWithRatingEntry(
            places = places,
            selectedPlaceUuid = selectedPlaceUuid,
            onPlaceSelected = { selectedPlaceUuidString = it.toString() },
            onSelectionCleared = { selectedPlaceUuidString = null },
            onRatePlace = { placeUuid ->
                selectedPlaceUuidString = placeUuid.toString()
                ratingState = RatingUiState()
                routeName = Place2BeRoute.RATING.name
            },
        )

        Place2BeRoute.RATING -> RatingScreen(
            rating = ratingState,
            onVibeChanged = { ratingState = ratingState.copy(vibe = it) },
            onSafetyChanged = { ratingState = ratingState.copy(safety = it) },
            onAccessibilityChanged = { ratingState = ratingState.copy(accessibility = it) },
            onReviewTextChanged = { ratingState = ratingState.copy(reviewText = it) },
            onSubmit = {
                val placeUuid = selectedPlaceUuid ?: return@RatingScreen
                ratingViewModel.submitRating(
                    placeUuid = placeUuid,
                    userUuid = DEMO_USER_UUID,
                    vibe = ratingState.vibe,
                    safety = ratingState.safety,
                    accessibility = ratingState.accessibility,
                    text = ratingState.reviewText,
                )
                dataRevision++
                routeName = Place2BeRoute.MAP.name
            },
        )
    }
}

private enum class Place2BeRoute {
    MAP,
    RATING,
}

private val DEMO_USER_UUID: UUID = UUID.fromString("f5257520-3685-4a1a-be5b-4a0ceb1baba7")