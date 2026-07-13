package de.place2be.data.mock

import de.place2be.domain.model.Bookmark
import de.place2be.domain.model.Place
import de.place2be.domain.model.PlaceAttribute
import de.place2be.domain.model.PlaceCategory
import de.place2be.domain.model.Review
import de.place2be.domain.model.User
import java.util.UUID

/**
 * Lokale Mock-Daten für den MVP.
 *
 * Diese Datenquelle ersetzt im Prototyp eine echte Karten-, Backend- oder
 * Datenbank-Anbindung. Sie ist bewusst zentral gehalten, damit UI-Komponenten
 * keine Demo-Daten hart codieren müssen.
 */
class MockPlaceDataSource {
    private val demoUserUuid = UUID.fromString("aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaaa")

    val users: List<User> = listOf(
        User(
            uuid = demoUserUuid,
            displayName = "MainEntdecker",
            userScore = 128,
        ),
    )

    val places: List<Place> = listOf(
        Place(
            uuid = UUID.fromString("11111111-1111-4111-8111-111111111111"),
            name = "Mainufer",
            category = PlaceCategory.PROMENADE,
            description = "Promenade am Main mit Blick auf Skyline und Wasser.",
            latitude = 50.1049,
            longitude = 8.6837,
            locationHint = "Frankfurt am Main, Mainufer",
            attributes = setOf(
                PlaceAttribute.SEATING,
                PlaceAttribute.SHADE,
                PlaceAttribute.LANGUAGE_AND_CULTURE,
            ),
            initialScore = 4.1,
        ),
        Place(
            uuid = UUID.fromString("22222222-2222-4222-8222-222222222222"),
            name = "Bethmannpark",
            category = PlaceCategory.PARK,
            description = "Ruhiger Park mit Grünflächen und Sitzgelegenheiten.",
            latitude = 50.1211,
            longitude = 8.6902,
            locationHint = "Frankfurt am Main, Nordend",
            attributes = setOf(
                PlaceAttribute.SEATING,
                PlaceAttribute.SHADE,
                PlaceAttribute.PUBLIC_TOILETS,
            ),
            initialScore = 3.9,
        ),
        Place(
            uuid = UUID.fromString("33333333-3333-4333-8333-333333333333"),
            name = "Konstablerwache",
            category = PlaceCategory.SQUARE,
            description = "Zentraler öffentlicher Platz mit guter ÖPNV-Anbindung.",
            latitude = 50.1144,
            longitude = 8.6873,
            locationHint = "Frankfurt am Main, Innenstadt",
            attributes = setOf(
                PlaceAttribute.FOOD_AND_DRINK,
                PlaceAttribute.LOCAL_EVENTS,
                PlaceAttribute.TIME_DEPENDENT_EVENT,
            ),
            initialScore = 3.3,
        ),
        Place(
            uuid = UUID.fromString("44444444-4444-4444-8444-444444444444"),
            name = "Goetheplatz",
            category = PlaceCategory.SQUARE,
            description = "Urbaner Treffpunkt in zentraler Lage.",
            latitude = 50.1116,
            longitude = 8.6779,
            locationHint = "Frankfurt am Main, Innenstadt",
            attributes = setOf(
                PlaceAttribute.SEATING,
                PlaceAttribute.FOOD_AND_DRINK,
            ),
            initialScore = 3.6,
        ),
        Place(
            uuid = UUID.fromString("55555555-5555-4555-8555-555555555555"),
            name = "Museumsufer",
            category = PlaceCategory.PROMENADE,
            description = "Öffentlicher Bereich entlang mehrerer Museen mit kulturellem Umfeld.",
            latitude = 50.1055,
            longitude = 8.6771,
            locationHint = "Frankfurt am Main, Sachsenhausen",
            attributes = setOf(
                PlaceAttribute.LANGUAGE_AND_CULTURE,
                PlaceAttribute.SEATING,
                PlaceAttribute.SHADE,
            ),
            initialScore = 4.2,
        ),
    )

    val reviews: List<Review> = listOf(
        Review(
            placeUuid = places[0].uuid,
            userUuid = demoUserUuid,
            vibe = 5,
            safety = 4,
            accessibility = 5,
            timestampMillis = daysAgo(1),
            text = "Schöner Blick auf den Main und eine entspannte Stimmung.",
            likes = 2,
            dislikes = 0,
        ),
        Review(
            placeUuid = places[0].uuid,
            userUuid = demoUserUuid,
            vibe = 3,
            safety = 3,
            accessibility = 4,
            timestampMillis = daysAgo(60),
            likes = 1,
            dislikes = 1,
        ),
        Review(
            placeUuid = places[1].uuid,
            userUuid = demoUserUuid,
            vibe = 4,
            safety = 4,
            accessibility = 3,
            timestampMillis = daysAgo(7),
            text = "Ruhig, grün und mit ausreichend Sitzmöglichkeiten.",
            likes = 0,
            dislikes = 0,
        ),
    )

    val bookmarks: List<Bookmark> = listOf(
        Bookmark(
            userUuid = demoUserUuid,
            placeUuid = places[0].uuid,
            createdAtMillis = daysAgo(2),
        ),
        Bookmark(
            userUuid = demoUserUuid,
            placeUuid = places[4].uuid,
            createdAtMillis = daysAgo(5),
        ),
    )

    private fun daysAgo(days: Long): Long {
        return System.currentTimeMillis() - days * MILLIS_PER_DAY
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
