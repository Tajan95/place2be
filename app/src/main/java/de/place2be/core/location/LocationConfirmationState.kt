package de.place2be.core.location

/**
 * Beschreibt den Zustand der Standortbestätigung im MVP.
 *
 * Für die Demo kann die Standortbestätigung simuliert werden. Die Zustände
 * halten trotzdem fest, dass echte Berechtigungen und echte Standortprüfung
 * fachlich vorgesehen sind.
 */
enum class LocationConfirmationState {
    NOT_REQUESTED,
    PERMISSION_REQUESTED,
    PERMISSION_GRANTED,
    PERMISSION_DENIED,
    SIMULATED_CONFIRMED,
    CONFIRMED_ON_SITE,
}
