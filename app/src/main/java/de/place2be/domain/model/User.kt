package de.place2be.domain.model

import java.util.UUID

/**
 * Minimales Nutzer-Modell für den MVP.
 *
 * Für den local-first-Prototyp reicht ein einfacher Demo-User. Ein echtes
 * Authentifizierungs- oder Profil-System ist nicht Teil des MVP.
 */
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
