package de.place2be.domain.model

/**
 * Erklärbares Ergebnis der dynamischen Nutzer-Score-Berechnung.
 *
 * Der öffentlich sichtbare Gesamtwert bleibt ein nach oben offener Integer.
 * Aktivität und Reputation werden getrennt berechnet, damit später auf der
 * Profilseite nachvollziehbar bleibt, wodurch der Score entstanden ist.
 */
data class UserScoreResult(
    val activityPoints: Int,
    val reputationPoints: Int,
    val ratingActivityPoints: Int,
    val textReviewActivityPoints: Int,
    val reactionActivityPoints: Int,
) {
    val totalScore: Int
        get() = activityPoints + reputationPoints

    init {
        require(activityPoints >= 0) { "Activity points must not be negative." }
        require(reputationPoints >= 0) { "Reputation points must not be negative." }
        require(ratingActivityPoints >= 0) { "Rating activity points must not be negative." }
        require(textReviewActivityPoints >= 0) { "Text review activity points must not be negative." }
        require(reactionActivityPoints >= 0) { "Reaction activity points must not be negative." }
        require(
            activityPoints == ratingActivityPoints + textReviewActivityPoints + reactionActivityPoints,
        ) { "Activity points must equal the sum of all activity components." }
    }

    companion object {
        val EMPTY = UserScoreResult(
            activityPoints = 0,
            reputationPoints = 0,
            ratingActivityPoints = 0,
            textReviewActivityPoints = 0,
            reactionActivityPoints = 0,
        )
    }
}
