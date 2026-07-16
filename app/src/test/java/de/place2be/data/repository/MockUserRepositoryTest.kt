package de.place2be.data.repository

import de.place2be.data.mock.MockPlaceDataSource
import java.io.File
import java.nio.file.Files
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockUserRepositoryTest {
    private lateinit var storageDirectory: File
    private lateinit var repository: MockUserRepository

    @Before
    fun setUp() {
        storageDirectory = Files.createTempDirectory("place2be-bookmark-test").toFile()
        val dataSource = MockPlaceDataSource.create(storageDirectory) { fileName ->
            when (fileName) {
                "places.json" -> PLACES_JSON
                "users.json" -> USERS_JSON
                "reviews.json" -> "[]"
                "bookmarks.json" -> "[]"
                else -> error("Unexpected seed file: $fileName")
            }
        }
        repository = MockUserRepository(dataSource)
    }

    @After
    fun tearDown() {
        storageDirectory.deleteRecursively()
    }

    @Test
    fun `bookmark can be added and removed through repository`() {
        assertFalse(repository.isBookmarked(USER_UUID, PLACE_UUID))

        repository.setBookmarked(
            userUuid = USER_UUID,
            placeUuid = PLACE_UUID,
            bookmarked = true,
        )

        assertTrue(repository.isBookmarked(USER_UUID, PLACE_UUID))
        assertEquals(setOf(PLACE_UUID), repository.getBookmarkedPlaceUuids(USER_UUID))
        assertEquals(1, repository.getBookmarks(USER_UUID).size)

        repository.setBookmarked(
            userUuid = USER_UUID,
            placeUuid = PLACE_UUID,
            bookmarked = false,
        )

        assertFalse(repository.isBookmarked(USER_UUID, PLACE_UUID))
        assertTrue(repository.getBookmarks(USER_UUID).isEmpty())
    }

    @Test
    fun `adding the same bookmark twice remains idempotent`() {
        repository.setBookmarked(USER_UUID, PLACE_UUID, bookmarked = true)
        repository.setBookmarked(USER_UUID, PLACE_UUID, bookmarked = true)

        assertEquals(1, repository.getBookmarks(USER_UUID).size)
    }

    private companion object {
        val USER_UUID: UUID = UUID.fromString("f5257520-3685-4a1a-be5b-4a0ceb1baba7")
        val PLACE_UUID: UUID = UUID.fromString("e31093f8-b56e-45f9-b0e3-6f93a8db517e")

        val USERS_JSON = """
            [
              {
                "uuid": "$USER_UUID",
                "displayName": "MainEntdecker",
                "userScore": 0
              }
            ]
        """.trimIndent()

        val PLACES_JSON = """
            [
              {
                "uuid": "$PLACE_UUID",
                "name": "Testort",
                "category": "PARK",
                "description": "Ein Ort für den Bookmark-Test.",
                "imageReference": null,
                "latitude": 50.0,
                "longitude": 8.0,
                "locationHint": "Frankfurt am Main",
                "attributes": [],
                "initialScore": 3.0
              }
            ]
        """.trimIndent()
    }
}
