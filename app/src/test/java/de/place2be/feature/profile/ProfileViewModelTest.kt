package de.place2be.feature.profile

import de.place2be.domain.model.Bookmark
import de.place2be.domain.model.Place
import de.place2be.domain.model.PlaceCategory
import de.place2be.domain.model.Review
import de.place2be.domain.model.ReviewReaction
import de.place2be.domain.model.ReviewReactionType
import de.place2be.domain.model.User
import de.place2be.domain.repository.PlaceRepository
import de.place2be.domain.repository.ReviewReactionRepository
import de.place2be.domain.repository.UserRepository
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileViewModelTest {
    @Test
    fun `own profile exposes complete history newest first`() {
        val olderReview = review(
            uuid = OLDER_REVIEW_UUID,
            placeUuid = PARK.uuid,
            userUuid = PROFILE_USER_UUID,
            timestampMillis = 1_000L,
            text = "Ruhiger Ort mit ausreichend langem Rezensionstext.",
            likes = 8,
            dislikes = 2,
        )
        val newerReview = review(
            uuid = NEWER_REVIEW_UUID,
            placeUuid = PROMENADE.uuid,
            userUuid = PROFILE_USER_UUID,
            timestampMillis = 2_000L,
            text = null,
            likes = 1,
            dislikes = 3,
        )
        val viewModel = viewModel(
            reviews = listOf(olderReview, newerReview, foreignReview()),
        )

        val profile = requireNotNull(
            viewModel.getProfile(
                profileUserUuid = PROFILE_USER_UUID,
                viewerUserUuid = PROFILE_USER_UUID,
            ),
        )

        assertTrue(profile.isOwnProfile)
        assertEquals(ProfileViewMode.OWN, profile.viewMode)
        assertEquals("MainEntdecker", profile.displayName)
        assertEquals("M", profile.profileInitial)
        assertEquals(2, profile.reviewCount)
        assertEquals(1, profile.textReviewCount)
        assertEquals(6, profile.helpfulReactionCount)
        assertEquals(listOf(NEWER_REVIEW_UUID, OLDER_REVIEW_UUID), profile.history.map { it.reviewUuid })
        assertEquals("Mainufer", profile.history.first().placeName)
        assertEquals("Bethmannpark", profile.history.last().placeName)
        assertTrue(profile.score.totalScore >= 0)
    }

    @Test
    fun `public profile keeps aggregates but never exposes chronological history`() {
        val reviews = listOf(
            review(
                uuid = OLDER_REVIEW_UUID,
                placeUuid = PARK.uuid,
                userUuid = PROFILE_USER_UUID,
                timestampMillis = 1_000L,
                text = "Eine öffentliche Rezension bleibt nur am Ort sichtbar.",
                likes = 12,
                dislikes = 2,
            ),
            review(
                uuid = NEWER_REVIEW_UUID,
                placeUuid = PROMENADE.uuid,
                userUuid = PROFILE_USER_UUID,
                timestampMillis = 2_000L,
                text = null,
                likes = 3,
                dislikes = 1,
            ),
        )
        val viewModel = viewModel(reviews)

        val profile = requireNotNull(
            viewModel.getProfile(
                profileUserUuid = PROFILE_USER_UUID,
                viewerUserUuid = VIEWER_USER_UUID,
            ),
        )

        assertFalse(profile.isOwnProfile)
        assertEquals(ProfileViewMode.PUBLIC, profile.viewMode)
        assertEquals(2, profile.reviewCount)
        assertEquals(1, profile.textReviewCount)
        assertEquals(12, profile.helpfulReactionCount)
        assertTrue(profile.history.isEmpty())
        assertTrue(profile.score.totalScore >= 0)
    }

    @Test
    fun `unknown profile user returns null`() {
        val profile = viewModel(emptyList()).getProfile(
            profileUserUuid = UUID.fromString("79934e17-922c-4c7f-a5e3-0aa4451b68af"),
            viewerUserUuid = PROFILE_USER_UUID,
        )

        assertNull(profile)
    }

    private fun viewModel(reviews: List<Review>) = ProfileViewModel(
        userRepository = FakeUserRepository(
            users = listOf(
                User(uuid = PROFILE_USER_UUID, displayName = "MainEntdecker"),
                User(uuid = VIEWER_USER_UUID, displayName = "Parkfreundin"),
            ),
        ),
        placeRepository = FakePlaceRepository(
            places = listOf(PARK, PROMENADE),
            reviews = reviews,
        ),
        reviewReactionRepository = FakeReactionRepository(),
    )

    private fun review(
        uuid: UUID,
        placeUuid: UUID,
        userUuid: UUID,
        timestampMillis: Long,
        text: String?,
        likes: Int,
        dislikes: Int,
    ) = Review(
        uuid = uuid,
        placeUuid = placeUuid,
        userUuid = userUuid,
        vibe = 4,
        safety = 4,
        accessibility = 4,
        timestampMillis = timestampMillis,
        text = text,
        likes = likes,
        dislikes = dislikes,
    )

    private fun foreignReview() = review(
        uuid = FOREIGN_REVIEW_UUID,
        placeUuid = PARK.uuid,
        userUuid = VIEWER_USER_UUID,
        timestampMillis = 3_000L,
        text = "Fremde Rezension",
        likes = 4,
        dislikes = 0,
    )

    private class FakePlaceRepository(
        private val places: List<Place>,
        private val reviews: List<Review>,
    ) : PlaceRepository {
        override fun getPlaces(): List<Place> = places

        override fun getPlace(placeUuid: UUID): Place? = places.firstOrNull { it.uuid == placeUuid }

        override fun getReview(reviewUuid: UUID): Review? = reviews.firstOrNull { it.uuid == reviewUuid }

        override fun getReviewsForPlace(placeUuid: UUID): List<Review> =
            reviews.filter { it.placeUuid == placeUuid }

        override fun getReviews(): List<Review> = reviews

        override fun addReview(review: Review) = Unit
    }

    private class FakeUserRepository(
        private val users: List<User>,
    ) : UserRepository {
        override fun getUser(userUuid: UUID): User? = users.firstOrNull { it.uuid == userUuid }

        override fun getBookmarkedPlaceUuids(userUuid: UUID): Set<UUID> = emptySet()

        override fun getBookmarks(userUuid: UUID): List<Bookmark> = emptyList()

        override fun isBookmarked(userUuid: UUID, placeUuid: UUID): Boolean = false

        override fun setBookmarked(userUuid: UUID, placeUuid: UUID, bookmarked: Boolean) = Unit
    }

    private class FakeReactionRepository : ReviewReactionRepository {
        override fun getReaction(reviewUuid: UUID, userUuid: UUID): ReviewReaction? = null

        override fun getReactionsForUser(userUuid: UUID): List<ReviewReaction> = emptyList()

        override fun toggleReaction(
            reviewUuid: UUID,
            userUuid: UUID,
            type: ReviewReactionType,
        ): ReviewReaction? = null
    }

    private companion object {
        val PROFILE_USER_UUID: UUID = UUID.fromString("f5257520-3685-4a1a-be5b-4a0ceb1baba7")
        val VIEWER_USER_UUID: UUID = UUID.fromString("914060c3-1f3f-4e20-ba70-1ed4ea2ed66b")
        val OLDER_REVIEW_UUID: UUID = UUID.fromString("576b5dd5-32f8-4e47-b4fc-8f78710c55de")
        val NEWER_REVIEW_UUID: UUID = UUID.fromString("7bb8494e-2fb4-4db4-94e7-48d7f62c4e6b")
        val FOREIGN_REVIEW_UUID: UUID = UUID.fromString("f2ead8ea-d8e2-4f9c-b8da-ff060f337c34")

        val PARK = Place(
            uuid = UUID.fromString("1cffbae7-e91c-4d1d-8212-c5f3e6b37fe6"),
            name = "Bethmannpark",
            category = PlaceCategory.PARK,
            description = "Ruhiger Park",
            locationHint = "Frankfurt am Main, Nordend",
            latitude = 50.1211,
            longitude = 8.6902,
        )
        val PROMENADE = Place(
            uuid = UUID.fromString("e31093f8-b56e-45f9-b0e3-6f93a8db517e"),
            name = "Mainufer",
            category = PlaceCategory.PROMENADE,
            description = "Promenade am Main",
            locationHint = "Frankfurt am Main, Mainufer",
            latitude = 50.1049,
            longitude = 8.6837,
        )
    }
}
