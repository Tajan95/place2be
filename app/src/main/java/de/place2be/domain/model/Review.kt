package de.place2be.domain.model

import java.util.UUID

/**
 * Bewertung eines Ortes durch eine Nutzerin oder einen Nutzer.
 *
 * Die drei Kernkriterien stammen aus dem Meeting vom 12.07.2026:
 * Vibes, Sicherheit und Erreichbarkeit.
 */
data class Review(
    val uuid: UUID = UUID.randomUUID(),
    val placeUuid: UUID,
    val userUuid: UUID,
    val vibe: Int,
    val safety: Int,
    val accessibility: Int,
    val timestampMillis: Long,
    val likes: Int = 0,
    val dislikes: Int = 0,
) {
    val score: Double
        get() = (vibe + safety + accessibility) / NUMBER_OF_RATING_CRITERIA

    init {
        require(uuid.version() == RANDOM_UUID_VERSION) { "Review UUID must be a random UUID (version 4)." }
        require(placeUuid.version() == RANDOM_UUID_VERSION) { "Place UUID must be a random UUID (version 4)." }
        require(userUuid.version() == RANDOM_UUID_VERSION) { "User UUID must be a random UUID (version 4)." }
        require(vibe in RATING_RANGE) { "Vibe must be rated from 1 to 5." }
        require(safety in RATING_RANGE) { "Safety must be rated from 1 to 5." }
        require(accessibility in RATING_RANGE) { "Accessibility must be rated from 1 to 5." }
        require(timestampMillis >= 0) { "Timestamp must not be negative." }
        require(likes >= 0) { "Likes must not be negative." }
        require(dislikes >= 0) { "Dislikes must not be negative." }
    }

    private companion object {
        val RATING_RANGE = 1..5
        const val NUMBER_OF_RATING_CRITERIA = 3.0
        const val RANDOM_UUID_VERSION = 4
    }
}
