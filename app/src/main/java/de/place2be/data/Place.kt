package de.place2be.data

import java.util.UUID

data class Place(
    val uuid: UUID = UUID.randomUUID(),
    val name: String,
    val category: PlaceCategory,
    val description: String,
    val imageReference: String,
    val latitude: Double,
    val longitude: Double,
    val locationHint: String,
    val score: Double,
) {
    init {
        require(uuid.version() == RANDOM_UUID_VERSION) { "Place UUID must be a random UUID (version 4)." }
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90." }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180." }
        require(score in MIN_SCORE..MAX_SCORE) { "Score must be between $MIN_SCORE and $MAX_SCORE." }
    }

    private companion object {
        const val MIN_SCORE = 0.0
        const val MAX_SCORE = 5.0
        const val RANDOM_UUID_VERSION = 4
    }
}
