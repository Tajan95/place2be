# Projektstruktur für den place2be-MVP

Dieses Dokument beschreibt die vorläufige Projektstruktur nach dem Meeting vom 12.07.2026. Die Struktur verbindet die bereits von Artem/Morris angelegte Grundidee einer Trennung zwischen `core`, `data`, `domain` und `feature` mit einer MVVM- und Feature-orientierten Android-Struktur.

## Ziel der Struktur

Die Struktur soll drei Dinge gleichzeitig leisten:

1. Die App soll bis zur Präsentation am 27.07.2026 pragmatisch umsetzbar bleiben.
2. UI, Datenmodell, Datenzugriff und fachliche Logik sollen sauber getrennt sein.
3. Eine spätere echte Datenquelle, Kartenintegration oder Standortprüfung soll möglich bleiben, ohne die UI komplett umzubauen.

## Vorgeschlagene Struktur

```text
de.place2be
├── MainActivity.kt
├── app
│   └── Place2BeApp.kt
├── core
│   └── location
│       └── LocationConfirmationState.kt
├── data
│   ├── mock
│   │   └── MockPlaceDataSource.kt
│   └── repository
│       ├── MockPlaceRepository.kt
│       └── MockUserRepository.kt
├── domain
│   ├── model
│   │   ├── Place.kt
│   │   ├── Bookmark.kt
│   │   ├── PlaceAttribute.kt
│   │   ├── PlaceCategory.kt
│   │   ├── Review.kt
│   │   └── User.kt
│   ├── repository
│   │   ├── PlaceRepository.kt
│   │   └── UserRepository.kt
│   └── usecase
│       └── CalculatePlaceScoreUseCase.kt
├── feature
│   ├── map
│   │   ├── MapScreen.kt
│   │   └── MapViewModel.kt
│   ├── onboarding
│   │   ├── OnboardingScreen.kt
│   │   └── OnboardingViewModel.kt
│   ├── placeDetail
│   │   ├── PlaceDetailScreen.kt
│   │   └── PlaceDetailViewModel.kt
│   └── rating
│       ├── RatingScreen.kt
│       └── RatingViewModel.kt
└── ui
    └── theme
```

## Begründung

### Feature-Pakete

Jeder größere Screen wird als eigenes Feature verstanden. Das entspricht der Meeting-Entscheidung, dass ein Screen jeweils eine `Screen`-Datei für die UI und eine `ViewModel`-Datei für die Logik bzw. den UI-Zustand bekommen soll.

Beispiele:

- `feature/map/MapScreen.kt` enthält die Mock-Map-UI.
- `feature/map/MapViewModel.kt` bereitet Daten für die Map vor.
- `feature/rating/RatingScreen.kt` enthält die Bewertungs-UI.
- `feature/rating/RatingViewModel.kt` verarbeitet Eingaben wie Vibes, Sicherheit und Erreichbarkeit.

### Domain-Schicht

Die Domain-Schicht enthält fachliche Konzepte, die unabhängig von Compose oder Android UI sind:

- Orte,
- Bewertungen,
- Kategorien,
- Tags/Ortseigenschaften,
- Nutzer,
- Score-Berechnung.

Die Score-Logik gehört bewusst nicht in die UI und auch nicht direkt in einen Screen. Sie liegt in `domain/usecase/CalculatePlaceScoreUseCase.kt`, damit sie testbar und in der Präsentation klar erklärbar bleibt.

### Data-Schicht

Die Data-Schicht liefert Daten für die App. Im MVP wird local-first mit JSON-Dateien gearbeitet. `app/src/main/data/mockdata` enthält die versionierten Startdaten und ist als Android-Assets-Quelle registriert. `MockPlaceDataSource` kopiert sie beim ersten Start in den internen App-Speicher und zentralisiert dort CRUD für Orte, Reviews, Nutzer und Bookmarks. Die Repository-Adapter übersetzen diesen Zugriff auf die Domain-Interfaces.

Später kann diese Schicht erweitert oder ersetzt werden durch:

- Room/SQLite,
- Firebase,
- REST-API,
- echte Karten-/Standortdaten.

Die UI sollte davon möglichst wenig wissen.

### Core-Schicht

Die Core-Schicht enthält übergreifende technische Hilfsstrukturen, die keinem einzelnen Feature gehören. Für den MVP ist vor allem die Standortbestätigung relevant, auch wenn die echte GPS-Prüfung noch vereinfacht oder simuliert werden kann.

## Verhältnis zu bestehendem Code

Im Repository wurden bereits erste Datenstrukturen und Platzhalter angelegt. Diese Struktur greift diese Vorarbeit auf, ordnet die fachlichen Datenmodelle aber klarer in `domain/model` ein. Die Data-Schicht soll vor allem Datenquellen und Repository-Implementierungen enthalten, nicht die fachliche Bedeutung der App-Objekte selbst.

## Wichtig für die Weiterarbeit

- UI-Code bleibt in `feature/*/*Screen.kt`.
- Screen-Zustand und Interaktionslogik liegen in `feature/*/*ViewModel.kt`.
- Fachliche Berechnungen liegen in `domain/usecase`.
- Fachliche Datenobjekte liegen in `domain/model`.
- Datenzugriff wird über `domain/repository` abstrahiert und in `data/repository` umgesetzt.
