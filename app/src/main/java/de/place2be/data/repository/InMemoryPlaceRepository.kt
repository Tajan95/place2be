package de.place2be.data.repository

import de.place2be.data.mock.MockPlaceDataSource
import de.place2be.domain.model.Place
import de.place2be.domain.model.Review
import de.place2be.domain.repository.PlaceRepository
import java.util.UUID

/**
 * Einfache local-first Repository-Implementierung für den MVP.
 *
 * Diese Klasse kapselt Mock-Daten und lokal hinzugefügte Bewertungen. Später
 * kann sie durch Room, Firebase oder eine REST-Implementierung ersetzt werden,
 * ohne dass die Feature-UI direkt angepasst werden muss.
 */
class InMemoryPlaceRepository(
    mockPlaceDataSource: MockPlaceDataSource = MockPlaceDataSource(),
) : PlaceRepository {
    private val places = mockPlaceDataSource.places.toMutableList()
    private val reviews = mockPlaceDataSource.reviews.toMutableList()

    override fun getPlaces(): List<Place> = places.toList()

    override fun getPlace(placeUuid: UUID): Place? {
        return places.firstOrNull { it.uuid == placeUuid }
    }

    override fun getReviewsForPlace(placeUuid: UUID): List<Review> {
        return reviews.filter { it.placeUuid == placeUuid }
    }

    override fun addReview(review: Review) {
        reviews += review
    }
}
