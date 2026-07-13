package de.place2be.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class InMemoryUserRepositoryTest {
    private val userUuid = UUID.fromString("aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaaa")
    private val placeUuid = UUID.fromString("33333333-3333-4333-8333-333333333333")

    @Test
    fun bookmark_canBeAddedAndRemoved() {
        val repository = InMemoryUserRepository()

        assertFalse(repository.isBookmarked(userUuid, placeUuid))
        repository.setBookmarked(userUuid, placeUuid, bookmarked = true)
        assertTrue(repository.isBookmarked(userUuid, placeUuid))
        repository.setBookmarked(userUuid, placeUuid, bookmarked = false)
        assertFalse(repository.isBookmarked(userUuid, placeUuid))
    }
}
