package de.place2be.data.mock

import com.google.gson.Gson
import de.place2be.domain.model.Review
import java.io.File
import java.nio.file.Files
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockPlaceDataSourceReviewTextRetentionTest {
    private val gson = Gson()
    private lateinit var storageDirectory: File

    @Before
    fun setUp() {
        storageDirectory = Files.createTempDirectory("place2be-review-retention").toFile()
    }

    @After
    fun tearDown() {
        storageDirectory.deleteRecursively()
    }

    @Test
    fun `creating the fifty first newer text review removes only the oldest text`() {
        val firstPlaceSeedReviews = (1L..50L).map { timestamp ->
            review(
                placeUuid = FIRST_PLACE_UUID,
                timestampMillis = timestamp,
                text = "Rezension $timestamp",
            )
        }
        val secondPlaceReview = review(
            placeUuid = SECOND_PLACE_UUID,
            timestampMillis = 1L,
            text = "Text am zweiten Ort",
        )
        val seedReviews = firstPlaceSeedReviews + secondPlaceReview
        val seedContentProvider = seedContentProvider(seedReviews)
        val dataSource = MockPlaceDataSource.create(storageDirectory, seedContentProvider)

        val newestReview = review(
            placeUuid = FIRST_PLACE_UUID,
            timestampMillis = 51L,
            text = "Neueste Rezension",
        )
        val persistedNewestReview = dataSource.createReview(newestReview)

        assertEquals(newestReview, persistedNewestReview)

        val storedFirstPlaceReviews = dataSource.getReviewsForPlace(FIRST_PLACE_UUID)
        assertEquals(51, storedFirstPlaceReviews.size)
        assertEquals(50, storedFirstPlaceReviews.count { it.text != null })

        val oldestSeedReview = firstPlaceSeedReviews.first()
        val storedOldestReview = storedFirstPlaceReviews.single { it.uuid == oldestSeedReview.uuid }
        assertEquals(oldestSeedReview.copy(text = null), storedOldestReview)
        assertNull(storedOldestReview.text)

        val storedNewestReview = storedFirstPlaceReviews.single { it.uuid == newestReview.uuid }
        assertEquals(newestReview, storedNewestReview)

        val storedSecondPlaceReview = dataSource.getReviewsForPlace(SECOND_PLACE_UUID).single()
        assertEquals(secondPlaceReview, storedSecondPlaceReview)
        assertEquals("Text am zweiten Ort", storedSecondPlaceReview.text)

        val recreatedDataSource = MockPlaceDataSource.create(storageDirectory, seedContentProvider)
        val recreatedOldestReview = recreatedDataSource
            .getReviewsForPlace(FIRST_PLACE_UUID)
            .single { it.uuid == oldestSeedReview.uuid }
        assertEquals(oldestSeedReview.copy(text = null), recreatedOldestReview)
        assertEquals(50, recreatedDataSource.getReviewsForPlace(FIRST_PLACE_UUID).count { it.text != null })
        assertEquals(52, recreatedDataSource.getReviews().size)
    }

    @Test
    fun `existing storage with more than fifty texts is normalized on initialization`() {
        val legacyReviews = (1L..51L).map { timestamp ->
            review(
                placeUuid = FIRST_PLACE_UUID,
                timestampMillis = timestamp,
                text = "Legacy-Rezension $timestamp",
            )
        }

        val dataSource = MockPlaceDataSource.create(
            storageDirectory = storageDirectory,
            seedContentProvider = seedContentProvider(legacyReviews),
        )
        val storedReviews = dataSource.getReviewsForPlace(FIRST_PLACE_UUID)

        assertEquals(51, storedReviews.size)
        assertEquals(50, storedReviews.count { it.text != null })
        assertEquals(
            legacyReviews.first().copy(text = null),
            storedReviews.single { it.uuid == legacyReviews.first().uuid },
        )
        assertTrue(storedReviews.drop(1).all { it.text != null })
    }

    private fun seedContentProvider(seedReviews: List<Review>): (String) -> String = { fileName ->
        when (fileName) {
            "places.json" -> PLACES_JSON
            "reviews.json" -> gson.toJson(seedReviews.map { review -> SeedReviewJson.from(review) })
            "users.json" -> USERS_JSON
            "bookmarks.json" -> "[]"
            else -> error("Unexpected seed file: $fileName")
        }
    }

    private fun review(
        placeUuid: UUID,
        timestampMillis: Long,
        text: String,
    ): Review = Review(
        uuid = UUID.randomUUID(),
        placeUuid = placeUuid,
        userUuid = USER_UUID,
        vibe = 4,
        safety = 3,
        accessibility = 2,
        timestampMillis = timestampMillis,
        text = text,
        likes = timestampMillis.toInt(),
        dislikes = 1,
    )

    private data class SeedReviewJson(
        val uuid: String,
        val placeUuid: String,
        val userUuid: String,
        val vibe: Int,
        val safety: Int,
        val accessibility: Int,
        val timestampMillis: Long,
        val text: String?,
        val likes: Int,
        val dislikes: Int,
    ) {
        companion object {
            fun from(review: Review): SeedReviewJson = SeedReviewJson(
                uuid = review.uuid.toString(),
                placeUuid = review.placeUuid.toString(),
                userUuid = review.userUuid.toString(),
                vibe = review.vibe,
                safety = review.safety,
                accessibility = review.accessibility,
                timestampMillis = review.timestampMillis,
                text = review.text,
                likes = review.likes,
                dislikes = review.dislikes,
            )
        }
    }

    private companion object {
        val FIRST_PLACE_UUID: UUID = UUID.fromString("11111111-1111-4111-8111-111111111111")
        val SECOND_PLACE_UUID: UUID = UUID.fromString("22222222-2222-4222-8222-222222222222")
        val USER_UUID: UUID = UUID.fromString("33333333-3333-4333-8333-333333333333")

        val PLACES_JSON = """
            [
              {
                "uuid": "$FIRST_PLACE_UUID",
                "name": "Erster Testort",
                "category": "PARK",
                "description": "Testort fuer die Textaufbewahrung",
                "imageReference": null,
                "latitude": 50.1,
                "longitude": 8.6,
                "locationHint": "Frankfurt am Main",
                "attributes": [],
                "initialScore": 3.0
              },
              {
                "uuid": "$SECOND_PLACE_UUID",
                "name": "Zweiter Testort",
                "category": "PROMENADE",
                "description": "Kontrollort fuer die ortsweise Begrenzung",
                "imageReference": null,
                "latitude": 50.2,
                "longitude": 8.7,
                "locationHint": "Frankfurt am Main",
                "attributes": [],
                "initialScore": 3.0
              }
            ]
        """.trimIndent()

        val USERS_JSON = """
            [
              {
                "uuid": "$USER_UUID",
                "displayName": "Testnutzer",
                "userScore": 0
              }
            ]
        """.trimIndent()
    }
}
