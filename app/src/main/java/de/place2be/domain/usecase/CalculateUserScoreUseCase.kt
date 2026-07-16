package de.place2be.domain.usecase

import de.place2be.domain.model.Place
import de.place2be.domain.model.Review
import de.place2be.domain.model.ReviewReaction
import de.place2be.domain.model.UserScoreResult
import java.util.UUID
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Berechnet den Nutzer-Score dynamisch aus Bewertungen und aktiven Reaktionen.
 *
 * Es werden keine Punkte dauerhaft hochgezählt. Dadurch bleiben entfernte oder
 * gewechselte Reaktionen, gelöschte Daten und spätere Regelanpassungen
 * konsistent. Der Gesamtwert besteht aus Aktivitäts- und Reputationspunkten.
 */
class CalculateUserScoreUseCase(
    private val proximityRadiusMeters: Double = DEFAULT_PROXIMITY_RADIUS_METERS,
    private val nowMillisProvider: () -> Long = System::currentTimeMillis,
) {
    fun calculate(
        userUuid: UUID,
        places: List<Place>,
        reviews: List<Review>,
        reactions: List<ReviewReaction>,
    ): UserScoreResult {
        val placesByUuid = places.associateBy(Place::uuid)
        val reviewsByUuid = reviews.associateBy(Review::uuid)
        val userReviews = reviews
            .filter { it.userUuid == userUuid }
            .sortedWith(compareBy<Review>(Review::timestampMillis).thenBy { it.uuid.toString() })

        val ratingActivityPoints = calculateRatingActivityPoints(
            userReviews = userReviews,
            placesByUuid = placesByUuid,
        )
        val textReviewActivityPoints = userReviews.count { review ->
            review.text?.trim()?.length?.let { it >= MIN_TEXT_REVIEW_LENGTH } == true
        } * TEXT_REVIEW_BONUS_POINTS
        val reactionActivityPoints = calculateReactionActivityPoints(
            userUuid = userUuid,
            reactions = reactions,
            reviewsByUuid = reviewsByUuid,
        )
        val reputationPoints = calculateReputationPoints(
            userReviews = userReviews,
            allReviews = reviews,
        )
        val activityPoints = ratingActivityPoints + textReviewActivityPoints + reactionActivityPoints

        return UserScoreResult(
            activityPoints = activityPoints,
            reputationPoints = reputationPoints,
            ratingActivityPoints = ratingActivityPoints,
            textReviewActivityPoints = textReviewActivityPoints,
            reactionActivityPoints = reactionActivityPoints,
        )
    }

    private fun calculateRatingActivityPoints(
        userReviews: List<Review>,
        placesByUuid: Map<UUID, Place>,
    ): Int = userReviews.mapIndexed { index, review ->
        val previousReviews = userReviews.subList(0, index)
        val previousVisitsToPlace = previousReviews.count { it.placeUuid == review.placeUuid }
        val explorationBonus = (MAX_EXPLORATION_BONUS - previousVisitsToPlace).coerceAtLeast(0)

        val currentPlace = placesByUuid[review.placeUuid]
        val nearbyPlacesInWindow = if (currentPlace == null) {
            0
        } else {
            previousReviews
                .asSequence()
                .filter { previous ->
                    val ageMillis = review.timestampMillis - previous.timestampMillis
                    previous.placeUuid != review.placeUuid && ageMillis in 0L..PROXIMITY_WINDOW_MILLIS
                }
                .mapNotNull { previous -> placesByUuid[previous.placeUuid] }
                .filter { previousPlace -> distanceMeters(currentPlace, previousPlace) <= proximityRadiusMeters }
                .map(Place::uuid)
                .distinct()
                .count()
        }
        val proximityPenalty = (
            nearbyPlacesInWindow * PROXIMITY_PENALTY_PER_PLACE
        ).coerceAtMost(MAX_PROXIMITY_PENALTY)

        (BASE_RATING_POINTS + explorationBonus - proximityPenalty)
            .coerceAtLeast(MINIMUM_RATING_POINTS)
    }.sum()

    private fun calculateReactionActivityPoints(
        userUuid: UUID,
        reactions: List<ReviewReaction>,
        reviewsByUuid: Map<UUID, Review>,
    ): Int {
        val rewardedPerPlace = mutableMapOf<UUID, Int>()
        val rewardedPerDay = mutableMapOf<Long, Int>()
        var points = 0

        reactions
            .asSequence()
            .filter { it.userUuid == userUuid }
            .sortedWith(compareBy<ReviewReaction>(ReviewReaction::createdAtMillis).thenBy { it.uuid.toString() })
            .forEach { reaction ->
                val review = reviewsByUuid[reaction.reviewUuid] ?: return@forEach
                if (review.userUuid == userUuid) return@forEach

                val placeUuid = review.placeUuid
                val day = reaction.createdAtMillis / MILLIS_PER_DAY
                val placeRewardCount = rewardedPerPlace[placeUuid] ?: 0
                val dayRewardCount = rewardedPerDay[day] ?: 0

                if (
                    placeRewardCount < MAX_REWARDED_REACTIONS_PER_PLACE &&
                    dayRewardCount < MAX_REACTION_POINTS_PER_DAY
                ) {
                    points += REACTION_ACTIVITY_POINTS
                    rewardedPerPlace[placeUuid] = placeRewardCount + 1
                    rewardedPerDay[day] = dayRewardCount + 1
                }
            }

        return points
    }

    private fun calculateReputationPoints(
        userReviews: List<Review>,
        allReviews: List<Review>,
    ): Int {
        val nowMillis = nowMillisProvider()
        val recentThreshold = (nowMillis - REPUTATION_ACTIVITY_WINDOW_MILLIS).coerceAtLeast(0L)
        val recentReviewCountsByPlace = allReviews
            .asSequence()
            .filter { it.timestampMillis in recentThreshold..nowMillis }
            .groupingBy(Review::placeUuid)
            .eachCount()

        return userReviews
            .asSequence()
            .filter { !it.text.isNullOrBlank() }
            .sumOf { review ->
                val netPositiveReactions = (review.likes - review.dislikes).coerceAtLeast(0)
                if (netPositiveReactions == 0) return@sumOf 0

                val logarithmicBase = REPUTATION_LOG_MULTIPLIER * ln(1.0 + netPositiveReactions)
                val recentPlaceActivity = (recentReviewCountsByPlace[review.placeUuid] ?: 0).coerceAtLeast(1)
                val placeActivityFactor = sqrt(
                    REPUTATION_REFERENCE_REVIEW_COUNT / recentPlaceActivity.toDouble(),
                ).coerceIn(MIN_PLACE_ACTIVITY_FACTOR, MAX_PLACE_ACTIVITY_FACTOR)

                (logarithmicBase * placeActivityFactor)
                    .roundToInt()
                    .coerceIn(0, MAX_REPUTATION_POINTS_PER_REVIEW)
            }
    }

    private fun distanceMeters(first: Place, second: Place): Double {
        val firstLatitude = first.latitude ?: return Double.POSITIVE_INFINITY
        val firstLongitude = first.longitude ?: return Double.POSITIVE_INFINITY
        val secondLatitude = second.latitude ?: return Double.POSITIVE_INFINITY
        val secondLongitude = second.longitude ?: return Double.POSITIVE_INFINITY

        val latitudeDelta = Math.toRadians(secondLatitude - firstLatitude)
        val longitudeDelta = Math.toRadians(secondLongitude - firstLongitude)
        val firstLatitudeRadians = Math.toRadians(firstLatitude)
        val secondLatitudeRadians = Math.toRadians(secondLatitude)
        val haversine = sin(latitudeDelta / 2).pow(2) +
            cos(firstLatitudeRadians) * cos(secondLatitudeRadians) * sin(longitudeDelta / 2).pow(2)
        return 2 * EARTH_RADIUS_METERS * asin(sqrt(haversine.coerceIn(0.0, 1.0)))
    }

    companion object {
        const val BASE_RATING_POINTS = 1
        const val MAX_EXPLORATION_BONUS = 9
        const val PROXIMITY_PENALTY_PER_PLACE = 5
        const val MAX_PROXIMITY_PENALTY = 10
        const val MINIMUM_RATING_POINTS = 1
        const val MIN_TEXT_REVIEW_LENGTH = 20
        const val TEXT_REVIEW_BONUS_POINTS = 2
        const val MAX_REWARDED_REACTIONS_PER_PLACE = 3
        const val MAX_REACTION_POINTS_PER_DAY = 6
        const val REACTION_ACTIVITY_POINTS = 1
        const val MAX_REPUTATION_POINTS_PER_REVIEW = 10

        private const val DEFAULT_PROXIMITY_RADIUS_METERS = 100.0
        private const val REPUTATION_LOG_MULTIPLIER = 3.0
        private const val REPUTATION_REFERENCE_REVIEW_COUNT = 20.0
        private const val MIN_PLACE_ACTIVITY_FACTOR = 0.75
        private const val MAX_PLACE_ACTIVITY_FACTOR = 1.25
        private const val EARTH_RADIUS_METERS = 6_371_000.0
        private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1_000L
        private const val PROXIMITY_WINDOW_MILLIS = MILLIS_PER_DAY
        private const val REPUTATION_ACTIVITY_WINDOW_MILLIS = 365L * MILLIS_PER_DAY
    }
}
