package de.place2be.domain.model

import java.util.UUID

/**
 * Verknüpft einen Nutzer mit einem gespeicherten Ort.
 *
 * Das eigenständige Modell vermeidet eine eingebettete, duplizierte
 * Favoritenliste im User und lässt sich später direkt persistieren oder über
 * ein Backend synchronisieren.
 */
data class Bookmark(
    val uuid: UUID = UUID.randomUUID(),
    val userUuid: UUID,
    val placeUuid: UUID,
    val createdAtMillis: Long,
) {
    init {
        require(uuid.version() == RANDOM_UUID_VERSION) { "Bookmark UUID must be a random UUID (version 4)." }
        require(userUuid.version() == RANDOM_UUID_VERSION) { "User UUID must be a random UUID (version 4)." }
        require(placeUuid.version() == RANDOM_UUID_VERSION) { "Place UUID must be a random UUID (version 4)." }
        require(createdAtMillis >= 0) { "Creation timestamp must not be negative." }
    }

    private companion object {
        const val RANDOM_UUID_VERSION = 4
    }
}
