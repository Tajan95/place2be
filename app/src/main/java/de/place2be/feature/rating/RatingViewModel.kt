package de.place2be.feature.rating

import de.place2be.domain.model.Review
import de.place2be.domain.repository.PlaceRepository
import java.util.UUID

class RatingViewModel(
    private val placeRepository: PlaceRepository,
) {
    fun submitRating(
        placeUuid: UUID,
        userUuid: UUID,
        vibe: Int,
        safety: Int,
        accessibility: Int,
        text: String? = null,
    ): Review {
        val review = Review(
            placeUuid = placeUuid,
            userUuid = userUuid,
            vibe = vibe,
            safety = safety,
            accessibility = accessibility,
            timestampMillis = System.currentTimeMillis(),
            text = text?.trim()?.takeIf(String::isNotEmpty),
        )
        placeRepository.addReview(review)
        return review
    }
}

data class RatingUiState(
    val vibe: Int = DEFAULT_RATING,
    val safety: Int = DEFAULT_RATING,
    val accessibility: Int = DEFAULT_RATING,
    val reviewText: String = "",
) {
    val isSubmitEnabled: Boolean
        get() = vibe in RATING_RANGE &&
            safety in RATING_RANGE &&
            accessibility in RATING_RANGE &&
            reviewText.length <= MAX_REVIEW_TEXT_LENGTH

    companion object {
        const val DEFAULT_RATING = 3
        const val MAX_REVIEW_TEXT_LENGTH = 300
        val RATING_RANGE = 1..5
    }
}
