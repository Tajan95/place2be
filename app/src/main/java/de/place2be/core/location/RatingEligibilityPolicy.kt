package de.place2be.core.location

/**
 * Kapselt die MVP-Regeln dafür, ob eine Bewertung abgegeben werden darf.
 *
 * Fachliche Idee: Nutzerinnen und Nutzer sollen einen Ort nur bewerten können,
 * wenn sie tatsächlich vor Ort sind. Für eine spätere echte Standortprüfung kann
 * zusätzlich eine Mindestaufenthaltsdauer berücksichtigt werden, damit kurze
 * Vorbeifahrten nicht ausreichen. Der simulierte Zustand bleibt für die Demo
 * bewusst als direkter Shortcut nutzbar.
 */
class RatingEligibilityPolicy(
    private val minimumStayMillis: Long = DEFAULT_MINIMUM_STAY_MILLIS,
) {
    fun evaluate(
        locationConfirmationState: LocationConfirmationState,
        onSiteSinceTimestampMillis: Long? = null,
        nowTimestampMillis: Long = System.currentTimeMillis(),
    ): RatingEligibilityState {
        return when (locationConfirmationState) {
            LocationConfirmationState.SIMULATED_CONFIRMED -> RatingEligibilityState(
                canRate = true,
                helperText = "Simulierte Vor-Ort-Bestätigung aktiv.",
                requiredStayMillis = minimumStayMillis,
                confirmedStayMillis = minimumStayMillis,
            )

            LocationConfirmationState.CONFIRMED_ON_SITE -> evaluateConfirmedStay(
                onSiteSinceTimestampMillis = onSiteSinceTimestampMillis,
                nowTimestampMillis = nowTimestampMillis,
            )

            LocationConfirmationState.PERMISSION_GRANTED -> RatingEligibilityState(
                canRate = false,
                helperText = "Standort ist erlaubt. Bleib kurz vor Ort, bevor du bewertest.",
                requiredStayMillis = minimumStayMillis,
                confirmedStayMillis = 0L,
            )

            LocationConfirmationState.PERMISSION_DENIED -> RatingEligibilityState(
                canRate = false,
                helperText = "Bewerten ist nur mit Standortfreigabe oder Demo-Bestätigung möglich.",
                requiredStayMillis = minimumStayMillis,
                confirmedStayMillis = 0L,
            )

            LocationConfirmationState.NOT_REQUESTED,
            LocationConfirmationState.PERMISSION_REQUESTED,
            -> RatingEligibilityState(
                canRate = false,
                helperText = "Geh hin und sag uns, was du denkst!",
                requiredStayMillis = minimumStayMillis,
                confirmedStayMillis = 0L,
            )
        }
    }

    private fun evaluateConfirmedStay(
        onSiteSinceTimestampMillis: Long?,
        nowTimestampMillis: Long,
    ): RatingEligibilityState {
        val confirmedStayMillis = onSiteSinceTimestampMillis
            ?.let { (nowTimestampMillis - it).coerceAtLeast(0L) }
            ?: 0L
        val remainingMillis = (minimumStayMillis - confirmedStayMillis).coerceAtLeast(0L)

        return if (remainingMillis == 0L) {
            RatingEligibilityState(
                canRate = true,
                helperText = "Du bist vor Ort und kannst bewerten.",
                requiredStayMillis = minimumStayMillis,
                confirmedStayMillis = confirmedStayMillis,
            )
        } else {
            RatingEligibilityState(
                canRate = false,
                helperText = "Bleib noch kurz vor Ort, bevor du bewertest.",
                requiredStayMillis = minimumStayMillis,
                confirmedStayMillis = confirmedStayMillis,
            )
        }
    }

    private companion object {
        const val DEFAULT_MINIMUM_STAY_MILLIS = 90_000L
    }
}

data class RatingEligibilityState(
    val canRate: Boolean,
    val helperText: String,
    val requiredStayMillis: Long,
    val confirmedStayMillis: Long,
) {
    val remainingStayMillis: Long
        get() = (requiredStayMillis - confirmedStayMillis).coerceAtLeast(0L)
}
