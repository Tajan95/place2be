package de.place2be.domain.usecase

import de.place2be.domain.model.Place
import de.place2be.domain.model.PlaceCategory
import de.place2be.domain.model.Review
import de.place2be.domain.model.ReviewReaction
import de.place2be.domain.model.ReviewReactionType
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateUserScoreUseCaseTest {
    @Test
    fun `exploration bonus decreases over repeated rated visits and never drops below one`() {
        val reviews = (0 until 11).map { index ->
            review(
                uuid = REVIEW_UUIDS[index],
                placeUuid = PLACE_A.uuid,
                userUuid = CURRENT_USER_UUID,
                timestampMillis = BASE_TIME + index * 25L * MILLIS_PER_HOUR,
            )
        }

        val result = useCase().calculate(
            userUuid = CURRENT_USER_UUID,
            places = listOf(PLACE_A),
            reviews = reviews,
            reactions = emptyList(),
        )

        // 10 + 9 + ... + 1 für die ersten zehn Besuche, danach weiterhin 1.
        assertEquals(56, result.ratingActivityPoints)
        assertEquals(56, result.activityPoints)
    }

    @Test
    fun `nearby first visits receive cumulative penalty capped at ten points`() {
        val reviews = listOf(
            review(REVIEW_UUIDS[0], PLACE_A.uuid, CURRENT_USER_UUID, BASE_TIME),
            review(REVIEW_UUIDS[1], PLACE_NEAR_A.uuid, CURRENT_USER_UUID, BASE_TIME + MILLIS_PER_HOUR),
            review(REVIEW_UUIDS[2], PLACE_NEAR_B.uuid, CURRENT_USER_UUID, BASE_TIME + 2 * MILLIS_PER_HOUR),
        )

        val result = useCase().calculate(
            userUuid = CURRENT_USER_UUID,
            places = listOf(PLACE_A, PLACE_NEAR_A, PLACE_NEAR_B),
            reviews = reviews,
            reactions = emptyList(),
        )

        // Erster Ort 10, zweiter Ort 5, dritter Ort durch -10 und Untergrenze 1.
        assertEquals(16, result.ratingActivityPoints)
    }

    @Test
    fun `text bonus requires at least twenty trimmed characters`() {
        val reviews = listOf(
            review(
                REVIEW_UUIDS[0],
                PLACE_A.uuid,
                CURRENT_USER_UUID,
                BASE_TIME,
                text = "Genau zwanzig Zeichen!",
            ),
            review(
                REVIEW_UUIDS[1],
                PLACE_FAR.uuid,
                CURRENT_USER_UUID,
                BASE_TIME + 2 * MILLIS_PER_DAY,
                text = "Zu kurz",
            ),
        )

        val result = useCase().calculate(
            userUuid = CURRENT_USER_UUID,
            places = listOf(PLACE_A, PLACE_FAR),
            reviews = reviews,
            reactions = emptyList(),
        )

        assertEquals(2, result.textReviewActivityPoints)
        assertEquals(
            result.ratingActivityPoints + result.textReviewActivityPoints + result.reactionActivityPoints,
            result.activityPoints,
        )
    }

    @Test
    fun `reaction activity is capped per place and per day`() {
        val foreignReviews = (0 until 11).map { index ->
            val placeUuid = when {
                index < 4 -> PLACE_A.uuid
                index < 8 -> PLACE_FAR.uuid
                else -> PLACE_THIRD.uuid
            }
            review(
                uuid = REVIEW_UUIDS[index],
                placeUuid = placeUuid,
                userUuid = OTHER_USER_UUID,
                timestampMillis = BASE_TIME - MILLIS_PER_DAY,
                text = "Fremde Rezension Nummer $index mit ausreichend Text.",
            )
        }
        val reactions = foreignReviews.mapIndexed { index, review ->
            ReviewReaction(
                uuid = REACTION_UUIDS[index],
                reviewUuid = review.uuid,
                userUuid = CURRENT_USER_UUID,
                type = if (index % 2 == 0) ReviewReactionType.LIKE else ReviewReactionType.DISLIKE,
                createdAtMillis = if (index < 8) BASE_TIME else BASE_TIME + MILLIS_PER_DAY,
            )
        }

        val result = useCase().calculate(
            userUuid = CURRENT_USER_UUID,
            places = listOf(PLACE_A, PLACE_FAR, PLACE_THIRD),
            reviews = foreignReviews,
            reactions = reactions,
        )

        // Tag 1: maximal drei pro Ort und sechs pro Tag. Tag 2: drei am dritten Ort.
        assertEquals(9, result.reactionActivityPoints)
        assertEquals(9, result.activityPoints)
    }

    @Test
    fun `busy places reduce reputation while quiet places receive moderate boost`() {
        val ownQuietReview = review(
            uuid = REVIEW_UUIDS[0],
            placeUuid = PLACE_A.uuid,
            userUuid = CURRENT_USER_UUID,
            timestampMillis = BASE_TIME - MILLIS_PER_DAY,
            text = "Eine hilfreiche ausführliche Rezension.",
            likes = 11,
            dislikes = 1,
        )
        val quietResult = useCase().calculate(
            userUuid = CURRENT_USER_UUID,
            places = listOf(PLACE_A),
            reviews = listOf(ownQuietReview),
            reactions = emptyList(),
        )

        val ownBusyReview = ownQuietReview.copy(
            uuid = REVIEW_UUIDS[1],
            placeUuid = PLACE_FAR.uuid,
        )
        val busyCommunityReviews = (2 until 11).flatMap { group ->
            (0 until 11).map { offset ->
                review(
                    uuid = UUID.fromString(
                        "${(group + 1).toString(16).padStart(8, '0')}-1234-4abc-8def-${(offset + 1).toString().padStart(12, '0')}",
                    ),
                    placeUuid = PLACE_FAR.uuid,
                    userUuid = OTHER_USER_UUID,
                    timestampMillis = BASE_TIME - (offset + 1) * MILLIS_PER_HOUR,
                )
            }
        }
        val busyResult = useCase().calculate(
            userUuid = CURRENT_USER_UUID,
            places = listOf(PLACE_FAR),
            reviews = listOf(ownBusyReview) + busyCommunityReviews,
            reactions = emptyList(),
        )

        assertEquals(9, quietResult.reputationPoints)
        assertEquals(5, busyResult.reputationPoints)
        assertTrue(quietResult.reputationPoints > busyResult.reputationPoints)
    }

    @Test
    fun `reputation per review is capped at ten points and total score is additive`() {
        val review = review(
            uuid = REVIEW_UUIDS[0],
            placeUuid = PLACE_A.uuid,
            userUuid = CURRENT_USER_UUID,
            timestampMillis = BASE_TIME,
            text = "Diese Rezension hat sehr viele positive Reaktionen erhalten.",
            likes = 10_000,
            dislikes = 0,
        )

        val result = useCase().calculate(
            userUuid = CURRENT_USER_UUID,
            places = listOf(PLACE_A),
            reviews = listOf(review),
            reactions = emptyList(),
        )

        assertEquals(10, result.reputationPoints)
        assertEquals(result.activityPoints + result.reputationPoints, result.totalScore)
    }

    private fun useCase() = CalculateUserScoreUseCase(nowMillisProvider = { BASE_TIME })

    private fun review(
        uuid: UUID,
        placeUuid: UUID,
        userUuid: UUID,
        timestampMillis: Long,
        text: String? = null,
        likes: Int = 0,
        dislikes: Int = 0,
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

    private companion object {
        const val MILLIS_PER_HOUR = 60L * 60L * 1_000L
        const val MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR
        const val BASE_TIME = 1_780_000_000_000L

        val CURRENT_USER_UUID: UUID = UUID.fromString("f5257520-3685-4a1a-be5b-4a0ceb1baba7")
        val OTHER_USER_UUID: UUID = UUID.fromString("1ab06d2e-5c18-4cf5-8be2-d451591f5681")

        val PLACE_A = place("e31093f8-b56e-45f9-b0e3-6f93a8db517e", 50.0000, 8.0000)
        val PLACE_NEAR_A = place("1aeb7943-2b0b-49d2-947e-8a4d7a893a9d", 50.0003, 8.0000)
        val PLACE_NEAR_B = place("914e24f8-3d81-4595-8094-309f8b96e89e", 50.0006, 8.0000)
        val PLACE_FAR = place("8683bea8-9758-4e07-8ba1-152e10f81159", 50.0200, 8.0200)
        val PLACE_THIRD = place("bb7970a1-b7ec-4aa4-80c0-75a82fbc46b2", 50.0400, 8.0400)

        val REVIEW_UUIDS = listOf(
            "576b5dd5-32f8-4e47-b4fc-8f78710c55de",
            "7bb8494e-2fb4-4db4-94e7-48d7f62c4e6b",
            "f2ead8ea-d8e2-4f9c-b8da-ff060f337c34",
            "9fe25879-fc1d-4318-a9ad-030a0c468095",
            "7ca8e69b-486e-489e-b1ef-4660df77a7c5",
            "e1e6a3c5-f8f6-40db-a57d-0f36d3b3ac32",
            "8ce943b5-3770-4dc5-b14a-0d227fdc6035",
            "6fba117d-34b9-46b5-a11c-fcbcd3925f72",
            "0248c9d0-9e18-40db-b08b-a3304521146b",
            "063e05ec-d267-4796-a743-8a9392a59f19",
            "a6211f92-9171-4e03-93e0-ff06df126752",
        ).map(UUID::fromString)

        val REACTION_UUIDS = listOf(
            "ab6df358-8fe0-4e76-a001-167bbb79461f",
            "b0051c24-b88b-445a-b5b2-3f709bf5714a",
            "6dd2ce4a-c59f-4e45-93d8-690b8ef59c57",
            "8db26451-56c6-4105-ad6e-b7f408f89210",
            "d24a4ae8-ff98-4384-9e5f-bdd1675d8cae",
            "e4e7d793-4364-4a7d-b9ed-150e477c8595",
            "1d457a9c-aeda-43b7-85f0-690dccd45d85",
            "82f2c216-79c6-4db8-a34b-7aec851fe784",
            "a4f24aca-5904-43a6-b615-460aa3e9cd7e",
            "b16a7db4-fab7-426d-9457-a52bcefd853d",
            "b99b1c15-ec38-4390-b0e8-fe73405aa72f",
        ).map(UUID::fromString)

        fun place(uuid: String, latitude: Double, longitude: Double) = Place(
            uuid = UUID.fromString(uuid),
            name = "Testort $uuid",
            category = PlaceCategory.PARK,
            description = "Ein Ort für Nutzer-Score-Tests.",
            latitude = latitude,
            longitude = longitude,
            locationHint = "Frankfurt am Main",
        )
    }
}
