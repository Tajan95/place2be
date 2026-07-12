package de.place2be.feature.onboarding

class OnboardingViewModel {
    fun getPages(): List<OnboardingPageUiState> = listOf(
        OnboardingPageUiState(
            title = "Willkommen bei place2be",
            body = "Entdecke öffentliche Orte, an denen du eine gute Zeit verbringen kannst, ohne zwingend Geld auszugeben.",
        ),
        OnboardingPageUiState(
            title = "Bewerte aktuelle Eindrücke",
            body = "Bewerte Orte nach Vibes, Sicherheit und Erreichbarkeit. Aktuelle Bewertungen zählen stärker als alte.",
        ),
        OnboardingPageUiState(
            title = "Standort & Mock-Map",
            body = "Der MVP nutzt eine vereinfachte Kartenansicht. Standortberechtigungen sind fachlich vorgesehen und können später echt geprüft werden.",
        ),
    )
}

data class OnboardingPageUiState(
    val title: String,
    val body: String,
)
