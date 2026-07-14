package de.place2be.data.repository

import de.place2be.data.mock.MockPlaceDataSource
import de.place2be.domain.model.Bookmark
import de.place2be.domain.model.User
import de.place2be.domain.repository.UserRepository
import java.util.UUID

/** Domain-Adapter fuer Nutzer und Bookmarks aus den JSON-Mock-Dateien. */
class MockUserRepository(
    private val dataSource: MockPlaceDataSource,
) : UserRepository {
    override fun getUser(userUuid: UUID): User? = dataSource.getUser(userUuid)

    override fun getBookmarkedPlaceUuids(userUuid: UUID): Set<UUID> = dataSource.getBookmarks()
        .asSequence()
        .filter { it.userUuid == userUuid }
        .map(Bookmark::placeUuid)
        .toSet()

    override fun getBookmarks(userUuid: UUID): List<Bookmark> = dataSource.getBookmarks()
        .filter { it.userUuid == userUuid }
        .sortedByDescending(Bookmark::createdAtMillis)

    override fun isBookmarked(userUuid: UUID, placeUuid: UUID): Boolean = dataSource.getBookmarks()
        .any { it.userUuid == userUuid && it.placeUuid == placeUuid }

    override fun setBookmarked(userUuid: UUID, placeUuid: UUID, bookmarked: Boolean) {
        if (bookmarked && !isBookmarked(userUuid, placeUuid)) {
            dataSource.createBookmark(
                Bookmark(
                    userUuid = userUuid,
                    placeUuid = placeUuid,
                    createdAtMillis = System.currentTimeMillis(),
                ),
            )
        } else if (!bookmarked) {
            dataSource.deleteBookmark(userUuid, placeUuid)
        }
    }
}
