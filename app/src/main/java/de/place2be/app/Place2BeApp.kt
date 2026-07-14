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
import de.place2be.domain.model.Review
import de.place2be.domain.usecase.ReviewSubmissionCooldownPolicy
import de.place2be.feature.map.MapScreenWithRatingEntry
import de.place2be.feature.map.MapViewModel
import de.place2be.feature.rating.RatingViewModel
import java.util.UUID

/**
 * Zentrale App-Composable für den MVP-Demo-Flow.
 *
 * Die Bewertung bleibt Teil der erweiterbaren Orts-Detailansicht über der Map.
 * Die Vor-Ort-Bedingung wird auf diesem Feature-Branch bewusst simuliert, damit
 * Slider, Textrezension, Persistenz, Cooldown und Score-Aktualisierung in der IDE
 * getestet werden können. Nur die Standortfreigabe ist simuliert; die
 * 24-Stunden-Sperre wird anhand der tatsächlich gespeicherten Reviews geprüft.
 * Die fachliche Standort-/Mindestaufenthaltslogik bleibt in core.location
 * vorbereitet.
 */
@Composable
fun Place2BeApp() {
    val appContext = LocalContext.current.applicationContext
    val mockDataSource = remember(appContext) { MockPlaceDataSource.create(appContext) }
    val placeRepository = remember(mockDataSource) { MockPlaceRepository(mockDataSource) }
    val userRepository = remember(mockDataSource) { MockUserRepository(mockDataSource) }
    val ratingViewModel = remember(placeRepository) { RatingViewModel(placeRepository) }
    val cooldownPolicy = remember { ReviewSubmissionCooldownPolicy() }

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
            .map(Review::userUuid)
            .distinct()
            .associateWith { userUuid ->
                userRepository.getUser(userUuid)?.displayName ?: "Community-Mitglied"
            }
    }
    val submissionAvailability = remember(selectedReviews, dataRevision) {
        cooldownPolicy.evaluate(
            reviewsForPlace = selectedReviews,
            userUuid = DEMO_USER_UUID,
        )
    }

    MapScreenWithRatingEntry(
        places = places,
        selectedPlaceUuid = selectedPlaceUuid,
        reviewsForSelectedPlace = selectedReviews,
        reviewAuthorNames = reviewAuthorNames,
        currentUserUuid = DEMO_USER_UUID,
        ratingCooldownRemainingMillis = submissionAvailability.remainingMillis,
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
            // Erzwingt eine neue Repository-Abfrage, damit Rezension, Cooldown
            // und aktualisierte Kriterienwerte unmittelbar sichtbar werden.
            dataRevision++
        },
    )
}

private val DEMO_USER_UUID: UUID = UUID.fromString("f5257520-3685-4a1a-be5b-4a0ceb1baba7")
