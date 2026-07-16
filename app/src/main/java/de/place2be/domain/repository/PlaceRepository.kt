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

    /**
     * Optionale gezielte Review-Suche. Bestehende alternative Implementierungen
     * bleiben durch den Default kompatibel; persistente Reaktions-Repositories
     * sollten diese Methode überschreiben.
     */
    fun getReview(reviewUuid: UUID): Review? = null

    fun getReviewsForPlace(placeUuid: UUID): List<Review>

    fun addReview(review: Review)

    /** Siehe [getReview]; Standardimplementierungen sind zunächst read-only. */
    fun updateReview(review: Review): Boolean = false
}
