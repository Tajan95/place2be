package de.place2be.feature.rating

import de.place2be.data.repository.InMemoryPlaceRepository
import de.place2be.domain.model.Review
import de.place2be.domain.repository.PlaceRepository
import java.util.UUID

class RatingViewModel(
    private val placeRepository: PlaceRepository = InMemoryPlaceRepository(),
) {
    fun submitRating(
        placeUuid: UUID,
        userUuid: UUID,
        vibe: Int,
        safety: Int,
        accessibility: Int,
        text: String? = null,
    ) {
        val review = Review(
            placeUuid = placeUuid,
            userUuid = userUuid,
            vibe = vibe,
            safety = safety,
            accessibility = accessibility,
            timestampMillis = System.currentTimeMillis(),
            text = text,
        )
        placeRepository.addReview(review)
    }
}

data class RatingUiState(
    val vibe: Int = DEFAULT_RATING,
    val safety: Int = DEFAULT_RATING,
    val accessibility: Int = DEFAULT_RATING,
) {
    companion object {
        const val DEFAULT_RATING = 3
    }
}
