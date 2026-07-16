package de.place2be.data.repository

import de.place2be.data.mock.MockPlaceDataSource
import de.place2be.domain.model.Place
import de.place2be.domain.model.Review
import de.place2be.domain.repository.PlaceRepository
import java.util.UUID

/** Domain-Adapter fuer die zentrale JSON-Mock-Datenquelle. */
class MockPlaceRepository(
    private val dataSource: MockPlaceDataSource,
) : PlaceRepository {
    override fun getPlaces(): List<Place> = dataSource.getPlaces()

    override fun getPlace(placeUuid: UUID): Place? = dataSource.getPlace(placeUuid)

    override fun getReview(reviewUuid: UUID): Review? =
        dataSource.getReviews().firstOrNull { it.uuid == reviewUuid }

    override fun getReviewsForPlace(placeUuid: UUID): List<Review> =
        dataSource.getReviewsForPlace(placeUuid)

    override fun addReview(review: Review) {
        dataSource.createReview(review)
    }

    override fun updateReview(review: Review): Boolean = dataSource.updateReview(review)
}
