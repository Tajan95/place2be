package de.place2be.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
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
import de.place2be.feature.profile.ProfileScreen
import de.place2be.feature.profile.ProfileViewModel
import de.place2be.feature.rating.RatingViewModel
import de.place2be.ui.theme.DarkInk
import de.place2be.ui.theme.LeafSurface
import de.place2be.ui.theme.Moss
import de.place2be.ui.theme.WarmSurface
import java.util.UUID

/**
 * Zentrale App-Composable für den MVP-Demo-Flow.
 *
 * Map, erweiterbare Ortsdetails und Bewertung bleiben der primäre Flow. Das
 * Profil ist über den bestehenden Nutzerkreis im Dashboard erreichbar und zeigt
 * den dynamischen Nutzer-Score sowie die private Bewertungs-Historie.
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
    val profileViewModel = remember(
        userRepository,
        placeRepository,
        reviewReactionRepository,
        calculateUserScoreUseCase,
    ) {
        ProfileViewModel(
            userRepository = userRepository,
            placeRepository = placeRepository,
            reviewReactionRepository = reviewReactionRepository,
            calculateUserScoreUseCase = calculateUserScoreUseCase,
        )
    }

    var dataRevision by rememberSaveable { mutableStateOf(0) }
    var selectedPlaceUuidString by rememberSaveable { mutableStateOf<String?>(null) }
    var destinationName by rememberSaveable { mutableStateOf(AppDestination.MAP.name) }
    val destination = AppDestination.valueOf(destinationName)

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
    val currentUser = remember(userRepository, dataRevision) {
        userRepository.getUser(DEMO_USER_UUID)
    }
    val profileUiState = remember(profileViewModel, dataRevision) {
        profileViewModel.getProfile(
            profileUserUuid = DEMO_USER_UUID,
            viewerUserUuid = DEMO_USER_UUID,
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

    BackHandler(enabled = destination == AppDestination.PROFILE) {
        destinationName = AppDestination.MAP.name
    }

    when (destination) {
        AppDestination.MAP -> {
            Box(modifier = Modifier.fillMaxSize()) {
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
                        // Profil-Historie und Score werden aus demselben Datenstand neu aufgebaut.
                        dataRevision++
                    },
                )

                ProfileEntryButton(
                    initial = currentUser?.displayName
                        ?.trim()
                        ?.firstOrNull()
                        ?.uppercaseChar()
                        ?.toString()
                        ?: "?",
                    totalScore = userScoreResult.totalScore,
                    onClick = { destinationName = AppDestination.PROFILE.name },
                )
            }
        }

        AppDestination.PROFILE -> {
            if (profileUiState != null) {
                ProfileScreen(
                    profile = profileUiState,
                    onBack = { destinationName = AppDestination.MAP.name },
                )
            } else {
                MissingProfileScreen(
                    onBack = { destinationName = AppDestination.MAP.name },
                )
            }
        }
    }
}

/**
 * Legt eine klickbare Schicht exakt über den bereits sichtbaren Profilkreis des
 * Map-Headers. So bleibt das Dashboard-Layout unverändert und erhält dennoch
 * einen klaren Profilzugang ohne zusätzliches Einstellungs-Zahnrad.
 */
@Composable
private fun ProfileEntryButton(
    initial: String,
    totalScore: Int,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .statusBarsPadding()
            .padding(top = 23.dp, end = 32.dp)
            .size(42.dp),
        shape = CircleShape,
        color = LeafSurface,
        contentColor = Moss,
        border = androidx.compose.foundation.BorderStroke(2.dp, Moss.copy(alpha = 0.65f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initial,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun MissingProfileScreen(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmSurface,
        contentColor = DarkInk,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                onClick = onBack,
                shape = RoundedCornerShape(18.dp),
                color = LeafSurface,
                contentColor = Moss,
            ) {
                Text(
                    text = "Profil nicht gefunden · Zurück zur Karte",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private enum class AppDestination {
    MAP,
    PROFILE,
}

private val DEMO_USER_UUID: UUID = UUID.fromString("f5257520-3685-4a1a-be5b-4a0ceb1baba7")
