package de.place2be.data.repository

import de.place2be.domain.model.Place
import de.place2be.domain.model.PlaceCategory
import de.place2be.domain.model.Review
import de.place2be.domain.model.ReviewReactionType
import de.place2be.domain.repository.PlaceRepository
import java.io.File
import java.nio.file.Files
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class MockReviewReactionRepositoryTest {
    private lateinit var storageDirectory: File
    private lateinit var placeRepository: MutablePlaceRepository
    private lateinit var repository: MockReviewReactionRepository

    @Before
    fun setUp() {
        storageDirectory = Files.createTempDirectory("place2be-reaction-test").toFile()
        placeRepository = MutablePlaceRepository(TEST_REVIEW)
        repository = MockReviewReactionRepository.create(storageDirectory, placeRepository)
    }

    @After
    fun tearDown() {
        storageDirectory.deleteRecursively()
    }

    @Test
    fun `same reaction toggles on and off without duplicate entries`() {
        val created = repository.toggleReaction(REVIEW_UUID, CURRENT_USER_UUID, ReviewReactionType.LIKE)

        assertEquals(ReviewReactionType.LIKE, created?.type)
        assertEquals(5, placeRepository.currentReview.likes)
        assertEquals(1, placeRepository.currentReview.dislikes)
        assertEquals(1, repository.getReactionsForUser(CURRENT_USER_UUID).size)

        val removed = repository.toggleReaction(REVIEW_UUID, CURRENT_USER_UUID, ReviewReactionType.LIKE)

        assertNull(removed)
        assertEquals(4, placeRepository.currentReview.likes)
        assertEquals(1, placeRepository.currentReview.dislikes)
        assertEquals(0, repository.getReactionsForUser(CURRENT_USER_UUID).size)
    }

    @Test
    fun `switching from like to dislike updates counters but preserves activity timestamp`() {
        val created = repository.toggleReaction(REVIEW_UUID, CURRENT_USER_UUID, ReviewReactionType.LIKE)
        val switched = repository.toggleReaction(REVIEW_UUID, CURRENT_USER_UUID, ReviewReactionType.DISLIKE)

        assertEquals(ReviewReactionType.DISLIKE, switched?.type)
        assertEquals(created?.createdAtMillis, switched?.createdAtMillis)
        assertEquals(4, placeRepository.currentReview.likes)
        assertEquals(2, placeRepository.currentReview.dislikes)
        assertEquals(1, repository.getReactionsForUser(CURRENT_USER_UUID).size)
    }

    @Test
    fun `reaction remains available after repository recreation`() {
        repository.toggleReaction(REVIEW_UUID, CURRENT_USER_UUID, ReviewReactionType.LIKE)

        val recreatedRepository = MockReviewReactionRepository.create(storageDirectory, placeRepository)

        assertEquals(
            ReviewReactionType.LIKE,
            recreatedRepository.getReaction(REVIEW_UUID, CURRENT_USER_UUID)?.type,
        )
    }

    @Test
    fun `users cannot react to their own review`() {
        assertThrows(IllegalArgumentException::class.java) {
            repository.toggleReaction(REVIEW_UUID, REVIEW_AUTHOR_UUID, ReviewReactionType.LIKE)
        }
    }

    private class MutablePlaceRepository(initialReview: Review) : PlaceRepository {
        var currentReview: Review = initialReview
            private set

        override fun getPlaces(): List<Place> = listOf(TEST_PLACE)

        override fun getPlace(placeUuid: UUID): Place? = TEST_PLACE.takeIf { it.uuid == placeUuid }

        override fun getReview(reviewUuid: UUID): Review? = currentReview.takeIf { it.uuid == reviewUuid }

        override fun getReviewsForPlace(placeUuid: UUID): List<Review> =
            listOf(currentReview).filter { it.placeUuid == placeUuid }

        override fun addReview(review: Review) {
            currentReview = review
        }

        override fun updateReview(review: Review): Boolean {
            if (review.uuid != currentReview.uuid) return false
            currentReview = review
            return true
        }
    }

    private companion object {
        val PLACE_UUID: UUID = UUID.fromString("e31093f8-b56e-45f9-b0e3-6f93a8db517e")
        val REVIEW_UUID: UUID = UUID.fromString("576b5dd5-32f8-4e47-b4fc-8f78710c55de")
        val REVIEW_AUTHOR_UUID: UUID = UUID.fromString("1ab06d2e-5c18-4cf5-8be2-d451591f5681")
        val CURRENT_USER_UUID: UUID = UUID.fromString("f5257520-3685-4a1a-be5b-4a0ceb1baba7")

        val TEST_PLACE = Place(
            uuid = PLACE_UUID,
            name = "Testort",
            category = PlaceCategory.PARK,
            description = "Testort für Reaktionen.",
            imageReference = null,
            latitude = 50.0,
            longitude = 8.0,
            locationHint = "Frankfurt am Main",
            attributes = emptySet(),
            initialScore = 3.0,
        )

        val TEST_REVIEW = Review(
            uuid = REVIEW_UUID,
            placeUuid = PLACE_UUID,
            userUuid = REVIEW_AUTHOR_UUID,
            vibe = 4,
            safety = 3,
            accessibility = 5,
            timestampMillis = 1_780_000_000_000L,
            text = "Hilfreiche Testrezension.",
            likes = 4,
            dislikes = 1,
        )
    }
}
