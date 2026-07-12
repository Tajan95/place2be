package de.place2be.domain.model

import java.util.UUID

/**
 * Fachliches Domain-Modell für einen öffentlichen Ort.
 *
 * Der aktuelle Score wird nicht dauerhaft als statischer Wert verstanden,
 * sondern kann über Reviews und den CalculatePlaceScoreUseCase berechnet
 * werden. initialScore dient nur als Fallback bzw. Startwert für Mock-Daten.
 */
data class Place(
    val uuid: UUID = UUID.randomUUID(),
    val name: String,
    val category: PlaceCategory,
    val description: String,
    val imageReference: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationHint: String,
    val attributes: Set<PlaceAttribute> = emptySet(),
    val initialScore: Double = DEFAULT_SCORE,
) {
    init {
        require(uuid.version() == RANDOM_UUID_VERSION) { "Place UUID must be a random UUID (version 4)." }
        require(name.isNotBlank()) { "Place name must not be blank." }
        require(description.isNotBlank()) { "Place description must not be blank." }
        require(locationHint.isNotBlank()) { "Location hint must not be blank." }
        latitude?.let { require(it in -90.0..90.0) { "Latitude must be between -90 and 90." } }
        longitude?.let { require(it in -180.0..180.0) { "Longitude must be between -180 and 180." } }
        require(initialScore in MIN_SCORE..MAX_SCORE) { "Initial score must be between $MIN_SCORE and $MAX_SCORE." }
    }

    private companion object {
        const val MIN_SCORE = 0.0
        const val MAX_SCORE = 5.0
        const val DEFAULT_SCORE = 3.0
        const val RANDOM_UUID_VERSION = 4
    }
}
