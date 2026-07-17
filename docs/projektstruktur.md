# Projektstruktur des place2be-MVP

Dieses Dokument beschreibt den finalen MVP-Stand nach der Umsetzung von Map-, Bewertungs-, Review-, Profil-, Nutzer-Score- und Onboarding-Funktionen.

## Ziele der Struktur

1. UI, Datenmodelle, Datenzugriff und fachliche Regeln bleiben getrennt.
2. Der Demo-Flow ist bis zur Präsentation am 27.07.2026 stabil und nachvollziehbar.
3. Spätere Karten-, Backend-, Account- oder Standortlösungen können ergänzt werden, ohne die Domain-Logik vollständig neu zu schreiben.

## Aktuelle Paketstruktur

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
│   │   ├── OnboardingCompletionStore.kt
│   │   ├── OnboardingScreen.kt
│   │   └── OnboardingViewModel.kt
│   ├── placeDetail
│   │   ├── PlaceDetailScreen.kt
│   │   └── PlaceDetailViewModel.kt
│   ├── profile
│   │   ├── ProfileScreen.kt
│   │   ├── ProfileScreenWithOnboardingHelp.kt
│   │   └── ProfileViewModel.kt
│   └── rating
│       ├── RatingScreen.kt
│       └── RatingViewModel.kt
└── ui
    ├── component
    └── theme
```

## Aktiver Demo-Flow

`Place2BeApp.kt` setzt die Repositories, Use Cases und ViewModel-artigen Klassen zusammen. Der aktive Ablauf lautet:

```text
Erststart-Onboarding
        ↓
Mock-Map
        ↓
Mini-Preview
        ↓
erweitertes Bottom-Sheet
        ↓
Bewertung + Textrezension + vorhandene Reviews
        ↓
eigenes oder öffentliches Profil
```

Die eigentliche Ortsdetail- und Bewertungsinteraktion ist im MVP in `MapScreenWithRatingEntry` integriert. Die separaten `PlaceDetailScreen`- und `RatingScreen`-Dateien bilden weiterhin die fachliche Feature-Struktur bzw. vorbereitete Einzelkomponenten ab, sind aber nicht der primäre Navigationspfad der Live-Demo.

## App-Schicht

`Place2BeApp.kt` verantwortet:

- Erstellung der Local-first-Datenquelle,
- Repository-Zusammensetzung,
- Auswahl des Demo-Nutzers,
- Map-, Profil- und Onboarding-Zielzustände,
- Weitergabe von Interaktions-Callbacks,
- Aktualisierung über `dataRevision`,
- Erhalt des Map- und Bottom-Sheet-Kontexts unter Profil- und Hilfe-Overlays.

Die Navigation wird im MVP über `rememberSaveable` und einen kleinen Zielzustand umgesetzt. Navigation Compose bleibt eine spätere technische Erweiterung.

## Feature-Schicht

### Map

- `MapScreen.kt` zeichnet die stilisierte Mock-Map, Marker und Default-Panels.
- `MapViewModel.kt` bereitet Orte, Scores, Tags und Bookmark-Zustände für die Karte auf.
- `MapScreenWithRatingEntry.kt` verbindet Preview, Detailansicht, Bewertung und Reviews.

### Rating und Reviews

- `RatingViewModel.kt` validiert und speichert Bewertungen.
- `ReviewSubmissionCooldownPolicy` erzwingt die 24-Stunden-Regel.
- Reviews erscheinen nach dem Speichern sofort erneut aus dem Repository.
- Rezent- und Beliebt-Sortierung, Text-Aufklappen und Reaktionen liegen im integrierten Ortsflow.

### Profil

`ProfileViewModel` erzeugt zwei klar getrennte Zustände:

- `OWN`: vollständige private Historie, Score-Aufteilung und private Aktionen,
- `PUBLIC`: aggregierte Kennzahlen ohne Orts- oder Bewegungschronologie.

Fremde Profile sind über Autorenbereiche in Rezensionen erreichbar. `ProfileScreenWithOnboardingHelp.kt` ergänzt für das eigene Profil den Zugang zum vollständigen Hilfefluss.

### Onboarding

- `OnboardingViewModel.kt` liefert die vier Erklärseiten.
- `OnboardingCompletionStore.kt` abstrahiert den persistenten Abschlussstatus.
- `OnboardingScreen.kt` wird sowohl beim Erststart als auch im Hilfe-Modus verwendet.

Der Android-Store verwendet app-interne `SharedPreferences` und speichert nur einen booleschen Abschlusswert.

## Domain-Schicht

Die Domain-Schicht enthält fachliche Konzepte ohne Compose-Abhängigkeit:

- Orte, Kategorien und Tags,
- Bewertungen und Textrezensionen,
- Bookmarks,
- accountgebundene Review-Reaktionen,
- Nutzer und Nutzer-Score-Ergebnis,
- zeitgewichteten Orts-Score,
- Aktivitäts- und Reputationsregeln,
- Bewertungs-Cooldown.

`CalculatePlaceScoreUseCase` und `CalculateUserScoreUseCase` halten die komplexeren Regeln bewusst außerhalb der UI und werden durch lokale Unit-Tests abgesichert.

## Data-Schicht

`app/src/main/data/mockdata` enthält versionierte Startdaten. Beim ersten Start kopiert `MockPlaceDataSource` sie in den internen App-Speicher.

Die Datenquelle übernimmt:

- CRUD für Orte, Reviews, Nutzer und Bookmarks,
- persistente Arbeitskopien statt Änderungen an Assets,
- Begrenzung auf 50 gespeicherte Rezensionstexte pro Ort,
- Erhalt verdrängter numerischer Bewertungsdaten.

`MockReviewReactionRepository` persistiert accountgebundene Likes und Dislikes separat. Repository-Adapter stellen der Domain-Schicht stabile Interfaces bereit.

## Demo-Nutzer und Account-Abgrenzung

Der Local-first-MVP verwendet einen festen pseudonymisierten Demo-Nutzer. Es gibt keinen Registrierungs-, Login- oder Profilerstellungsprozess. Das Profil demonstriert Score, Historie und Datenschutz; ein produktiver Account-Lifecycle ist bewusst nicht Bestandteil der aktuellen Struktur.

Eine spätere Account-Lösung müsste zusätzliche Komponenten für Authentifizierung, Session, Kontowiederherstellung, Einwilligungen, Löschung und Synchronisation ergänzen.

## Technische MVP-Kompromisse

- ViewModel-artige Klassen leiten nicht durchgängig von `androidx.lifecycle.ViewModel` ab.
- Navigation Compose wird nicht verwendet.
- Dependency Injection erfolgt manuell in `Place2BeApp`.
- Standortbestätigung ist simuliert.
- Daten sind lokal und nicht zwischen Geräten synchronisiert.

Diese Punkte sind dokumentierte technische Erweiterungen und keine versteckten produktiven Eigenschaften.

## Regeln für die Weiterarbeit

- UI bleibt in `feature/*/*Screen.kt`.
- Screen-Aufbereitung bleibt in ViewModel-artigen Klassen.
- Fachliche Regeln liegen in `domain/usecase`.
- Datenobjekte liegen in `domain/model`.
- Datenzugriff erfolgt über `domain/repository`.
- Öffentliche Profile erhalten ohne neue Datenschutzentscheidung keine chronologische Ortshistorie.
- Eine spätere Backend- oder Account-Anbindung darf Local-first-Entscheidungen nicht stillschweigend in öffentliche Datenfreigaben umwandeln.