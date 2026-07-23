package de.place2be.data.mock

import de.place2be.domain.model.PlaceAttribute
import de.place2be.domain.model.PlaceCategory
import java.io.File
import java.nio.file.Files
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockSeedDataConsistencyTest {
    private lateinit var storageDirectory: File
    private lateinit var dataSource: MockPlaceDataSource

    @Before
    fun setUp() {
        storageDirectory = Files.createTempDirectory("place2be-seed-consistency").toFile()
        dataSource = MockPlaceDataSource.create(
            storageDirectory = storageDirectory,
            seedContentProvider = { fileName -> seedFile(fileName).readText(Charsets.UTF_8) },
        )
    }

    @After
    fun tearDown() {
        storageDirectory.deleteRecursively()
    }

    @Test
    fun `expanded seed covers every category and attribute`() {
        val places = dataSource.getPlaces()

        assertEquals(10, places.size)
        assertEquals(PlaceCategory.entries.toSet(), places.mapTo(mutableSetOf()) { it.category })
        assertEquals(
            PlaceAttribute.entries.toSet(),
            places.flatMapTo(mutableSetOf()) { it.attributes },
        )
        assertEquals(places.size, places.mapTo(mutableSetOf()) { it.uuid }.size)
    }

    @Test
    fun `seed reviews and profiles have consistent references without demo activity`() {
        val places = dataSource.getPlaces()
        val users = dataSource.getUsers()
        val reviews = dataSource.getReviews()
        val bookmarks = dataSource.getBookmarks()
        val placeUuids = places.mapTo(mutableSetOf()) { it.uuid }
        val userUuids = users.mapTo(mutableSetOf()) { it.uuid }

        assertTrue(reviews.all { it.placeUuid in placeUuids })
        assertTrue(reviews.all { it.userUuid in userUuids })
        assertTrue(places.all { place -> reviews.count { it.placeUuid == place.uuid } >= 3 })
        assertEquals(reviews.size, reviews.mapTo(mutableSetOf()) { it.uuid }.size)
        assertTrue(reviews.none { it.userUuid == DEMO_USER_UUID })
        assertTrue(bookmarks.none { it.userUuid == DEMO_USER_UUID })
        assertTrue(bookmarks.isEmpty())
    }

    private fun seedFile(fileName: String): File {
        val candidates = listOf(
            File("src/main/data/mockdata", fileName),
            File("app/src/main/data/mockdata", fileName),
        )
        return candidates.firstOrNull(File::isFile)
            ?: error("Seed file not found: $fileName")
    }

    private companion object {
        val DEMO_USER_UUID: UUID = UUID.fromString("f5257520-3685-4a1a-be5b-4a0ceb1baba7")
    }
}
