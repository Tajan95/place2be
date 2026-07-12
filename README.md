# place2be

place2be ist eine Android-App, die Nutzerinnen und Nutzern Empfehlungen für öffentliche Orte gibt, an denen sie eine schöne Zeit verbringen können. Im Mittelpunkt stehen frei zugängliche Orte wie Parks, Plätze, Promenaden, Einkaufsmeilen oder andere urbane Treffpunkte, die sich besonders für kostenlose oder niedrigschwellige Freizeitaktivitäten eignen.

## Kurzbeschreibung

place2be hilft Menschen dabei, spontan schöne öffentliche Orte zu entdecken und einzuschätzen, ob sich ein Besuch aktuell lohnt. Nutzerinnen und Nutzer können Orte ansehen, bewerten und damit deren Ranking beeinflussen.

Ein zentrales Konzept ist, dass Bewertungen nicht dauerhaft gleich stark zählen: Ältere Bewertungen verlieren mit der Zeit an Gewicht. Dadurch bleibt das Ranking dynamisch und bildet besser ab, dass Orte sich verändern können.

## Projektkontext

Dieses Projekt entsteht im Rahmen des Moduls **Mobile Software-Entwicklung am Beispiel der Android-Plattform**. Ziel ist ein funktionsfähiger Android-MVP, der die wichtigsten Konzepte aus der Veranstaltung praktisch anwendet: mobile UX, agile Entwicklung, Android-Architektur, Daten- und Zustandsmanagement, Performance sowie grundlegende IT-Sicherheit.

## MVP nach Meeting vom 12.07.2026

Im MVP konzentriert sich place2be auf:

- Onboarding beim ersten Start mit später erneut aufrufbaren Hilfs-/Hinweisfunktionen
- Start auf einer Map-Ansicht nach abgeschlossenem Onboarding
- Mock-Map statt echter Live-Kartenintegration
- Anzeige öffentlicher, primär kostenlos nutzbarer Orte
- Detailansicht eines ausgewählten Ortes
- kurze Bewertung über die drei Kriterien **Vibes**, **Sicherheit** und **Erreichbarkeit**
- dynamischer Score durch Community-Feedback
- stärkere Gewichtung aktueller Bewertungen gegenüber älteren Bewertungen
- Tags/Ortseigenschaften wie Sitzmöglichkeiten, Schatten, Barrierefreiheit oder öffentliche Toiletten
- lokale/prototypische Datenhaltung mit sauberer Architektur für spätere Backend-Anbindung
- Standortberechtigungen bzw. Standortbestätigung werden fachlich berücksichtigt, echte Prüfung kann im MVP vereinfacht oder simuliert sein

## Nice-to-have / spätere Erweiterungen

- echte GPS-basierte Standortprüfung
- echte Kartenintegration
- Filteransicht nach Tags
- Nutzerprofil
- Einstellungen
- Punkte als Gamification-Anreiz für abgegebene Bewertungen
- Ranglisten oder Vergleich mit Kontakten
- neue Orte vorschlagen
- zentrale Datenbank / Backend / Firebase / REST-API
- zeitabhängige Events, z. B. Wochenmarkt nur zu bestimmten Zeiten

## Bewertungskriterien

Die Nutzerbewertung erfolgt über drei Kriterien:

1. **Vibes** – sichtbarer App-Begriff für Atmosphäre, Stimmung und Unterhaltungspotential.
2. **Sicherheit** – wie sicher der Ort aktuell wirkt.
3. **Erreichbarkeit** – wie gut der Ort erreichbar bzw. zugänglich ist.

Diese Bewertungskriterien sind von Tags/Ortseigenschaften getrennt. Tags beschreiben objektivere Merkmale eines Ortes, während die Bewertung den aktuellen subjektiven Eindruck der Community abbildet.

## Ranking-System

Das Ranking eines Ortes basiert auf dem Feedback der Community. Positive Bewertungen lassen einen Ort steigen, negative Rückmeldungen können ihn sinken lassen. Aspekte wie Vibes, Sicherheit und Erreichbarkeit fließen in die Bewertung ein.

Damit das Ranking aktuell bleibt, wird das Bewertungsalter berücksichtigt: Neuere Bewertungen zählen stärker als ältere Bewertungen. Dadurch können Orte, die sich verschlechtern, im Ranking fallen, während ehemals schlecht bewertete Orte durch neue positive Rückmeldungen wieder steigen können.

## Architektur

Der MVP wird vorläufig **local-first** umgesetzt. Das bedeutet: Die App arbeitet zunächst mit Mock-Daten, In-Memory-Daten oder später optional lokaler Persistenz. Gleichzeitig soll die Architektur so getrennt werden, dass eine spätere reale Backend-Anbindung möglich bleibt.

Die App ist MVVM- und Feature-orientiert strukturiert:

- `feature/*/*Screen.kt` für Compose-UI
- `feature/*/*ViewModel.kt` für UI-Zustand und Screen-Logik
- `domain/model` für fachliche Datenmodelle
- `domain/usecase` für fachliche Logik wie Score-Berechnung
- `domain/repository` für Repository-Interfaces
- `data/mock` und `data/repository` für lokale Datenquellen und Implementierungen
- `core` für übergreifende technische Hilfsstrukturen

Die Score-Logik liegt bewusst nicht in der UI, sondern in `domain/usecase/CalculatePlaceScoreUseCase.kt`.

## Technologie

- Kotlin
- Android Studio
- Jetpack Compose
- Material 3
- Gradle Kotlin DSL

## Dokumentation

Weitere Projektdokumentation befindet sich im Ordner [`docs`](docs/):

- [`docs/architekturentscheidungen.md`](docs/architekturentscheidungen.md)
- [`docs/lasten-pflichtenheft.md`](docs/lasten-pflichtenheft.md)
- [`docs/meeting-notes-2026-07-12.md`](docs/meeting-notes-2026-07-12.md)
- [`docs/projektstruktur.md`](docs/projektstruktur.md)

## Ziel

place2be soll Menschen helfen, schöne öffentliche Orte zu entdecken, bessere Freizeitentscheidungen zu treffen und gemeinsam eine aktuelle Karte lebenswerter öffentlicher Orte aufzubauen.
