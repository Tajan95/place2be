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

Die eigentliche Ortsdetail- und Bewertungsinteraktion ist im MVP in `MapScreenWithRatingEntry` integriert. Die separaten `PlaceDetailScreen`- und `RatingScreen`-Dateien bilden weiterhin die fachliche Feature-Struktur beziehungsweise vorbereitete Einzelkomponenten ab, sind aber nicht der primäre Navigationspfad der Live-Demo.

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

## Stateful und stateless Composables

Jetpack Compose trennt nicht zwingend nach eigenen Klassen zwischen stateful und stateless. Entscheidend ist, wo der Zustand gehalten wird und ob eine Komponente ihre Darstellung vollständig über Parameter erhält.

### Stateless Composables

Stateless Composables:

- erhalten ihren sichtbaren Zustand über Parameter,
- melden Interaktionen über Callbacks nach außen,
- besitzen keine eigene fachliche Datenquelle,
- lassen sich leichter wiederverwenden, testen und in Previews darstellen.

Typische Beispiele im Projekt sind kleine Darstellungskomponenten wie:

- Rating-Pills,
- Filterzeilen,
- Listenzeilen für Orte,
- Review- oder Profil-Unterkomponenten, sofern Daten und Aktionen vollständig übergeben werden.

Eine stateless Signatur folgt sinngemäß diesem Muster:

```kotlin
@Composable
fun FilterOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
)
```

Die Komponente kennt weder Repository noch JSON-Dateien und entscheidet nicht selbst, welche Option fachlich ausgewählt ist.

### Stateful Composables

Stateful Composables:

- halten oder koordinieren UI-Zustand,
- verwenden beispielsweise `remember` oder `rememberSaveable`,
- kombinieren mehrere Datenquellen oder State-Holder,
- geben vorbereiteten Zustand an untergeordnete Komponenten weiter.

`Place2BeApp` bildet im MVP die stateful **Composition Root**. Dort werden Repositories, Use Cases, Navigation, ausgewählter Ort, betrachtetes Profil und `dataRevision` zusammengeführt.

Auch `MapScreenWithRatingEntry` enthält bewusst lokalen UI-Zustand, etwa für:

- Sliderwerte,
- optionalen Rezensionstext,
- Auf- und Einklappen des Textfelds,
- Review-Sortierung,
- Speicherbestätigung,
- Scrollposition des Bottom-Sheets.

### State Hoisting

Das bevorzugte Muster lautet:

```text
Stateful Parent
  → gibt Zustand und Callbacks weiter
Stateless Child
  → rendert Daten und meldet Aktionen zurück
```

Die Fachlogik bleibt dabei außerhalb der UI. Ein Composable darf beispielsweise anzeigen, ob ein Ort gespeichert ist, aber die persistente Bookmark-Operation wird über einen Callback an Repository- beziehungsweise App-Schicht zurückgegeben.

### Pragmatische MVP-Grenze

Der aktuelle MVP ist nicht vollständig „stateless bis zum letzten Screen“. Einige komplexe Interaktionszustände verbleiben lokal im integrierten Map-/Detail-Flow, um die Demo stabil und den Codeumfang kontrollierbar zu halten.

Eine spätere technische Weiterentwicklung könnte:

- mehr Zustand in native Lifecycle-`ViewModel`-Klassen verschieben,
- `SavedStateHandle` für wiederherstellbaren Navigationszustand verwenden,
- Navigation Compose integrieren,
- komplexe Screens in kleinere State-Holder und stateless Unterkomponenten zerlegen.

## Feature-Schicht

### Map

- `MapScreen.kt` zeichnet die stilisierte Mock-Map, Marker und Default-Panels.
- `MapViewModel.kt` bereitet Orte, Scores, Tags und Bookmark-Zustände für die Karte auf.
- `MapScreenWithRatingEntry.kt` verbindet Preview, Detailansicht, Bewertung und Reviews.

### Rating und Reviews

- `RatingViewModel.kt` validiert und speichert Bewertungen.
- `ReviewSubmissionCooldownPolicy` erzwingt die 24-Stunden-Regel.
- Reviews erscheinen nach dem Speichern sofort erneut aus dem Repository.
- Zeitbasierte und Beliebt-Sortierung, Text-Aufklappen und Reaktionen liegen im integrierten Ortsflow.

Die sichtbare zeitbasierte Sortierung wird mit Issue #36 von `Rezent` zu `Zuletzt` umbenannt; der interne Wert `RECENT` kann bestehen bleiben.

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
- Ein Teil des komplexen Screen-Zustands verbleibt lokal in Composables.
- Standortbestätigung ist simuliert.
- Daten sind lokal und nicht zwischen Geräten synchronisiert.

Diese Punkte sind dokumentierte technische Erweiterungen und keine versteckten produktiven Eigenschaften.

## Regeln für die Weiterarbeit

- UI bleibt in `feature/*/*Screen.kt`.
- Screen-Aufbereitung bleibt in ViewModel-artigen Klassen.
- Fachliche Regeln liegen in `domain/usecase`.
- Datenobjekte liegen in `domain/model`.
- Datenzugriff erfolgt über `domain/repository`.
- Zustand wird nach Möglichkeit nach oben verlagert und über Parameter weitergegeben.
- Untergeordnete Darstellungskomponenten bleiben möglichst stateless.
- Öffentliche Profile erhalten ohne neue Datenschutzentscheidung keine chronologische Ortshistorie.
- Eine spätere Backend- oder Account-Anbindung darf Local-first-Entscheidungen nicht stillschweigend in öffentliche Datenfreigaben umwandeln.
