package de.place2be.data.repository

import de.place2be.data.mock.MockPlaceDataSource
import de.place2be.domain.model.Bookmark
import de.place2be.domain.model.User
import de.place2be.domain.repository.UserRepository
import java.util.UUID

/**
 * Local-first Repository für Demo-Nutzer und gespeicherte Orte.
 */
class InMemoryUserRepository(
    mockPlaceDataSource: MockPlaceDataSource = MockPlaceDataSource(),
) : UserRepository {
    private val users = mockPlaceDataSource.users.associateBy(User::uuid)
    private val bookmarks = mockPlaceDataSource.bookmarks.toMutableList()

    override fun getUser(userUuid: UUID): User? = users[userUuid]

    override fun getBookmarkedPlaceUuids(userUuid: UUID): Set<UUID> {
        return bookmarks
            .asSequence()
            .filter { it.userUuid == userUuid }
            .map(Bookmark::placeUuid)
            .toSet()
    }

    override fun isBookmarked(userUuid: UUID, placeUuid: UUID): Boolean {
        return bookmarks.any { it.userUuid == userUuid && it.placeUuid == placeUuid }
    }

    override fun setBookmarked(userUuid: UUID, placeUuid: UUID, bookmarked: Boolean) {
        if (bookmarked && !isBookmarked(userUuid, placeUuid)) {
            bookmarks += Bookmark(
                userUuid = userUuid,
                placeUuid = placeUuid,
                createdAtMillis = System.currentTimeMillis(),
            )
        } else if (!bookmarked) {
            bookmarks.removeAll { it.userUuid == userUuid && it.placeUuid == placeUuid }
        }
    }
}
