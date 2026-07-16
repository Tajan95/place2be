# Projektstruktur für den place2be-MVP

Dieses Dokument beschreibt die vorläufige Projektstruktur nach dem Meeting vom 12.07.2026. Die Struktur verbindet die bereits von Artem/Morris angelegte Grundidee einer Trennung zwischen `core`, `data`, `domain` und `feature` mit einer MVVM- und Feature-orientierten Android-Struktur.

## Ziel der Struktur

Die Struktur soll drei Dinge gleichzeitig leisten:

1. Die App soll bis zur Präsentation am 27.07.2026 pragmatisch umsetzbar bleiben.
2. UI, Datenmodell, Datenzugriff und fachliche Logik sollen sauber getrennt sein.
3. Eine spätere echte Datenquelle, Kartenintegration oder Standortprüfung soll möglich bleiben, ohne die UI komplett umzubauen.

## Aktuelle Struktur

```text
de.place2be
├── MainActivity.kt
├── app
│   └── Place2BeApp.kt
├── core
│   └── location
│       ├── LocationConfirmationState.kt
│       └── RatingEligibilityPolicy.kt
├── data
│   ├── mock
│   │   └── MockPlaceDataSource.kt
│   └── repository
│       ├── MockPlaceRepository.kt
│       ├── MockReviewReactionRepository.kt
│       └── MockUserRepository.kt
├── domain
│   ├── model
│   │   ├── Bookmark.kt
│   │   ├── Place.kt
│   │   ├── PlaceAttribute.kt
│   │   ├── PlaceCategory.kt
│   │   ├── Review.kt
│   │   ├── ReviewReaction.kt
│   │   ├── User.kt
│   │   └── UserScoreResult.kt
│   ├── repository
│   │   ├── PlaceRepository.kt
│   │   ├── ReviewReactionRepository.kt
│   │   └── UserRepository.kt
│   └── usecase
│       ├── CalculatePlaceScoreUseCase.kt
│       ├── CalculateUserScoreUseCase.kt
│       └── ReviewSubmissionCooldownPolicy.kt
├── feature
│   ├── map
│   │   ├── MapScreen.kt
│   │   ├── MapScreenWithRatingEntry.kt
│   │   └── MapViewModel.kt
│   ├── onboarding
│   │   ├── OnboardingScreen.kt
│   │   └── OnboardingViewModel.kt
│   ├── placeDetail
│   │   ├── PlaceDetailScreen.kt
│   │   └── PlaceDetailViewModel.kt
│   ├── profile
│   │   ├── ProfileScreen.kt
│   │   └── ProfileViewModel.kt
│   └── rating
│       ├── RatingScreen.kt
│       └── RatingViewModel.kt
└── ui
    ├── component
    └── theme
```

## Begründung

### Feature-Pakete

Jeder größere Screen wird als eigenes Feature verstanden. Das entspricht der Meeting-Entscheidung, dass ein Screen jeweils eine `Screen`-Datei für die UI und eine `ViewModel`-Datei für Logik bzw. UI-Zustand bekommen soll.

Beispiele:

- `feature/map/MapScreen.kt` enthält die Mock-Map-UI.
- `feature/map/MapViewModel.kt` bereitet Daten für die Map vor.
- `feature/rating/RatingScreen.kt` enthält die Bewertungs-UI.
- `feature/rating/RatingViewModel.kt` verarbeitet Eingaben wie Vibes, Sicherheit und Erreichbarkeit.
- `feature/profile/ProfileScreen.kt` zeigt Nutzer-Score, aggregierte Kennzahlen und die private eigene Historie.
- `feature/profile/ProfileViewModel.kt` trennt gemäß ADR-010 zwischen eigenem Profil und öffentlicher Zusammenfassung.

### Domain-Schicht

Die Domain-Schicht enthält fachliche Konzepte, die unabhängig von Compose oder Android UI sind:

- Orte,
- Bewertungen,
- Kategorien,
- Tags/Ortseigenschaften,
- Nutzer,
- Bookmarks und Review-Reaktionen,
- Orts- und Nutzer-Score-Berechnung.

Die Score-Logik gehört bewusst nicht in die UI und auch nicht direkt in einen Screen. Sie liegt in `domain/usecase/CalculatePlaceScoreUseCase.kt` und `domain/usecase/CalculateUserScoreUseCase.kt`, damit sie testbar und in der Präsentation klar erklärbar bleibt.

### Data-Schicht

Die Data-Schicht liefert Daten für die App. Im MVP wird local-first mit JSON-Dateien gearbeitet. `app/src/main/data/mockdata` enthält die versionierten Startdaten und ist als Android-Assets-Quelle registriert. `MockPlaceDataSource` kopiert sie beim ersten Start in den internen App-Speicher und zentralisiert dort CRUD für Orte, Reviews, Nutzer und Bookmarks. Review-Reaktionen werden ebenfalls lokal persistiert. Die Repository-Adapter übersetzen diesen Zugriff auf die Domain-Interfaces.

Später kann diese Schicht erweitert oder ersetzt werden durch:

- Room/SQLite,
- Firebase,
- REST-API,
- echte Karten-/Standortdaten.

Die UI sollte davon möglichst wenig wissen.

### Core-Schicht

Die Core-Schicht enthält übergreifende technische Hilfsstrukturen, die keinem einzelnen Feature gehören. Für den MVP ist vor allem die Standortbestätigung relevant, auch wenn die echte GPS-Prüfung noch vereinfacht oder simuliert werden kann.

## Profil und Datenschutz

Das Profil-Feature greift ausschließlich über Repository-Interfaces und Domain-Use-Cases auf Daten zu. `ProfileViewModel` erzeugt zwei fachlich getrennte Ansichten:

- `OWN`: vollständige private Bewertungs-Historie, Score-Aufteilung, Hilfe und vorbereitete Einstellungen,
- `PUBLIC`: nur aggregierte Profilwerte ohne chronologische Orts- oder Bewegungshistorie.

Im Local-first-MVP ist zunächst nur das eigene Demo-Profil direkt erreichbar. Die öffentliche Variante ist im UI-Zustand vorbereitet und wird durch Unit-Tests abgesichert.

## Wichtig für die Weiterarbeit

- UI-Code bleibt in `feature/*/*Screen.kt`.
- Screen-Zustand und Interaktionslogik liegen in `feature/*/*ViewModel.kt`.
- Fachliche Berechnungen liegen in `domain/usecase`.
- Fachliche Datenobjekte liegen in `domain/model`.
- Datenzugriff wird über `domain/repository` abstrahiert und in `data/repository` umgesetzt.
- Öffentliche Profilansichten dürfen keine chronologische Bewertungs- oder Bewegungshistorie erhalten, ohne dass die Datenschutzentscheidung bewusst neu bewertet wird.
