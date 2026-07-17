package de.place2be.data.mock

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.place2be.domain.model.Bookmark
import de.place2be.domain.model.Place
import de.place2be.domain.model.PlaceAttribute
import de.place2be.domain.model.PlaceCategory
import de.place2be.domain.model.Review
import de.place2be.domain.model.User
import java.io.File
import java.util.UUID

/**
 * Zentrale CRUD-Datenquelle fuer den JSON-basierten Mock.
 *
 * Die Dateien in src/main/data/mockdata sind als unveraenderliche Assets
 * registriert. Beim ersten Zugriff werden sie nach filesDir/mockdata kopiert.
 * Alle Lese- und Schreiboperationen arbeiten danach auf diesen internen Dateien.
 *
 * Pro Ort bleiben hoechstens die 50 neuesten Rezensionstexte gespeichert. Wird
 * die Grenze ueberschritten, wird nur das Textfeld der verdraengten Rezension auf
 * null gesetzt. Numerische Kriterien, Nutzerbezug, Zeitstempel und Reaktionen
 * bleiben als vollwertige Bewertungsdaten erhalten.
 */
class MockPlaceDataSource private constructor(
    private val storageDirectory: File,
    private val seedContentProvider: (String) -> String,
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create(),
) {
    private val fileLock = Any()

    init {
        require(storageDirectory.exists() || storageDirectory.mkdirs()) {
            "Could not create mock data directory: ${storageDirectory.absolutePath}"
        }
        initializeStorage()
        normalizeStoredReviewTexts()
    }

    fun getPlaces(): List<Place> = readList<PlaceJson>(MockDataFile.PLACES).map(PlaceJson::toDomain)

    fun getPlace(uuid: UUID): Place? = getPlaces().firstOrNull { it.uuid == uuid }

    fun createPlace(place: Place): Place = synchronized(fileLock) {
        val places = readListUnlocked<PlaceJson>(MockDataFile.PLACES)
        require(places.none { it.uuid == place.uuid.toString() }) { "Place ${place.uuid} already exists." }
        writeListUnlocked(MockDataFile.PLACES, places + PlaceJson.from(place))
        place
    }

    fun updatePlace(place: Place): Boolean = synchronized(fileLock) {
        updateByUuid(MockDataFile.PLACES, PlaceJson.from(place)) { it.uuid }
    }

    fun deletePlace(uuid: UUID): Boolean = synchronized(fileLock) {
        val deleted = deleteByUuid<PlaceJson>(MockDataFile.PLACES, uuid) { it.uuid }
        if (deleted) {
            writeListUnlocked(
                MockDataFile.REVIEWS,
                readListUnlocked<ReviewJson>(MockDataFile.REVIEWS).filterNot { it.placeUuid == uuid.toString() },
            )
            writeListUnlocked(
                MockDataFile.BOOKMARKS,
                readListUnlocked<BookmarkJson>(MockDataFile.BOOKMARKS).filterNot { it.placeUuid == uuid.toString() },
            )
        }
        deleted
    }

    fun getReviews(): List<Review> = readList<ReviewJson>(MockDataFile.REVIEWS).map(ReviewJson::toDomain)

    fun getReviewsForPlace(placeUuid: UUID): List<Review> =
        getReviews().filter { it.placeUuid == placeUuid }

    fun createReview(review: Review): Review = synchronized(fileLock) {
        require(getPlace(review.placeUuid) != null) { "Unknown place ${review.placeUuid}." }
        require(getUser(review.userUuid) != null) { "Unknown user ${review.userUuid}." }
        val reviews = readListUnlocked<ReviewJson>(MockDataFile.REVIEWS)
        val reviewJson = ReviewJson.from(review)
        require(reviews.none { it.uuid == reviewJson.uuid }) { "Review ${review.uuid} already exists." }

        val retainedReviews = retainNewestReviewTexts(reviews + reviewJson)
        writeListUnlocked(MockDataFile.REVIEWS, retainedReviews)
        retainedReviews.first { it.uuid == reviewJson.uuid }.toDomain()
    }

    fun updateReview(review: Review): Boolean = synchronized(fileLock) {
        val reviews = readListUnlocked<ReviewJson>(MockDataFile.REVIEWS)
        val replacement = ReviewJson.from(review)
        val index = reviews.indexOfFirst { it.uuid == replacement.uuid }
        if (index < 0) return@synchronized false

        val updatedReviews = reviews.toMutableList().apply { this[index] = replacement }
        writeListUnlocked(MockDataFile.REVIEWS, retainNewestReviewTexts(updatedReviews))
        true
    }

    fun deleteReview(uuid: UUID): Boolean = synchronized(fileLock) {
        deleteByUuid<ReviewJson>(MockDataFile.REVIEWS, uuid) { it.uuid }
    }

    fun getUsers(): List<User> = readList<UserJson>(MockDataFile.USERS).map(UserJson::toDomain)

    fun getUser(uuid: UUID): User? = getUsers().firstOrNull { it.uuid == uuid }

    fun createUser(user: User): User = synchronized(fileLock) {
        val users = readListUnlocked<UserJson>(MockDataFile.USERS)
        require(users.none { it.uuid == user.uuid.toString() }) { "User ${user.uuid} already exists." }
        writeListUnlocked(MockDataFile.USERS, users + UserJson.from(user))
        user
    }

    fun updateUser(user: User): Boolean = synchronized(fileLock) {
        updateByUuid(MockDataFile.USERS, UserJson.from(user)) { it.uuid }
    }

    fun deleteUser(uuid: UUID): Boolean = synchronized(fileLock) {
        val deleted = deleteByUuid<UserJson>(MockDataFile.USERS, uuid) { it.uuid }
        if (deleted) {
            writeListUnlocked(
                MockDataFile.REVIEWS,
                readListUnlocked<ReviewJson>(MockDataFile.REVIEWS).filterNot { it.userUuid == uuid.toString() },
            )
            writeListUnlocked(
                MockDataFile.BOOKMARKS,
                readListUnlocked<BookmarkJson>(MockDataFile.BOOKMARKS).filterNot { it.userUuid == uuid.toString() },
            )
        }
        deleted
    }

    fun getBookmarks(): List<Bookmark> =
        readList<BookmarkJson>(MockDataFile.BOOKMARKS).map(BookmarkJson::toDomain)

    fun createBookmark(bookmark: Bookmark): Bookmark = synchronized(fileLock) {
        require(getPlace(bookmark.placeUuid) != null) { "Unknown place ${bookmark.placeUuid}." }
        require(getUser(bookmark.userUuid) != null) { "Unknown user ${bookmark.userUuid}." }
        val bookmarks = readListUnlocked<BookmarkJson>(MockDataFile.BOOKMARKS)
        require(bookmarks.none { it.uuid == bookmark.uuid.toString() }) { "Bookmark ${bookmark.uuid} already exists." }
        require(bookmarks.none {
            it.userUuid == bookmark.userUuid.toString() && it.placeUuid == bookmark.placeUuid.toString()
        }) { "Place ${bookmark.placeUuid} is already bookmarked by ${bookmark.userUuid}." }
        writeListUnlocked(MockDataFile.BOOKMARKS, bookmarks + BookmarkJson.from(bookmark))
        bookmark
    }

    fun updateBookmark(bookmark: Bookmark): Boolean = synchronized(fileLock) {
        updateByUuid(MockDataFile.BOOKMARKS, BookmarkJson.from(bookmark)) { it.uuid }
    }

    fun deleteBookmark(uuid: UUID): Boolean = synchronized(fileLock) {
        deleteByUuid<BookmarkJson>(MockDataFile.BOOKMARKS, uuid) { it.uuid }
    }

    fun deleteBookmark(userUuid: UUID, placeUuid: UUID): Boolean = synchronized(fileLock) {
        val bookmarks = readListUnlocked<BookmarkJson>(MockDataFile.BOOKMARKS)
        val remaining = bookmarks.filterNot {
            it.userUuid == userUuid.toString() && it.placeUuid == placeUuid.toString()
        }
        if (remaining.size == bookmarks.size) return@synchronized false
        writeListUnlocked(MockDataFile.BOOKMARKS, remaining)
        true
    }

    private fun initializeStorage() = synchronized(fileLock) {
        val versionFile = File(storageDirectory, SEED_VERSION_FILE)
        val requiresNewSeed = versionFile.readTextOrNull() != SEED_VERSION.toString()
        MockDataFile.entries.forEach { dataFile ->
            val file = dataFile.fileIn(storageDirectory)
            if (requiresNewSeed || !file.exists()) {
                file.writeText(seedContentProvider(dataFile.fileName), Charsets.UTF_8)
            }
        }
        if (requiresNewSeed) versionFile.writeText(SEED_VERSION.toString(), Charsets.UTF_8)
    }

    /**
     * Bereinigt auch bereits bestehende lokale Arbeitskopien beim App-Start. So
     * gilt die 50er-Grenze nicht nur fuer neu angelegte, sondern ebenso fuer vor
     * dieser Regel gespeicherte Datenbestaende.
     */
    private fun normalizeStoredReviewTexts() = synchronized(fileLock) {
        val reviews = readListUnlocked<ReviewJson>(MockDataFile.REVIEWS)
        val retainedReviews = retainNewestReviewTexts(reviews)
        if (retainedReviews != reviews) {
            writeListUnlocked(MockDataFile.REVIEWS, retainedReviews)
        }
    }

    /**
     * Behaelt pro Ort die 50 neuesten nichtleeren Texte. Bei gleichem Zeitstempel
     * gewinnt der spaeter gespeicherte Listeneintrag, wodurch eine gerade neu
     * angelegte Rezension nicht durch einen gleichzeitigen Altbestand verdraengt
     * wird. Ausschliesslich das Textfeld aelterer Eintraege wird entfernt.
     */
    private fun retainNewestReviewTexts(reviews: List<ReviewJson>): List<ReviewJson> {
        val retainedTextReviewUuids = reviews
            .withIndex()
            .filter { indexedReview -> !indexedReview.value.text.isNullOrBlank() }
            .groupBy { indexedReview -> indexedReview.value.placeUuid }
            .values
            .flatMap { reviewsForPlace ->
                reviewsForPlace
                    .sortedWith(
                        compareByDescending<IndexedValue<ReviewJson>> { it.value.timestampMillis }
                            .thenByDescending { it.index },
                    )
                    .take(MAX_STORED_TEXT_REVIEWS_PER_PLACE)
            }
            .mapTo(mutableSetOf()) { indexedReview -> indexedReview.value.uuid }

        return reviews.map { storedReview ->
            if (!storedReview.text.isNullOrBlank() && storedReview.uuid !in retainedTextReviewUuids) {
                storedReview.copy(text = null)
            } else {
                storedReview
            }
        }
    }

    private fun File.readTextOrNull(): String? = if (exists()) readText(Charsets.UTF_8) else null

    private inline fun <reified T> readList(dataFile: MockDataFile): List<T> = synchronized(fileLock) {
        readListUnlocked(dataFile)
    }

    private inline fun <reified T> readListUnlocked(dataFile: MockDataFile): List<T> {
        val listType = TypeToken.getParameterized(List::class.java, T::class.java).type
        return gson.fromJson<List<T>>(dataFile.fileIn(storageDirectory).readText(Charsets.UTF_8), listType)
            ?: emptyList()
    }

    private fun <T> writeListUnlocked(dataFile: MockDataFile, values: List<T>) {
        dataFile.fileIn(storageDirectory).writeText(gson.toJson(values), Charsets.UTF_8)
    }

    private inline fun <reified T> updateByUuid(
        dataFile: MockDataFile,
        replacement: T,
        uuidOf: (T) -> String,
    ): Boolean {
        val values = readListUnlocked<T>(dataFile)
        val index = values.indexOfFirst { uuidOf(it) == uuidOf(replacement) }
        if (index < 0) return false
        writeListUnlocked(dataFile, values.toMutableList().apply { this[index] = replacement })
        return true
    }

    private inline fun <reified T> deleteByUuid(
        dataFile: MockDataFile,
        uuid: UUID,
        uuidOf: (T) -> String,
    ): Boolean {
        val values = readListUnlocked<T>(dataFile)
        val remaining = values.filterNot { uuidOf(it) == uuid.toString() }
        if (remaining.size == values.size) return false
        writeListUnlocked(dataFile, remaining)
        return true
    }

    companion object {
        private const val ASSET_DIRECTORY = "mockdata"
        private const val SEED_VERSION_FILE = ".seed-version"
        private const val SEED_VERSION = 2
        private const val MAX_STORED_TEXT_REVIEWS_PER_PLACE = 50

        fun create(context: Context): MockPlaceDataSource {
            val appContext = context.applicationContext
            return MockPlaceDataSource(
                storageDirectory = File(appContext.filesDir, ASSET_DIRECTORY),
                seedContentProvider = { fileName ->
                    appContext.assets.open("$ASSET_DIRECTORY/$fileName")
                        .bufferedReader(Charsets.UTF_8)
                        .use { it.readText() }
                },
            )
        }

        /** Factory fuer lokale JVM-Tests und Tools ohne Android Context. */
        fun create(
            storageDirectory: File,
            seedContentProvider: (String) -> String,
        ): MockPlaceDataSource = MockPlaceDataSource(storageDirectory, seedContentProvider)
    }
}

