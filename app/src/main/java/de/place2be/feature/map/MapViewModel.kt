package de.place2be.feature.map

import de.place2be.core.location.LocationConfirmationState
import de.place2be.core.location.RatingEligibilityPolicy
import de.place2be.domain.model.Place
import de.place2be.domain.model.PlaceAttribute
import de.place2be.domain.model.PlaceCategory
import de.place2be.domain.repository.PlaceRepository
import de.place2be.domain.repository.UserRepository
import de.place2be.domain.usecase.CalculatePlaceScoreUseCase
import java.util.UUID

/**
 * Bereitet die Domain-Daten fuer eine beliebig austauschbare Kartenoberflaeche auf.
 * Weder die Mock-Map noch eine spaetere echte Kartenintegration greifen direkt
 * auf die lokale Datenquelle zu.
 */
class MapViewModel(
    private val placeRepository: PlaceRepository,
    private val userRepository: UserRepository,
    private val calculatePlaceScoreUseCase: CalculatePlaceScoreUseCase = CalculatePlaceScoreUseCase(),
    private val ratingEligibilityPolicy: RatingEligibilityPolicy = RatingEligibilityPolicy(),
    private val currentUserUuid: UUID = DEMO_USER_UUID,
    private val locationConfirmationState: LocationConfirmationState = LocationConfirmationState.NOT_REQUESTED,
    private val onSiteSinceTimestampMillis: Long? = null,
) {
    fun getMapItems(): List<MapPlaceUiState> {
        val places = placeRepository.getPlaces()
        val bookmarksByPlaceUuid = userRepository.getBookmarks(currentUserUuid)
            .associateBy { it.placeUuid }
        val coordinateBounds = CoordinateBounds.from(places)

        return places
            .map { place ->
                place.toMapPlaceUiState(
                    coordinateBounds = coordinateBounds,
                    bookmarkedAtMillis = bookmarksByPlaceUuid[place.uuid]?.createdAtMillis,
                )
            }
            .sortedByDescending(MapPlaceUiState::currentScore)
    }

    private fun Place.toMapPlaceUiState(
        coordinateBounds: CoordinateBounds,
        bookmarkedAtMillis: Long?,
    ): MapPlaceUiState {
        val reviews = placeRepository.getReviewsForPlace(uuid)
        val scoreResult = calculatePlaceScoreUseCase.calculate(
            reviews = reviews,
            fallbackScore = initialScore,
        )
        val ratingEligibility = ratingEligibilityPolicy.evaluate(
            locationConfirmationState = locationConfirmationState,
            onSiteSinceTimestampMillis = onSiteSinceTimestampMillis,
        )

        return MapPlaceUiState(
            uuid = uuid,
            name = name,
            description = description,
            category = category,
            categoryLabel = category.toDisplayLabel(),
            locationHint = locationHint,
            attributes = attributes,
            currentScore = scoreResult.overallScore,
            vibeScore = scoreResult.vibeScore,
            safetyScore = scoreResult.safetyScore,
            accessibilityScore = scoreResult.accessibilityScore,
            mapXFraction = coordinateBounds.xFraction(longitude),
            mapYFraction = coordinateBounds.yFraction(latitude),
            bookmarkedAtMillis = bookmarkedAtMillis,
            canRate = ratingEligibility.canRate,
            ratingEligibilityMessage = ratingEligibility.helperText,
        )
    }

    private data class CoordinateBounds(
        val minLatitude: Double,
        val maxLatitude: Double,
        val minLongitude: Double,
        val maxLongitude: Double,
    ) {
        fun xFraction(longitude: Double?): Float {
            if (longitude == null || maxLongitude == minLongitude) return 0.5f
            return NORMALIZED_START +
                ((longitude - minLongitude) / (maxLongitude - minLongitude)).toFloat() * NORMALIZED_RANGE
        }

        fun yFraction(latitude: Double?): Float {
            if (latitude == null || maxLatitude == minLatitude) return 0.5f
            return NORMALIZED_START +
                ((maxLatitude - latitude) / (maxLatitude - minLatitude)).toFloat() * NORMALIZED_RANGE
        }

        companion object {
            private const val NORMALIZED_START = 0.14f
            private const val NORMALIZED_RANGE = 0.72f

            fun from(places: List<Place>): CoordinateBounds {
                val latitudes = places.mapNotNull(Place::latitude)
                val longitudes = places.mapNotNull(Place::longitude)
                return CoordinateBounds(
                    minLatitude = latitudes.minOrNull() ?: 0.0,
                    maxLatitude = latitudes.maxOrNull() ?: 1.0,
                    minLongitude = longitudes.minOrNull() ?: 0.0,
                    maxLongitude = longitudes.maxOrNull() ?: 1.0,
                )
            }
        }
    }

    private fun PlaceCategory.toDisplayLabel(): String = when (this) {
        PlaceCategory.PARK -> "Park"
        PlaceCategory.SQUARE -> "Platz"
        PlaceCategory.PROMENADE -> "Promenade"
        PlaceCategory.SHOPPING_STREET -> "Einkaufsmeile"
        PlaceCategory.SHOPPING_CENTER -> "Einkaufszentrum"
        PlaceCategory.DISTRICT -> "Stadtviertel"
        PlaceCategory.OTHER_PUBLIC_PLACE -> "Öffentlicher Ort"
    }

    private companion object {
        val DEMO_USER_UUID: UUID = UUID.fromString("f5257520-3685-4a1a-be5b-4a0ceb1baba7")
    }
}

data class MapPlaceUiState(
    val uuid: UUID,
    val name: String,
    val description: String,
    val category: PlaceCategory,
    val categoryLabel: String,
    val locationHint: String,
    val attributes: Set<PlaceAttribute>,
    val currentScore: Double,
    val vibeScore: Double,
    val safetyScore: Double,
    val accessibilityScore: Double,
    val mapXFraction: Float,
    val mapYFraction: Float,
    val bookmarkedAtMillis: Long?,
    val canRate: Boolean,
    val ratingEligibilityMessage: String,
) {
    val isBookmarked: Boolean
        get() = bookmarkedAtMillis != null
}

internal data class MapFilterSelection(
    val categoryNames: Set<String> = emptySet(),
    val attributeNames: Set<String> = emptySet(),
)

internal fun List<MapPlaceUiState>.filteredBy(selection: MapFilterSelection): List<MapPlaceUiState> =
    filter { place ->
        (selection.categoryNames.isEmpty() || place.category.name in selection.categoryNames) &&
            (selection.attributeNames.isEmpty() || selection.attributeNames.all { selected ->
                place.attributes.any { it.name == selected }
            })
    }

internal fun List<MapPlaceUiState>.popularFirst(): List<MapPlaceUiState> =
    sortedByDescending(MapPlaceUiState::currentScore)

internal fun List<MapPlaceUiState>.newestBookmarksFirst(): List<MapPlaceUiState> =
    filter(MapPlaceUiState::isBookmarked).sortedByDescending { it.bookmarkedAtMillis }
