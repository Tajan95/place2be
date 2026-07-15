package de.place2be.domain.usecase

import de.place2be.domain.model.Review
import kotlin.math.max

/**
 * Ergebnis der zeitlich gewichteten Ortsbewertung.
 *
 * Alle sichtbaren Werte stammen aus derselben Bewertungsmenge und verwenden
 * dieselbe Altersgewichtung. Dadurch bleiben Gesamtwert und Einzelkriterien
 * fachlich konsistent. Zusätzlich enthält das Ergebnis sowohl die gesamte
 * Bewertungsanzahl als auch den rollierenden Ein-Jahres-Ausschnitt für die UI.
 */
data class PlaceScoreResult(
    val overallScore: Double,
    val vibeScore: Double,
    val safetyScore: Double,
    val accessibilityScore: Double,
    val reviewCount: Int,
    val recentReviewCount: Int,
)

/**
 * Berechnet den aktuellen Score eines Ortes aus seinen Bewertungen.
 *
 * Neuere Bewertungen erhalten ein höheres Gewicht als ältere Bewertungen:
 *
 * `weight = 1 / (1 + ageInDays * decayFactorPerDay)`
 *
 * Alte Bewertungen verschwinden dadurch nicht vollständig, beeinflussen den
 * aktuellen Zustand eines Ortes aber schwächer. Die Logik liegt bewusst in der
 * Domain-Schicht und nicht in einem Screen oder einer Composable-Funktion.
 */
class CalculatePlaceScoreUseCase(
    private val decayFactorPerDay: Double = DEFAULT_DECAY_FACTOR_PER_DAY,
) {
    init {
        require(decayFactorPerDay >= 0.0) { "Decay factor must not be negative." }
    }

    fun calculate(
        reviews: List<Review>,
        fallbackScore: Double,
        nowTimestampMillis: Long = System.currentTimeMillis(),
    ): PlaceScoreResult {
        require(fallbackScore in SCORE_RANGE) { "Fallback score must be between 1 and 5." }
        require(nowTimestampMillis >= 0L) { "Current timestamp must not be negative." }

        val recentReviewCount = reviews.count { review ->
            review.timestampMillis in (nowTimestampMillis - ONE_YEAR_MILLIS)..nowTimestampMillis
        }

        if (reviews.isEmpty()) {
            return PlaceScoreResult(
                overallScore = fallbackScore,
                vibeScore = fallbackScore,
                safetyScore = fallbackScore,
                accessibilityScore = fallbackScore,
                reviewCount = 0,
                recentReviewCount = 0,
            )
        }

        var weightedVibeSum = 0.0
        var weightedSafetySum = 0.0
        var weightedAccessibilitySum = 0.0
        var weightSum = 0.0

        reviews.forEach { review ->
            val ageInDays = calculateAgeInDays(
                nowTimestampMillis = nowTimestampMillis,
                reviewTimestampMillis = review.timestampMillis,
            )
            val weight = calculateTimeWeight(ageInDays)

            weightedVibeSum += review.vibe * weight
            weightedSafetySum += review.safety * weight
            weightedAccessibilitySum += review.accessibility * weight
            weightSum += weight
        }

        if (weightSum == 0.0) {
            return PlaceScoreResult(
                overallScore = fallbackScore,
                vibeScore = fallbackScore,
                safetyScore = fallbackScore,
                accessibilityScore = fallbackScore,
                reviewCount = reviews.size,
                recentReviewCount = recentReviewCount,
            )
        }

        val vibeScore = weightedVibeSum / weightSum
        val safetyScore = weightedSafetySum / weightSum
        val accessibilityScore = weightedAccessibilitySum / weightSum
        val overallScore = (vibeScore + safetyScore + accessibilityScore) / NUMBER_OF_CRITERIA

        return PlaceScoreResult(
            overallScore = overallScore,
            vibeScore = vibeScore,
            safetyScore = safetyScore,
            accessibilityScore = accessibilityScore,
            reviewCount = reviews.size,
            recentReviewCount = recentReviewCount,
        )
    }

    private fun calculateAgeInDays(
        nowTimestampMillis: Long,
        reviewTimestampMillis: Long,
    ): Double {
        val ageMillis = max(0L, nowTimestampMillis - reviewTimestampMillis)
        return ageMillis / MILLIS_PER_DAY
    }

    private fun calculateTimeWeight(ageInDays: Double): Double =
        1.0 / (1.0 + ageInDays * decayFactorPerDay)

    private companion object {
        const val DEFAULT_DECAY_FACTOR_PER_DAY = 0.05
        const val MILLIS_PER_DAY = 86_400_000.0
        const val ONE_YEAR_MILLIS = 365L * 86_400_000L
        const val NUMBER_OF_CRITERIA = 3.0
        val SCORE_RANGE = 1.0..5.0
    }
}
