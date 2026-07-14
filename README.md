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
- Default-Map mit Schnellzugriffen wie Kategorien, beliebte Orte in der Nähe und gespeicherte Orte
- auswählbare Orte auf der Map mit Mini-Preview bzw. Schnellübersichtsleiste
- erweiterbare Detailansicht über der Karte, damit die App-Navigation räumlich zusammenhängend wirkt
- Detailansicht eines ausgewählten Ortes mit Bild, Adresse, Bookmark und aggregierten Kriterienwerten
- kurze Bewertung über die drei Kriterien **Vibes**, **Sicherheit** und **Erreichbarkeit**
- optionale bzw. ergänzende Textrezensionen als MVP-Bestandteil
- dynamischer Score durch Community-Feedback
- stärkere Gewichtung aktueller Bewertungen gegenüber älteren Bewertungen
- Likes/Dislikes auf Reviews, mindestens zunächst über Mock-Daten
- Profilseite mit Nutzer-Score als Aktivitäts- und Reputationssignal
- Tags/Ortseigenschaften wie Sitzmöglichkeiten, Schatten, Barrierefreiheit oder öffentliche Toiletten
- lokale/prototypische Datenhaltung mit sauberer Architektur für spätere Backend-Anbindung
- Standortberechtigungen bzw. Standortbestätigung werden fachlich berücksichtigt; Bewerten ist nur vor Ort möglich oder im MVP entsprechend simuliert

## Nice-to-have / spätere Erweiterungen

- echte GPS-basierte Standortprüfung
- echte Kartenintegration
- komplexere Filteransicht nach Tags
- Einstellungen im Nutzerprofil
- ausgefeilte Normalisierung des Nutzer-Scores gegen Score-Farming
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

## Ranking- und Reputationssystem

Das Ranking eines Ortes basiert auf dem Feedback der Community. Positive Bewertungen lassen einen Ort steigen, negative Rückmeldungen können ihn sinken lassen. Aspekte wie Vibes, Sicherheit und Erreichbarkeit fließen in die Bewertung ein.

Damit das Ranking aktuell bleibt, wird das Bewertungsalter berücksichtigt: Neuere Bewertungen zählen stärker als ältere Bewertungen. Dadurch können Orte, die sich verschlechtern, im Ranking fallen, während ehemals schlecht bewertete Orte durch neue positive Rückmeldungen wieder steigen können.

Zusätzlich soll ein Nutzer-Score Aktivität und Reputation abbilden. Punkte können z. B. durch Bewertungen, Textrezensionen und Likes auf eigene Rezensionen entstehen. Die genaue Normalisierung ist noch offen, damit stark frequentierte Orte nicht zu einfach für Score-Farming genutzt werden können.

## Datenschutz-Hinweis zum Profil

Die Profilseite ist MVP-relevant. Offen ist noch, wie viel der eigenen Bewertungs-Historie öffentlich sichtbar sein soll. Eine öffentliche vollständige Historie kann Rückschlüsse auf Identität oder Bewegungsmuster erlauben. Deshalb wird geprüft, ob andere Nutzer nur eine anonymisierte Profilansicht mit aggregierten Werten sehen sollen.

## Architektur

Der MVP wird **local-first** mit JSON-Mock-Daten umgesetzt. Versionierte Seed-Dateien unter `app/src/main/data/mockdata` werden beim ersten Start in den internen App-Speicher kopiert und dort über eine zentrale CRUD-Datenquelle bearbeitet. Gleichzeitig bleibt die Architektur so getrennt, dass später eine reale Backend-Anbindung möglich ist.

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
- [`docs/mockup-notes-2026-07-12.md`](docs/mockup-notes-2026-07-12.md)
- [`docs/projektstruktur.md`](docs/projektstruktur.md)

## Ziel

place2be soll Menschen helfen, schöne öffentliche Orte zu entdecken, bessere Freizeitentscheidungen zu treffen und gemeinsam eine aktuelle Karte lebenswerter öffentlicher Orte aufzubauen.
