package de.place2be.feature.placeDetail

import de.place2be.domain.model.PlaceAttribute
import de.place2be.domain.repository.PlaceRepository
import de.place2be.domain.usecase.CalculatePlaceScoreUseCase
import java.util.UUID

class PlaceDetailViewModel(
    private val placeRepository: PlaceRepository,
    private val calculatePlaceScoreUseCase: CalculatePlaceScoreUseCase = CalculatePlaceScoreUseCase(),
) {
    fun getPlaceDetail(placeUuid: UUID): PlaceDetailUiState? {
        val place = placeRepository.getPlace(placeUuid) ?: return null
        val reviews = placeRepository.getReviewsForPlace(placeUuid)
        return PlaceDetailUiState(
            uuid = place.uuid,
            name = place.name,
            description = place.description,
            categoryLabel = place.category.name,
            locationHint = place.locationHint,
            currentScore = calculatePlaceScoreUseCase.calculate(
                reviews = reviews,
                fallbackScore = place.initialScore,
            ),
            attributes = place.attributes.toList(),
            reviewCount = reviews.size,
        )
    }
}

data class PlaceDetailUiState(
    val uuid: UUID,
    val name: String,
    val description: String,
    val categoryLabel: String,
    val locationHint: String,
    val currentScore: Double,
    val attributes: List<PlaceAttribute>,
    val reviewCount: Int,
)
