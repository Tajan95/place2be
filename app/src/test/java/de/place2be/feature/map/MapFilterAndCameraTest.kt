package de.place2be.feature.map

import androidx.compose.ui.geometry.Offset
import com.google.gson.Gson
import de.place2be.domain.model.PlaceAttribute
import de.place2be.domain.model.PlaceCategory
import java.io.File
import java.util.UUID
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapFilterAndCameraTest {
    @Test
    fun `categories use OR while attributes use AND`() {
        val places = listOf(
            mapPlace(
                name = "Park",
                category = PlaceCategory.PARK,
                attributes = setOf(PlaceAttribute.SEATING, PlaceAttribute.SHADE),
            ),
            mapPlace(
                name = "Platz",
                category = PlaceCategory.SQUARE,
                attributes = setOf(PlaceAttribute.ACCESSIBLE, PlaceAttribute.SHADE),
            ),
            mapPlace(
                name = "Promenade",
                category = PlaceCategory.PROMENADE,
                attributes = setOf(PlaceAttribute.ACCESSIBLE, PlaceAttribute.SEATING),
            ),
        )

        val categoryMatches = places.filteredBy(
            MapFilterSelection(
                categoryNames = setOf(PlaceCategory.PARK.name, PlaceCategory.SQUARE.name),
            ),
        )
        assertEquals(setOf("Park", "Platz"), categoryMatches.mapTo(mutableSetOf()) { it.name })

        val attributeMatches = places.filteredBy(
            MapFilterSelection(
                attributeNames = setOf(PlaceAttribute.ACCESSIBLE.name, PlaceAttribute.SEATING.name),
            ),
        )
        assertEquals(listOf("Promenade"), attributeMatches.map { it.name })

        val combinedMatches = places.filteredBy(
            MapFilterSelection(
                categoryNames = setOf(PlaceCategory.PARK.name, PlaceCategory.PROMENADE.name),
                attributeNames = setOf(PlaceAttribute.SEATING.name),
            ),
        )
        assertEquals(setOf("Park", "Promenade"), combinedMatches.mapTo(mutableSetOf()) { it.name })
        assertEquals(places, places.filteredBy(MapFilterSelection()))
    }

    @Test
    fun `popular list excludes low rated and unreviewed places`() {
        val places = listOf(
            mapPlace("Sehr beliebt", PlaceCategory.PARK, currentScore = 4.6),
            mapPlace("Gerade beliebt", PlaceCategory.SQUARE, currentScore = 4.0),
            mapPlace("Unbeliebt", PlaceCategory.PROMENADE, currentScore = 3.9),
            mapPlace(
                "Ohne Bewertungen",
                PlaceCategory.DISTRICT,
                currentScore = 4.8,
                reviewCount = 0,
            ),
        )

        assertEquals(
            listOf("Sehr beliebt", "Gerade beliebt"),
            places.popularFirst().map { it.name },
        )
    }

    @Test
    fun `single category changes camera but keeps stable world position`() {
        val park = mapPlace("Park", PlaceCategory.PARK, mapX = 0.82f, mapY = 0.18f)
        val square = mapPlace("Platz", PlaceCategory.SQUARE, mapX = 0.24f, mapY = 0.54f)
        val promenade = mapPlace("Promenade", PlaceCategory.PROMENADE, mapX = 0.48f, mapY = 0.82f)
        val allPlaces = listOf(park, square, promenade)
        val stableWorldPositions = allPlaces.fitIntoMapViewport()
        val parkWorldPosition = stableWorldPositions.getValue(park.uuid)

        val visiblePlaces = allPlaces.filteredBy(
            MapFilterSelection(categoryNames = setOf(PlaceCategory.PARK.name)),
        )
        val camera = visiblePlaces
            .map { stableWorldPositions.getValue(it.uuid) }
            .fitMapCamera()
        val parkScreenPosition = parkWorldPosition.transformedBy(camera)

        assertEquals(parkWorldPosition, stableWorldPositions.getValue(park.uuid))
        assertEquals(2f, camera.zoom, FLOAT_TOLERANCE)
        assertEquals(0.5f, parkScreenPosition.x, FLOAT_TOLERANCE)
        assertEquals(0.43f, parkScreenPosition.y, FLOAT_TOLERANCE)
    }

    @Test
    fun `reset camera keeps default marker positions unchanged`() {
        val places = listOf(
            mapPlace("Nordwest", PlaceCategory.PARK, mapX = 0.1f, mapY = 0.2f),
            mapPlace("Südost", PlaceCategory.SQUARE, mapX = 0.9f, mapY = 0.66f),
        )
        val worldPositions = places.fitIntoMapViewport().values.toList()
        val camera = worldPositions.fitMapCamera()

        assertEquals(1f, camera.zoom, FLOAT_TOLERANCE)
        worldPositions.forEach { position ->
            val transformed = position.transformedBy(camera)
            assertEquals(position.x, transformed.x, FLOAT_TOLERANCE)
            assertEquals(position.y, transformed.y, FLOAT_TOLERANCE)
        }
    }

    @Test
    fun `map panning stays inside expanded mock map`() {
        val constrained = Offset(2_000f, -2_000f).constrainedToMapSurroundings(
            viewportWidthPx = 432f,
            viewportHeightPx = 800f,
        )

        assertEquals(367.2f, constrained.x, FLOAT_TOLERANCE)
        assertEquals(-680f, constrained.y, FLOAT_TOLERANCE)
    }

    @Test
    fun `nearby markers receive readable stable positions`() {
        val places = (1..6).map { index ->
            mapPlace(
                name = "Ort $index",
                category = PlaceCategory.OTHER_PUBLIC_PLACE,
                mapX = 0.5f,
                mapY = 0.5f,
            )
        }
        val positions = places.fitIntoMapViewport().values.toList()

        assertEquals(places.size, positions.size)
        positions.forEachIndexed { index, first ->
            positions.drop(index + 1).forEach { second ->
                val overlaps = abs(first.x - second.x) < MIN_MARKER_X_DISTANCE &&
                    abs(first.y - second.y) < MIN_MARKER_Y_DISTANCE
                assertFalse("Marker positions $first and $second overlap.", overlaps)
            }
        }
    }

    @Test
    fun `marker hint uses free left side to stay inside viewport`() {
        val place = mapPlace("Rechter Rand", PlaceCategory.PARK)
        val sides = listOf(place).resolveMarkerHintSides(
            positions = mapOf(place.uuid to MapViewportPosition(x = 0.9f, y = 0.4f)),
            viewportWidthDp = 432f,
            viewportHeightDp = 800f,
        )

        assertEquals(MarkerHintSide.LEFT, sides.getValue(place.uuid))
    }

    @Test
    fun `marker hint stays right when both sides are free`() {
        val place = mapPlace("Freier Ort", PlaceCategory.PARK)
        val sides = listOf(place).resolveMarkerHintSides(
            positions = mapOf(place.uuid to MapViewportPosition(x = 0.5f, y = 0.4f)),
            viewportWidthDp = 432f,
            viewportHeightDp = 800f,
        )

        assertEquals(MarkerHintSide.RIGHT, sides.getValue(place.uuid))
    }

    @Test
    fun `marker hint avoids a marker that is processed later`() {
        val firstPlace = mapPlace("Museumsufer", PlaceCategory.PROMENADE)
        val secondPlace = mapPlace("Mainufer", PlaceCategory.PROMENADE)
        val sides = listOf(firstPlace, secondPlace).resolveMarkerHintSides(
            positions = mapOf(
                firstPlace.uuid to MapViewportPosition(x = 0.30f, y = 0.4f),
                secondPlace.uuid to MapViewportPosition(x = 0.65f, y = 0.4f),
            ),
            viewportWidthDp = 432f,
            viewportHeightDp = 800f,
        )

        assertEquals(MarkerHintSide.LEFT, sides.getValue(firstPlace.uuid))
        assertEquals(MarkerHintSide.RIGHT, sides.getValue(secondPlace.uuid))
    }

    @Test
    fun `hint collisions are recalculated for filtered places`() {
        val target = mapPlace("Rechter Rand", PlaceCategory.PARK)
        val blocker = mapPlace("Blockierender Ort", PlaceCategory.SQUARE)
        val positions = mapOf(
            target.uuid to MapViewportPosition(x = 0.9f, y = 0.4f),
            blocker.uuid to MapViewportPosition(x = 0.55f, y = 0.4f),
        )

        val withBlocker = listOf(target, blocker).resolveMarkerHintSides(
            positions = positions,
            viewportWidthDp = 432f,
            viewportHeightDp = 800f,
        )
        val afterFilter = listOf(target).resolveMarkerHintSides(
            positions = positions,
            viewportWidthDp = 432f,
            viewportHeightDp = 800f,
        )

        assertEquals(MarkerHintSide.RIGHT, withBlocker.getValue(target.uuid))
        assertEquals(MarkerHintSide.LEFT, afterFilter.getValue(target.uuid))
    }

    @Test
    fun `dense markers use vertical hint offset only as collision fallback`() {
        val leftBlocker = mapPlace("Links", PlaceCategory.PARK)
        val target = mapPlace("Mitte", PlaceCategory.SQUARE)
        val rightBlocker = mapPlace("Rechts", PlaceCategory.PROMENADE)
        val places = listOf(leftBlocker, target, rightBlocker)
        val placements = places.resolveMarkerHintPlacements(
            positions = mapOf(
                leftBlocker.uuid to MapViewportPosition(x = 0.15f, y = 0.4f),
                target.uuid to MapViewportPosition(x = 0.50f, y = 0.4f),
                rightBlocker.uuid to MapViewportPosition(x = 0.85f, y = 0.4f),
            ),
            viewportWidthDp = 432f,
            viewportHeightDp = 800f,
        )

        assertEquals(0f, placements.getValue(leftBlocker.uuid).verticalOffsetDp, FLOAT_TOLERANCE)
        assertFalse(
            "The center hint should move vertically when both horizontal sides are blocked.",
            abs(placements.getValue(target.uuid).verticalOffsetDp) < FLOAT_TOLERANCE,
        )
    }

    @Test
    fun `expanded demo seed keeps hint cards clear of every marker`() {
        val seedPlaces = loadSeedPlaces()
        val minLatitude = seedPlaces.minOf(SeedMapPlace::latitude)
        val maxLatitude = seedPlaces.maxOf(SeedMapPlace::latitude)
        val minLongitude = seedPlaces.minOf(SeedMapPlace::longitude)
        val maxLongitude = seedPlaces.maxOf(SeedMapPlace::longitude)
        val places = seedPlaces.map { seed ->
            mapPlace(
                uuid = UUID.fromString(seed.uuid),
                name = seed.name,
                category = PlaceCategory.valueOf(seed.category),
                mapX = 0.14f +
                    ((seed.longitude - minLongitude) / (maxLongitude - minLongitude)).toFloat() * 0.72f,
                mapY = 0.14f +
                    ((maxLatitude - seed.latitude) / (maxLatitude - minLatitude)).toFloat() * 0.72f,
            )
        }
        val worldPositions = places.fitIntoMapViewport()
        assertEquals(
            MapViewportPosition(x = -0.38f, y = 0.20f),
            worldPositions.getValue(places.first { it.name == "Bockenheim" }.uuid),
        )
        assertEquals(
            MapViewportPosition(x = -0.24f, y = 1.10f),
            worldPositions.getValue(places.first { it.name == "Skyline Plaza" }.uuid),
        )
        assertEquals(
            MapViewportPosition(x = 1.38f, y = 0.72f),
            worldPositions.getValue(places.first { it.name == "Osthafenplatz" }.uuid),
        )
        assertEquals(
            MapViewportPosition(x = 0.30f, y = -0.22f),
            worldPositions.getValue(places.first { it.name == "Günthersburgpark" }.uuid),
        )
        assertEquals(
            MapViewportPosition(x = 1.15f, y = -0.08f),
            worldPositions.getValue(places.first { it.name == "Zeil" }.uuid),
        )
        val originalPlaceNames = setOf(
            "Mainufer",
            "Bethmannpark",
            "Konstablerwache",
            "Goetheplatz",
            "Museumsufer",
        )
        assertTrue(
            places.filter { it.name in originalPlaceNames }.all { place ->
                val position = worldPositions.getValue(place.uuid)
                position.x in 0.10f..0.90f && position.y in 0.20f..0.66f
            },
        )
        val camera = MapCamera()
        val screenPositions = places.associate { place ->
            place.uuid to worldPositions.getValue(place.uuid).transformedBy(camera)
        }
        val placements = places.resolveMarkerHintPlacements(
            positions = screenPositions,
            viewportWidthDp = PIXEL_9_WIDTH_DP,
            viewportHeightDp = PIXEL_9_HEIGHT_DP,
        )
        val hintBoundsByPlace = places.associate { place ->
            place.uuid to screenPositions.getValue(place.uuid).hintCardBounds(
                placement = placements.getValue(place.uuid),
                viewportWidthDp = PIXEL_9_WIDTH_DP,
                viewportHeightDp = PIXEL_9_HEIGHT_DP,
            )
        }

        places.forEach { place ->
            val hintBounds = hintBoundsByPlace.getValue(place.uuid)
            places.filterNot { it.uuid == place.uuid }.forEach { other ->
                val markerBounds = screenPositions.getValue(other.uuid).markerBounds(
                    viewportWidthDp = PIXEL_9_WIDTH_DP,
                    viewportHeightDp = PIXEL_9_HEIGHT_DP,
                )
                assertEquals(
                    "${place.name} hint overlaps ${other.name} marker.",
                    0f,
                    hintBounds.overlapArea(markerBounds),
                    FLOAT_TOLERANCE,
                )
            }
        }
        places.forEachIndexed { index, place ->
            places.drop(index + 1).forEach { other ->
                assertEquals(
                    "${place.name} hint overlaps ${other.name} hint.",
                    0f,
                    hintBoundsByPlace.getValue(place.uuid)
                        .overlapArea(hintBoundsByPlace.getValue(other.uuid)),
                    FLOAT_TOLERANCE,
                )
            }
        }
    }

    private fun loadSeedPlaces(): List<SeedMapPlace> {
        val candidates = listOf(
            File("src/main/data/mockdata/places.json"),
            File("app/src/main/data/mockdata/places.json"),
        )
        val seedFile = candidates.firstOrNull(File::isFile)
            ?: error("places.json seed file not found")
        return Gson().fromJson(seedFile.readText(Charsets.UTF_8), Array<SeedMapPlace>::class.java).toList()
    }

    private fun mapPlace(
        name: String,
        category: PlaceCategory,
        attributes: Set<PlaceAttribute> = emptySet(),
        mapX: Float = 0.5f,
        mapY: Float = 0.5f,
        currentScore: Double = 4.0,
        reviewCount: Int = 1,
        uuid: UUID = UUID.randomUUID(),
    ) = MapPlaceUiState(
        uuid = uuid,
        name = name,
        description = "$name Beschreibung",
        category = category,
        categoryLabel = name,
        locationHint = "Frankfurt am Main",
        attributes = attributes,
        currentScore = currentScore,
        vibeScore = 4.0,
        safetyScore = 4.0,
        accessibilityScore = 4.0,
        reviewCount = reviewCount,
        recentReviewCount = reviewCount,
        mapXFraction = mapX,
        mapYFraction = mapY,
        bookmarkedAtMillis = null,
        canRate = true,
        ratingEligibilityMessage = "",
    )

    private companion object {
        const val FLOAT_TOLERANCE = 0.0001f
        const val PIXEL_9_WIDTH_DP = 411.43f
        const val PIXEL_9_HEIGHT_DP = 923.43f
    }

    private data class SeedMapPlace(
        val uuid: String,
        val name: String,
        val category: String,
        val latitude: Double,
        val longitude: Double,
    )
}
