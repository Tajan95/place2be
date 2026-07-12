package de.place2be.data

import java.util.UUID

data class User(
    val uuid: UUID = UUID.randomUUID(),
    val displayName: String,
) {
    init {
        require(uuid.version() == RANDOM_UUID_VERSION) { "User UUID must be a random UUID (version 4)." }
        require(displayName.isNotBlank()) { "Display name must not be blank." }
    }

    private companion object {
        const val RANDOM_UUID_VERSION = 4
    }
}
