package de.place2be.feature.profile

import de.place2be.domain.model.Review
import de.place2be.domain.model.UserScoreResult
import de.place2be.domain.repository.PlaceRepository
import de.place2be.domain.repository.ReviewReactionRepository
import de.place2be.domain.repository.UserRepository
import de.place2be.domain.usecase.CalculateUserScoreUseCase
import java.util.UUID

/**
 * Bereitet private und öffentliche Profilinformationen aus der Domain-Schicht auf.
 *
 * Die vollständige chronologische Bewertungs-Historie wird ausschließlich für das
 * eigene Profil ausgegeben. Fremde Profile erhalten nur aggregierte Werte gemäß
 * der Datenschutzentscheidung aus ADR-010 / Issue #20.
 */
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val placeRepository: PlaceRepository,
    private val reviewReactionRepository: ReviewReactionRepository,
    private val calculateUserScoreUseCase: CalculateUserScoreUseCase = CalculateUserScoreUseCase(),
) {
    fun getProfile(
        profileUserUuid: UUID,
        viewerUserUuid: UUID,
    ): ProfileUiState? {
        val user = userRepository.getUser(profileUserUuid) ?: return null
        val places = placeRepository.getPlaces()
        val placesByUuid = places.associateBy { it.uuid }
        val allReviews = placeRepository.getReviews()
        val userReviews = allReviews
            .filter { it.userUuid == profileUserUuid }
            .sortedWith(compareByDescending<Review> { it.timestampMillis }.thenBy { it.uuid.toString() })
        val userReactions = reviewReactionRepository.getReactionsForUser(profileUserUuid)
        val score = calculateUserScoreUseCase.calculate(
            userUuid = profileUserUuid,
            places = places,
            reviews = allReviews,
            reactions = userReactions,
        )
        val viewMode = if (profileUserUuid == viewerUserUuid) {
            ProfileViewMode.OWN
        } else {
            ProfileViewMode.PUBLIC
        }

        val history = if (viewMode == ProfileViewMode.OWN) {
            userReviews.map { review ->
                val place = placesByUuid[review.placeUuid]
                ProfileHistoryItemUiState(
                    reviewUuid = review.uuid,
                    placeUuid = review.placeUuid,
                    placeName = place?.name ?: "Unbekannter Ort",
                    locationHint = place?.locationHint ?: "Ortsangabe nicht verfügbar",
                    timestampMillis = review.timestampMillis,
                    vibe = review.vibe,
                    safety = review.safety,
                    accessibility = review.accessibility,
                    text = review.text,
                )
            }
        } else {
            emptyList()
        }

        return ProfileUiState(
            userUuid = user.uuid,
            displayName = user.displayName,
            profileInitial = user.displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?",
            viewMode = viewMode,
            score = score,
            reviewCount = userReviews.size,
            textReviewCount = userReviews.count { !it.text.isNullOrBlank() },
            helpfulReactionCount = userReviews.sumOf { review ->
                (review.likes - review.dislikes).coerceAtLeast(0)
            },
            history = history,
        )
    }
}

enum class ProfileViewMode {
    OWN,
    PUBLIC,
}

data class ProfileUiState(
    val userUuid: UUID,
    val displayName: String,
    val profileInitial: String,
    val viewMode: ProfileViewMode,
    val score: UserScoreResult,
    val reviewCount: Int,
    val textReviewCount: Int,
    val helpfulReactionCount: Int,
    val history: List<ProfileHistoryItemUiState>,
) {
    val isOwnProfile: Boolean
        get() = viewMode == ProfileViewMode.OWN
}

data class ProfileHistoryItemUiState(
    val reviewUuid: UUID,
    val placeUuid: UUID,
    val placeName: String,
    val locationHint: String,
    val timestampMillis: Long,
    val vibe: Int,
    val safety: Int,
    val accessibility: Int,
    val text: String?,
)
