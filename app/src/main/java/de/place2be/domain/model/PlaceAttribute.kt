package de.place2be.domain.model

/**
 * Beschreibt objektive oder halb-objektive Eigenschaften eines Ortes.
 *
 * Diese Attribute sind keine Bewertungskriterien. Sie dienen als Tags bzw.
 * spätere Filteroptionen, z. B. für Schatten, Sitzmöglichkeiten oder
 * Barrierefreiheit.
 */
enum class PlaceAttribute {
    PUBLIC_TOILETS,
    ACCESSIBLE,
    SHADE,
    SEATING,
    FOOD_AND_DRINK,
    COVERED,
    AIR_CONDITIONED,
    LANGUAGE_AND_CULTURE,
    LOCAL_EVENTS,
    TIME_DEPENDENT_EVENT,
}
