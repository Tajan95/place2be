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
import de.place2be.feature.rating.RatingViewModel
import java.util.UUID

/**
 * Zentrale App-Composable für den MVP-Demo-Flow.
 *
 * Die Bewertung bleibt Teil der erweiterbaren Orts-Detailansicht über der Map.
 * Die Vor-Ort-Bedingung wird auf diesem Feature-Branch bewusst simuliert, damit
 * Slider, Textrezension, Persistenz und Score-Aktualisierung in der IDE getestet
 * werden können. Die fachliche Standort-/Mindestaufenthaltslogik bleibt in
 * core.location vorbereitet.
 */
@Composable
fun Place2BeApp() {
    val appContext = LocalContext.current.applicationContext
    val mockDataSource = remember(appContext) { MockPlaceDataSource.create(appContext) }
    val placeRepository = remember(mockDataSource) { MockPlaceRepository(mockDataSource) }
    val userRepository = remember(mockDataSource) { MockUserRepository(mockDataSource) }
    val ratingViewModel = remember(placeRepository) { RatingViewModel(placeRepository) }

    var dataRevision by rememberSaveable { mutableStateOf(0) }
    var selectedPlaceUuidString by rememberSaveable { mutableStateOf<String?>(null) }

    val mapViewModel = remember(placeRepository, userRepository, dataRevision) {
        MapViewModel(
            placeRepository = placeRepository,
            userRepository = userRepository,
            // Nur für die IDE-/MVP-Demo: Die echte App darf erst nach Standort-
            // und Aufenthaltsprüfung auf CONFIRMED_ON_SITE wechseln.
            locationConfirmationState = LocationConfirmationState.SIMULATED_CONFIRMED,
        )
    }
    val places = remember(mapViewModel, dataRevision) { mapViewModel.getMapItems() }
    val selectedPlaceUuid = selectedPlaceUuidString?.let(UUID::fromString)
    val selectedReviews = remember(selectedPlaceUuid, dataRevision) {
        selectedPlaceUuid?.let(placeRepository::getReviewsForPlace).orEmpty()
    }
    val reviewAuthorNames = remember(selectedReviews, userRepository, dataRevision) {
        selectedReviews
            .map(ReviewAuthorKey::from)
            .distinct()
            .associate { key ->
                key.userUuid to (userRepository.getUser(key.userUuid)?.displayName ?: "Community-Mitglied")
            }
    }

    MapScreenWithRatingEntry(
        places = places,
        selectedPlaceUuid = selectedPlaceUuid,
        reviewsForSelectedPlace = selectedReviews,
        reviewAuthorNames = reviewAuthorNames,
        onPlaceSelected = { selectedPlaceUuidString = it.toString() },
        onSelectionCleared = { selectedPlaceUuidString = null },
        onSubmitRating = { placeUuid, vibe, safety, accessibility, text ->
            ratingViewModel.submitRating(
                placeUuid = placeUuid,
                userUuid = DEMO_USER_UUID,
                vibe = vibe,
                safety = safety,
                accessibility = accessibility,
                text = text,
            )
            // Erzwingt eine neue Repository-Abfrage, damit die gespeicherte
            // Rezension und die aktualisierten Kriterienwerte sofort erscheinen.
            dataRevision++
        },
    )
}

private data class ReviewAuthorKey(val userUuid: UUID) {
    companion object {
        fun from(review: de.place2be.domain.model.Review): ReviewAuthorKey = ReviewAuthorKey(review.userUuid)
    }
}

private val DEMO_USER_UUID: UUID = UUID.fromString("f5257520-3685-4a1a-be5b-4a0ceb1baba7")
