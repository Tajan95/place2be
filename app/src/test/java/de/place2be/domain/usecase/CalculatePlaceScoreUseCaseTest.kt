package de.place2be.domain.usecase

import de.place2be.domain.model.Review
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatePlaceScoreUseCaseTest {
    private val useCase = CalculatePlaceScoreUseCase()

    @Test
    fun `returns fallback values when no reviews exist`() {
        val result = useCase.calculate(
            reviews = emptyList(),
            fallbackScore = 3.6,
            nowTimestampMillis = NOW,
        )

        assertEquals(3.6, result.overallScore, DELTA)
        assertEquals(3.6, result.vibeScore, DELTA)
        assertEquals(3.6, result.safetyScore, DELTA)
        assertEquals(3.6, result.accessibilityScore, DELTA)
        assertEquals(0, result.reviewCount)
        assertEquals(0, result.recentReviewCount)
    }

    @Test
    fun `newer review influences all scores more strongly than older review`() {
        val oldReview = review(
            vibe = 1,
            safety = 1,
            accessibility = 1,
            timestampMillis = NOW - 30 * MILLIS_PER_DAY,
        )
        val currentReview = review(
            vibe = 5,
            safety = 5,
            accessibility = 5,
            timestampMillis = NOW,
        )

        val result = useCase.calculate(
            reviews = listOf(oldReview, currentReview),
            fallbackScore = 3.0,
            nowTimestampMillis = NOW,
        )

        // Ohne Zeitgewichtung läge der Mittelwert exakt bei 3,0. Der aktuelle
        // positive Eindruck muss den sichtbaren Score daher über 3,0 heben.
        assertTrue(result.overallScore > 3.0)
        assertEquals(result.overallScore, result.vibeScore, DELTA)
        assertEquals(result.overallScore, result.safetyScore, DELTA)
        assertEquals(result.overallScore, result.accessibilityScore, DELTA)
        assertEquals(2, result.reviewCount)
        assertEquals(2, result.recentReviewCount)
    }

    @Test
    fun `criteria are weighted independently and overall score is their average`() {
        val oldReview = review(
            vibe = 1,
            safety = 5,
            accessibility = 2,
            timestampMillis = NOW - 30 * MILLIS_PER_DAY,
        )
        val currentReview = review(
            vibe = 5,
            safety = 1,
            accessibility = 4,
            timestampMillis = NOW,
        )

        val result = useCase.calculate(
            reviews = listOf(oldReview, currentReview),
            fallbackScore = 3.0,
            nowTimestampMillis = NOW,
        )

        assertTrue(result.vibeScore > 3.0)
        assertTrue(result.safetyScore < 3.0)
        assertTrue(result.accessibilityScore > 3.0)
        assertEquals(
            (result.vibeScore + result.safetyScore + result.accessibilityScore) / 3.0,
            result.overallScore,
            DELTA,
        )
    }

    @Test
    fun `recent review count uses a rolling one-year window`() {
        val currentReview = review(
            vibe = 4,
            safety = 4,
            accessibility = 4,
            timestampMillis = NOW,
        )
        val almostOneYearOldReview = review(
            vibe = 3,
            safety = 3,
            accessibility = 3,
            timestampMillis = NOW - 364 * MILLIS_PER_DAY,
        )
        val olderThanOneYearReview = review(
            vibe = 5,
            safety = 5,
            accessibility = 5,
            timestampMillis = NOW - 366 * MILLIS_PER_DAY,
        )

        val result = useCase.calculate(
            reviews = listOf(currentReview, almostOneYearOldReview, olderThanOneYearReview),
            fallbackScore = 3.0,
            nowTimestampMillis = NOW,
        )

        assertEquals(3, result.reviewCount)
        assertEquals(2, result.recentReviewCount)
    }

    private fun review(
        vibe: Int,
        safety: Int,
        accessibility: Int,
        timestampMillis: Long,
    ): Review = Review(
        uuid = UUID.randomUUID(),
        placeUuid = PLACE_UUID,
        userUuid = UUID.randomUUID(),
        vibe = vibe,
        safety = safety,
        accessibility = accessibility,
        timestampMillis = timestampMillis,
    )

    private companion object {
        const val NOW = 1_800_000_000_000L
        const val MILLIS_PER_DAY = 86_400_000L
        const val DELTA = 0.0001
        val PLACE_UUID: UUID = UUID.fromString("11111111-1111-4111-8111-111111111111")
    }
}
