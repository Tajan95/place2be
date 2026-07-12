package de.place2be.domain.repository

import de.place2be.domain.model.Place
import de.place2be.domain.model.Review
import java.util.UUID

/**
 * Abstraktion für den Zugriff auf Orte und Bewertungen.
 *
 * Die UI und Feature-ViewModels sollen gegen dieses Interface arbeiten,
 * nicht direkt gegen Mock-Daten oder eine konkrete spätere Datenquelle.
 */
interface PlaceRepository {
    fun getPlaces(): List<Place>

    fun getPlace(placeUuid: UUID): Place?

    fun getReviewsForPlace(placeUuid: UUID): List<Review>

    fun addReview(review: Review)
}
