package de.place2be.feature.onboarding

/**
 * Bereitet die fachlichen Onboarding-Inhalte auf und kapselt den persistenten
 * Abschlussstatus. Derselbe Seiteninhalt wird sowohl beim ersten App-Start als
 * auch spaeter ueber die Hilfe im eigenen Profil verwendet.
 */
class OnboardingViewModel(
    private val completionStore: OnboardingCompletionStore,
) {
    fun shouldShowOnFirstLaunch(): Boolean = !completionStore.isCompleted()

    fun completeFirstLaunchOnboarding() {
        completionStore.markCompleted()
    }

    fun getPages(): List<OnboardingPageUiState> = listOf(
        OnboardingPageUiState(
            symbol = "◎",
            eyebrow = "PLACE2BE",
            title = "Orte fuer eine gute Zeit",
            body = "Entdecke oeffentliche, niedrigschwellige Orte, an denen du dich gerne aufhalten kannst, ohne zwingend Geld auszugeben.",
            highlights = listOf(
                "Orte vergleichen und speichern",
                "Aktuelle Eindruecke der Community nutzen",
            ),
        ),
        OnboardingPageUiState(
            symbol = "⌖",
            eyebrow = "MOCK-MAP",
            title = "Die Karte ist bewusst vereinfacht",
            body = "Der Local-first-MVP nutzt eine stilisierte Mock-Map. Marker und Kurzlisten fuehren in dieselbe erweiterbare Ortsansicht.",
            highlights = listOf(
                "Marker antippen und Bottom-Sheet hochziehen",
                "Filter, beliebte und gespeicherte Orte nutzen",
            ),
        ),
        OnboardingPageUiState(
            symbol = "3",
            eyebrow = "BEWERTUNG",
            title = "Drei Kriterien, ein aktuelles Bild",
            body = "Bewerte einen Ort direkt vor Ort auf einer Skala von 1 bis 5. Eine kurze Textrezension ist optional.",
            highlights = listOf(
                "Vibes: Atmosphaere und Aufenthaltsgefuehl",
                "Sicherheit: subjektives Sicherheitsgefuehl",
                "Erreichbarkeit: Zugang und praktische Anbindung",
            ),
        ),
        OnboardingPageUiState(
            symbol = "↻",
            eyebrow = "AKTUALITAET & STANDORT",
            title = "Neue Eindruecke zaehlen staerker",
            body = "Bewertungen verlieren mit der Zeit an Gewicht. So bildet der sichtbare Orts-Score Veraenderungen besser ab als ein dauerhaft statischer Durchschnitt.",
            highlights = listOf(
                "Bewerten ist fachlich an einen Vor-Ort-Aufenthalt gebunden",
                "Die Standortbestaetigung ist im MVP simuliert und fuer echte APIs vorbereitet",
            ),
        ),
    )
}

data class OnboardingPageUiState(
    val symbol: String,
    val eyebrow: String,
    val title: String,
    val body: String,
    val highlights: List<String>,
)

enum class OnboardingMode {
    FIRST_LAUNCH,
    HELP,
}
