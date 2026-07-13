package de.place2be.domain.repository

import de.place2be.domain.model.Bookmark
import de.place2be.domain.model.User
import java.util.UUID

/**
 * Abstraktion für local-first Nutzerinformationen und Bookmarks.
 */
interface UserRepository {
    fun getUser(userUuid: UUID): User?

    fun getBookmarkedPlaceUuids(userUuid: UUID): Set<UUID>

    fun getBookmarks(userUuid: UUID): List<Bookmark>

    fun isBookmarked(userUuid: UUID, placeUuid: UUID): Boolean

    fun setBookmarked(userUuid: UUID, placeUuid: UUID, bookmarked: Boolean)
}
