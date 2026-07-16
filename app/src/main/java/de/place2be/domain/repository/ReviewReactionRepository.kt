package de.place2be.domain.repository

import de.place2be.domain.model.ReviewReaction
import de.place2be.domain.model.ReviewReactionType
import java.util.UUID

/**
 * Persistiert accountgebundene Reaktionen auf Rezensionen.
 *
 * `toggleReaction` bildet die gesamte MVP-Interaktion ab:
 * - keine Reaktion + Antippen -> Reaktion anlegen
 * - gleiche Reaktion erneut antippen -> Reaktion entfernen
 * - andere Reaktion antippen -> zwischen Like und Dislike wechseln
 */
interface ReviewReactionRepository {
    fun getReaction(reviewUuid: UUID, userUuid: UUID): ReviewReaction?

    fun getReactionsForUser(userUuid: UUID): List<ReviewReaction>

    fun toggleReaction(
        reviewUuid: UUID,
        userUuid: UUID,
        type: ReviewReactionType,
    ): ReviewReaction?
}
