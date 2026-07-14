package de.place2be.domain.usecase

import de.place2be.domain.model.Review
import java.util.UUID

/**
 * Verhindert, dass dieselbe Person denselben Ort beliebig oft hintereinander
 * bewertet. Nach einer numerischen Bewertung gilt pro Nutzer und Ort eine
 * Sperrfrist von 24 Stunden – unabhängig davon, ob ein Rezensionstext vorliegt.
 */
class ReviewSubmissionCooldownPolicy(
    private val cooldownMillis: Long = DEFAULT_COOLDOWN_MILLIS,
) {
    fun evaluate(
        reviewsForPlace: List<Review>,
        userUuid: UUID,
        nowMillis: Long = System.currentTimeMillis(),
    ): ReviewSubmissionAvailability {
        val latestOwnReviewTimestamp = reviewsForPlace
            .asSequence()
            .filter { it.userUuid == userUuid }
            .maxOfOrNull(Review::timestampMillis)
            ?: return ReviewSubmissionAvailability.available()

        val remainingMillis = (latestOwnReviewTimestamp + cooldownMillis - nowMillis)
            .coerceAtLeast(0L)

        return ReviewSubmissionAvailability(
            canSubmit = remainingMillis == 0L,
            remainingMillis = remainingMillis,
        )
    }

    private companion object {
        const val DEFAULT_COOLDOWN_MILLIS = 24L * 60L * 60L * 1_000L
    }
}

data class ReviewSubmissionAvailability(
    val canSubmit: Boolean,
    val remainingMillis: Long,
) {
    companion object {
        fun available(): ReviewSubmissionAvailability = ReviewSubmissionAvailability(
            canSubmit = true,
            remainingMillis = 0L,
        )
    }
}
