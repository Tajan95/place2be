package de.place2be.domain.model

import java.util.UUID

/**
 * Eine accountgebundene Reaktion auf eine Textrezension.
 *
 * Pro Nutzer und Rezension darf höchstens ein Eintrag existieren. Dadurch kann
 * die Anwendung Mehrfach-Likes verhindern, zwischen Like und Dislike wechseln
 * und eine Reaktion durch erneutes Antippen wieder entfernen.
 */
data class ReviewReaction(
    val uuid: UUID = UUID.randomUUID(),
    val reviewUuid: UUID,
    val userUuid: UUID,
    val type: ReviewReactionType,
    val createdAtMillis: Long,
) {
    init {
        require(uuid.version() == RANDOM_UUID_VERSION) {
            "ReviewReaction UUID must be a random UUID (version 4)."
        }
        require(reviewUuid.version() == RANDOM_UUID_VERSION) {
            "Review UUID must be a random UUID (version 4)."
        }
        require(userUuid.version() == RANDOM_UUID_VERSION) {
            "User UUID must be a random UUID (version 4)."
        }
        require(createdAtMillis >= 0L) { "Reaction timestamp must not be negative." }
    }

    private companion object {
        const val RANDOM_UUID_VERSION = 4
    }
}

enum class ReviewReactionType {
    LIKE,
    DISLIKE,
}
