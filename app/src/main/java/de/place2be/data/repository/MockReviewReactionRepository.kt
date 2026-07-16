package de.place2be.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.place2be.domain.model.Review
import de.place2be.domain.model.ReviewReaction
import de.place2be.domain.model.ReviewReactionType
import de.place2be.domain.repository.PlaceRepository
import de.place2be.domain.repository.ReviewReactionRepository
import java.io.File
import java.util.UUID

/**
 * JSON-basierte Local-first-Implementierung der Review-Reaktionen.
 *
 * Die bestehenden Like-/Dislike-Zahlen der Seed-Reviews bilden den bereits
 * vorhandenen Community-Stand ab. Neue, accountgebundene Reaktionen werden
 * zusätzlich in `review_reactions.json` gespeichert. Beim Toggle werden sowohl
 * der explizite Nutzer-Eintrag als auch die aggregierten Zähler der Rezension
 * konsistent aktualisiert.
 */
class MockReviewReactionRepository private constructor(
    private val reactionsFile: File,
    private val placeRepository: PlaceRepository,
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create(),
) : ReviewReactionRepository {
    private val fileLock = Any()

    init {
        val parent = reactionsFile.parentFile
        require(parent == null || parent.exists() || parent.mkdirs()) {
            "Could not create review reaction directory: ${parent?.absolutePath}"
        }
        if (!reactionsFile.exists()) reactionsFile.writeText("[]", Charsets.UTF_8)
    }

    override fun getReaction(reviewUuid: UUID, userUuid: UUID): ReviewReaction? = synchronized(fileLock) {
        readReactionsUnlocked().firstOrNull {
            it.reviewUuid == reviewUuid && it.userUuid == userUuid
        }
    }

    override fun getReactionsForUser(userUuid: UUID): List<ReviewReaction> = synchronized(fileLock) {
        readReactionsUnlocked()
            .filter { it.userUuid == userUuid }
            .sortedByDescending(ReviewReaction::createdAtMillis)
    }

    override fun toggleReaction(
        reviewUuid: UUID,
        userUuid: UUID,
        type: ReviewReactionType,
    ): ReviewReaction? = synchronized(fileLock) {
        val review = requireNotNull(placeRepository.getReview(reviewUuid)) {
            "Unknown review $reviewUuid."
        }
        require(review.userUuid != userUuid) { "Users cannot react to their own review." }

        val reactions = readReactionsUnlocked()
        val existingIndex = reactions.indexOfFirst {
            it.reviewUuid == reviewUuid && it.userUuid == userUuid
        }
        val existing = reactions.getOrNull(existingIndex)
        val nowMillis = System.currentTimeMillis()
        val nextReaction = when {
            existing == null -> ReviewReaction(
                reviewUuid = reviewUuid,
                userUuid = userUuid,
                type = type,
                createdAtMillis = nowMillis,
            )
            existing.type == type -> null
            else -> existing.copy(type = type, createdAtMillis = nowMillis)
        }

        val updatedReview = review.withReactionTransition(
            previous = existing?.type,
            next = nextReaction?.type,
        )
        check(placeRepository.updateReview(updatedReview)) {
            "Could not update reaction counters for review $reviewUuid."
        }

        val updatedReactions = reactions.toMutableList().apply {
            when {
                existingIndex < 0 && nextReaction != null -> add(nextReaction)
                existingIndex >= 0 && nextReaction == null -> removeAt(existingIndex)
                existingIndex >= 0 && nextReaction != null -> this[existingIndex] = nextReaction
            }
        }
        writeReactionsUnlocked(updatedReactions)
        nextReaction
    }

    private fun readReactionsUnlocked(): List<ReviewReaction> {
        val listType = TypeToken.getParameterized(List::class.java, ReviewReactionJson::class.java).type
        val jsonValues = gson.fromJson<List<ReviewReactionJson>>(
            reactionsFile.readText(Charsets.UTF_8),
            listType,
        ).orEmpty()
        return jsonValues.map(ReviewReactionJson::toDomain)
    }

    private fun writeReactionsUnlocked(reactions: List<ReviewReaction>) {
        reactionsFile.writeText(
            gson.toJson(reactions.map { ReviewReactionJson.from(it) }),
            Charsets.UTF_8,
        )
    }

    companion object {
        private const val REACTIONS_FILE_NAME = "review_reactions.json"
        private const val MOCK_DATA_DIRECTORY = "mockdata"

        fun create(
            context: Context,
            placeRepository: PlaceRepository,
        ): MockReviewReactionRepository {
            val appContext = context.applicationContext
            return MockReviewReactionRepository(
                reactionsFile = File(
                    File(appContext.filesDir, MOCK_DATA_DIRECTORY),
                    REACTIONS_FILE_NAME,
                ),
                placeRepository = placeRepository,
            )
        }

        /** Factory für lokale JVM-Tests ohne Android Context. */
        fun create(
            storageDirectory: File,
            placeRepository: PlaceRepository,
        ): MockReviewReactionRepository = MockReviewReactionRepository(
            reactionsFile = File(storageDirectory, REACTIONS_FILE_NAME),
            placeRepository = placeRepository,
        )
    }
}

private fun Review.withReactionTransition(
    previous: ReviewReactionType?,
    next: ReviewReactionType?,
): Review {
    val likeDelta = next.isType(ReviewReactionType.LIKE) - previous.isType(ReviewReactionType.LIKE)
    val dislikeDelta = next.isType(ReviewReactionType.DISLIKE) - previous.isType(ReviewReactionType.DISLIKE)
    return copy(
        likes = (likes + likeDelta).coerceAtLeast(0),
        dislikes = (dislikes + dislikeDelta).coerceAtLeast(0),
    )
}

private fun ReviewReactionType?.isType(expected: ReviewReactionType): Int = if (this == expected) 1 else 0

private data class ReviewReactionJson(
    val uuid: String,
    val reviewUuid: String,
    val userUuid: String,
    val type: String,
    val createdAtMillis: Long,
) {
    fun toDomain(): ReviewReaction = ReviewReaction(
        uuid = UUID.fromString(uuid),
        reviewUuid = UUID.fromString(reviewUuid),
        userUuid = UUID.fromString(userUuid),
        type = ReviewReactionType.valueOf(type),
        createdAtMillis = createdAtMillis,
    )

    companion object {
        fun from(reaction: ReviewReaction): ReviewReactionJson = ReviewReactionJson(
            uuid = reaction.uuid.toString(),
            reviewUuid = reaction.reviewUuid.toString(),
            userUuid = reaction.userUuid.toString(),
            type = reaction.type.name,
            createdAtMillis = reaction.createdAtMillis,
        )
    }
}
