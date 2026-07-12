package de.place2be.feature.map

import de.place2be.data.repository.InMemoryPlaceRepository
import de.place2be.domain.model.Place
import de.place2be.domain.repository.PlaceRepository
import de.place2be.domain.usecase.CalculatePlaceScoreUseCase
import java.util.UUID

/**
 * Bereitet Daten für die Mock-Map auf.
 *
 * Diese Klasse ist bewusst als ViewModel-nahe Logik angelegt. Falls später die
 * AndroidX-ViewModel-Abhängigkeit ergänzt wird, kann sie zu einem echten
 * Lifecycle-ViewModel ausgebaut werden.
 */
class MapViewModel(
    private val placeRepository: PlaceRepository = InMemoryPlaceRepository(),
    private val calculatePlaceScoreUseCase: CalculatePlaceScoreUseCase = CalculatePlaceScoreUseCase(),
) {
    fun getMapItems(): List<MapPlaceUiState> {
        return placeRepository.getPlaces()
            .map { place -> place.toMapPlaceUiState() }
            .sortedByDescending { it.currentScore }
    }

    private fun Place.toMapPlaceUiState(): MapPlaceUiState {
        val reviews = placeRepository.getReviewsForPlace(uuid)
        return MapPlaceUiState(
            uuid = uuid,
            name = name,
            categoryLabel = category.name,
            locationHint = locationHint,
            currentScore = calculatePlaceScoreUseCase.calculate(
                reviews = reviews,
                fallbackScore = initialScore,
            ),
        )
    }
}

data class MapPlaceUiState(
    val uuid: UUID,
    val name: String,
    val categoryLabel: String,
    val locationHint: String,
    val currentScore: Double,
)
