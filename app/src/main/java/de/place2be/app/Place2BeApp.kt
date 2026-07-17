package de.place2be.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
 * Map, erweiterbare Ortsdetails und Bewertung bleiben dauerhaft komponiert. Die
 * Profilansicht wird darübergelegt, damit bei einem kurzen Wechsel zum Autorprofil
 * der ausgewählte Ort, das geöffnete Bottom-Sheet und dessen Scrollkontext erhalten
 * bleiben. Die betrachtete Profil-UUID entscheidet zusammen mit der Demo-Nutzer-UUID,
 * ob ProfileViewModel eine private OWN- oder begrenzte PUBLIC-Ansicht liefert.
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
    var viewedProfileUserUuidString by rememberSaveable {
        mutableStateOf(DEMO_USER_UUID.toString())
    }
    val destination = AppDestination.valueOf(destinationName)
    val viewedProfileUserUuid = runCatching {
        UUID.fromString(viewedProfileUserUuidString)
    }.getOrDefault(DEMO_USER_UUID)

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
    val profileUiState = remember(profileViewModel, viewedProfileUserUuid, dataRevision) {
        profileViewModel.getProfile(
            profileUserUuid = viewedProfileUserUuid,
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

    fun openProfile(userUuid: UUID) {
        viewedProfileUserUuidString = userUuid.toString()
        destinationName = AppDestination.PROFILE.name
    }

    BackHandler(enabled = destination == AppDestination.PROFILE) {
        destinationName = AppDestination.MAP.name
    }

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
            onReviewAuthorSelected =(::openProfile),
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

        if (destination == AppDestination.MAP) {
            ProfileEntryButton(
                initial = profileViewModel
                    .getProfile(DEMO_USER_UUID, DEMO_USER_UUID)
                    ?.profileInitial
                    ?: "?",
                onClick = { openProfile(DEMO_USER_UUID) },
            )
        }

        if (destination == AppDestination.PROFILE) {
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
private fun BoxScope.ProfileEntryButton(
    initial: String,
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
                    text = "Dieses Community-Profil ist nicht mehr verfügbar · Zurück zum Ort",
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
