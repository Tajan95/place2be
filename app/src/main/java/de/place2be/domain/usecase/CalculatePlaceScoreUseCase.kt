package de.place2be.domain.usecase

import de.place2be.domain.model.Review
import kotlin.math.max

/**
 * Berechnet den aktuellen Score eines Ortes aus seinen Bewertungen.
 *
 * Wichtige Architekturentscheidung:
 * Die Score-Logik liegt bewusst in der Domain-Schicht und nicht in einem
 * Screen oder einer Composable-Funktion. Dadurch bleibt die Logik testbar,
 * wiederverwendbar und in der Präsentation klar erklärbar.
 */
class CalculatePlaceScoreUseCase(
    private val decayFactorPerDay: Double = DEFAULT_DECAY_FACTOR_PER_DAY,
) {
    fun calculate(
        reviews: List<Review>,
        fallbackScore: Double,
        nowTimestampMillis: Long = System.currentTimeMillis(),
    ): Double {
        if (reviews.isEmpty()) return fallbackScore

        var weightedScoreSum = 0.0
        var weightSum = 0.0

        reviews.forEach { review ->
            val ageInDays = calculateAgeInDays(
                nowTimestampMillis = nowTimestampMillis,
                reviewTimestampMillis = review.timestampMillis,
            )
            val weight = calculateTimeWeight(ageInDays)
            weightedScoreSum += review.score * weight
            weightSum += weight
        }

        return if (weightSum == 0.0) fallbackScore else weightedScoreSum / weightSum
    }

    private fun calculateAgeInDays(
        nowTimestampMillis: Long,
        reviewTimestampMillis: Long,
    ): Double {
        val ageMillis = max(0L, nowTimestampMillis - reviewTimestampMillis)
        return ageMillis / MILLIS_PER_DAY
    }

    private fun calculateTimeWeight(ageInDays: Double): Double {
        return 1.0 / (1.0 + ageInDays * decayFactorPerDay)
    }

    private companion object {
        const val DEFAULT_DECAY_FACTOR_PER_DAY = 0.05
        const val MILLIS_PER_DAY = 86_400_000.0
    }
}
