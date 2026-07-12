package de.place2be.data

import java.util.UUID

data class Review(
    val uuid: UUID = UUID.randomUUID(),
    val placeUuid: UUID,
    val userUuid: UUID,
    val safety: Int,
    val accessibility: Int,
    val vibe: Int,
    val timestamp: Long,
    val likes: Int,
) {
    val score: Double
        get() = (safety + accessibility + vibe) / NUMBER_OF_RATING_CRITERIA

    init {
        require(uuid.version() == RANDOM_UUID_VERSION) { "Review UUID must be a random UUID (version 4)." }
        require(placeUuid.version() == RANDOM_UUID_VERSION) { "Place UUID must be a random UUID (version 4)." }
        require(userUuid.version() == RANDOM_UUID_VERSION) { "User UUID must be a random UUID (version 4)." }
        require(safety in RATING_RANGE) { "Safety must be rated from 1 to 5." }
        require(accessibility in RATING_RANGE) { "Accessibility must be rated from 1 to 5." }
        require(vibe in RATING_RANGE) { "Vibe must be rated from 1 to 5." }
        require(timestamp >= 0) { "Timestamp must not be negative." }
    }

    private companion object {
        val RATING_RANGE = 1..5
        const val NUMBER_OF_RATING_CRITERIA = 3.0
        const val RANDOM_UUID_VERSION = 4
    }
}