private enum class MockDataFile(val fileName: String) {
    PLACES("places.json"),
    REVIEWS("reviews.json"),
    USERS("users.json"),
    BOOKMARKS("bookmarks.json"),
    ;

    fun fileIn(directory: File): File = File(directory, fileName)
}

private data class PlaceJson(
    val uuid: String,
    val name: String,
    val category: String,
    val description: String,
    val imageReference: String?,
    val latitude: Double?,
    val longitude: Double?,
    val locationHint: String,
    val attributes: List<String>,
    val initialScore: Double,
) {
    fun toDomain(): Place = Place(
        uuid = UUID.fromString(uuid),
        name = name,
        category = PlaceCategory.valueOf(category),
        description = description,
        imageReference = imageReference,
        latitude = latitude,
        longitude = longitude,
        locationHint = locationHint,
        attributes = attributes.mapTo(mutableSetOf(), PlaceAttribute::valueOf),
        initialScore = initialScore,
    )

    companion object {
        fun from(place: Place): PlaceJson = PlaceJson(
            uuid = place.uuid.toString(),
            name = place.name,
            category = place.category.name,
            description = place.description,
            imageReference = place.imageReference,
            latitude = place.latitude,
            longitude = place.longitude,
            locationHint = place.locationHint,
            attributes = place.attributes.map(PlaceAttribute::name).sorted(),
            initialScore = place.initialScore,
        )
    }
}

