package de.place2be.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.place2be.core.location.LocationConfirmationState
import de.place2be.data.mock.MockPlaceDataSource
import de.place2be.data.repository.MockPlaceRepository
import de.place2be.data.repository.MockReviewReactionRepository
import de.place2be.data.repository.MockUserRepository
import de.place2be.domain.model.Review
import de.place2be.domain.usecase.CalculateUserScoreUseCase
import de.place2be.domain.usecase.ReviewSubmissionCooldownPolicy
import de.place2be.feature.map.MapScreenWithRatingEntry
import de.place2be.feature.map.MapViewModel
import de.place2be.feature.rating.RatingViewModel
import de.place2be.ui.theme.LeafSurface
import de.place2be.ui.theme.Moss
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
 *
 * Bookmarks und Review-Reaktionen werden über Local-first-Repositories
 * gespeichert. Nach jeder Änderung wird derselbe lokale Datenstand erneut
 * gelesen. Der Nutzer-Score wird daraus dynamisch neu berechnet, statt als
 * dauerhaft inkrementierter Wert gespeichert zu werden.
 */
@Composable
fun Place2BeApp() {
    val appContext = LocalContext.current.applicationContext
    val mockDataSource = remember(appContext) { MockPlaceDataSource.create(appContext) }
    val placeRepository = remember(mockDataSource) { MockPlaceRepository(mockDataSource) }
    val userRepository = remember(mockDataSource) { MockUserRepository(mockDataSource) }
    val reviewReactionRepository = remember(appContext, placeRepository) {
        MockReviewReactionRepository.create(appContext, placeRepository)
    }
    val ratingViewModel = remember(placeRepository) { RatingViewModel(placeRepository) }
    val cooldownPolicy = remember { ReviewSubmissionCooldownPolicy() }
    val calculateUserScoreUseCase = remember { CalculateUserScoreUseCase() }

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
    val allDomainPlaces = remember(placeRepository, dataRevision) { placeRepository.getPlaces() }
    val allReviews = remember(placeRepository, dataRevision) { placeRepository.getReviews() }
    val currentUserReactions = remember(reviewReactionRepository, dataRevision) {
        reviewReactionRepository.getReactionsForUser(DEMO_USER_UUID)
    }
    val userScoreResult = remember(
        allDomainPlaces,
        allReviews,
        currentUserReactions,
        calculateUserScoreUseCase,
    ) {
        calculateUserScoreUseCase.calculate(
            userUuid = DEMO_USER_UUID,
            places = allDomainPlaces,
            reviews = allReviews,
            reactions = currentUserReactions,
        )
    }

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
    val currentUserReactionTypes = remember(selectedReviews, reviewReactionRepository, dataRevision) {
        selectedReviews.associate { review ->
            review.uuid to reviewReactionRepository
                .getReaction(review.uuid, DEMO_USER_UUID)
                ?.type
        }
    }
    val submissionAvailability = remember(selectedReviews, dataRevision) {
        cooldownPolicy.evaluate(
            reviewsForPlace = selectedReviews,
            userUuid = DEMO_USER_UUID,
        )
    }

    Box {
        MapScreenWithRatingEntry(
            places = places,
            selectedPlaceUuid = selectedPlaceUuid,
            reviewsForSelectedPlace = selectedReviews,
            reviewAuthorNames = reviewAuthorNames,
            currentUserReactionTypes = currentUserReactionTypes,
            currentUserUuid = DEMO_USER_UUID,
            ratingCooldownRemainingMillis = submissionAvailability.remainingMillis,
            onPlaceSelected = { selectedPlaceUuidString = it.toString() },
            onSelectionCleared = { selectedPlaceUuidString = null },
            onBookmarkToggle = { placeUuid, bookmarked ->
                userRepository.setBookmarked(
                    userUuid = DEMO_USER_UUID,
                    placeUuid = placeUuid,
                    bookmarked = bookmarked,
                )
                dataRevision++
            },
            onReviewReactionToggle = { reviewUuid, type ->
                reviewReactionRepository.toggleReaction(
                    reviewUuid = reviewUuid,
                    userUuid = DEMO_USER_UUID,
                    type = type,
                )
                // Aktualisiert Zähler, Auswahl, Sortierung und Nutzer-Score.
                dataRevision++
            },
            onSubmitRating = { placeUuid, vibe, safety, accessibility, text ->
                ratingViewModel.submitRating(
                    placeUuid = placeUuid,
                    userUuid = DEMO_USER_UUID,
                    vibe = vibe,
                    safety = safety,
                    accessibility = accessibility,
                    text = text,
                )
                // Bewertungs-, Textbonus- und Reputationsbasis werden neu berechnet.
                dataRevision++
            },
        )

        // Vorläufige MVP-Anzeige. Issue #15 übernimmt später die ausführliche
        // Profilansicht mit Aufteilung in Aktivität und Reputation.
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 28.dp, end = 78.dp),
            shape = RoundedCornerShape(12.dp),
            color = LeafSurface,
            contentColor = Moss,
        ) {
            Text(
                text = "Score ${userScoreResult.totalScore}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private val DEMO_USER_UUID: UUID = UUID.fromString("f5257520-3685-4a1a-be5b-4a0ceb1baba7")