private data class ReviewJson(
    val uuid: String,
    val placeUuid: String,
    val userUuid: String,
    val vibe: Int,
    val safety: Int,
    val accessibility: Int,
    val timestampMillis: Long,
    val text: String?,
    val likes: Int,
    val dislikes: Int,
) {
    fun toDomain(): Review = Review(
        uuid = UUID.fromString(uuid),
        placeUuid = UUID.fromString(placeUuid),
        userUuid = UUID.fromString(userUuid),
        vibe = vibe,
        safety = safety,
        accessibility = accessibility,
        timestampMillis = timestampMillis,
        text = text,
        likes = likes,
        dislikes = dislikes,
    )

    companion object {
        fun from(review: Review): ReviewJson = ReviewJson(
            uuid = review.uuid.toString(),
            placeUuid = review.placeUuid.toString(),
            userUuid = review.userUuid.toString(),
            vibe = review.vibe,
            safety = review.safety,
            accessibility = review.accessibility,
            timestampMillis = review.timestampMillis,
            text = review.text,
            likes = review.likes,
            dislikes = review.dislikes,
        )
    }
}

private data class UserJson(
    val uuid: String,
    val displayName: String,
    val userScore: Int,
) {
    fun toDomain(): User = User(UUID.fromString(uuid), displayName, userScore)

    companion object {
        fun from(user: User): UserJson = UserJson(user.uuid.toString(), user.displayName, user.userScore)
    }
}

private data class BookmarkJson(
    val uuid: String,
    val userUuid: String,
    val placeUuid: String,
    val createdAtMillis: Long,
) {
    fun toDomain(): Bookmark = Bookmark(
        uuid = UUID.fromString(uuid),
        userUuid = UUID.fromString(userUuid),
        placeUuid = UUID.fromString(placeUuid),
        createdAtMillis = createdAtMillis,
    )

    companion object {
        fun from(bookmark: Bookmark): BookmarkJson = BookmarkJson(
            bookmark.uuid.toString(),
            bookmark.userUuid.toString(),
            bookmark.placeUuid.toString(),
            bookmark.createdAtMillis,
        )
    }
}
