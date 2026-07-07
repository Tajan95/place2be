# place2be

place2be ist eine Android-App, die Nutzerinnen und Nutzern Empfehlungen für öffentliche Orte gibt, an denen sie eine schöne Zeit verbringen können. Im Mittelpunkt stehen frei zugängliche Orte wie Parks, Plätze, Promenaden, Einkaufsmeilen oder andere urbane Treffpunkte, die sich besonders für kostenlose oder niedrigschwellige Freizeitaktivitäten eignen.

## Kurzbeschreibung

place2be hilft Menschen dabei, spontan schöne öffentliche Orte zu entdecken und einzuschätzen, ob sich ein Besuch aktuell lohnt. Nutzerinnen und Nutzer können Orte ansehen, bewerten und damit deren Ranking beeinflussen.

Ein zentrales Konzept ist, dass Bewertungen nicht dauerhaft gleich stark zählen: Ältere Bewertungen verlieren mit der Zeit an Gewicht. Dadurch bleibt das Ranking dynamisch und bildet besser ab, dass Orte sich verändern können.

## Projektkontext

Dieses Projekt entsteht im Rahmen des Moduls **Mobile Software-Entwicklung am Beispiel der Android-Plattform**. Ziel ist ein funktionsfähiger Android-MVP, der die wichtigsten Konzepte aus der Veranstaltung praktisch anwendet: mobile UX, agile Entwicklung, Android-Architektur, Daten- und Zustandsmanagement, Performance sowie grundlegende IT-Sicherheit.

## MVP

Im MVP konzentriert sich place2be auf:

- Anzeige einer Liste öffentlicher, primär kostenlos nutzbarer Orte
- Detailansicht eines ausgewählten Ortes
- kurze Bewertung über 2–3 Kriterien, z. B. Atmosphäre, Sicherheit und Aufenthaltsqualität
- dynamischer Score durch Community-Feedback
- Zeitverfall alter Bewertungen, damit aktuelle Eindrücke stärker zählen
- lokale/prototypische Datenhaltung mit sauberer Architektur für spätere Backend-Anbindung
- vereinfachte oder simulierte Standortbestätigung für die Demo

## Nice-to-have / spätere Erweiterungen

- echte GPS-basierte Standortprüfung
- Kartenansicht
- Nutzerkonten
- Punkte als Gamification-Anreiz für abgegebene Bewertungen
- Ranglisten oder Vergleich mit Kontakten
- neue Orte vorschlagen
- zentrale Datenbank / Backend / Firebase / REST-API

## Ranking-System

Das Ranking eines Ortes basiert auf dem Feedback der Community. Positive Bewertungen lassen einen Ort steigen, negative Rückmeldungen können ihn sinken lassen. Aspekte wie Sicherheit, Atmosphäre, Sauberkeit oder Aufenthaltsqualität fließen in die Bewertung ein.

Damit das Ranking aktuell bleibt, wird ein Zeitverfall berücksichtigt: Neuere Bewertungen zählen stärker als ältere Bewertungen. Dadurch können Orte, die sich verschlechtern, im Ranking fallen, während ehemals schlecht bewertete Orte durch neue positive Rückmeldungen wieder steigen können.

## Vorläufige Architekturentscheidung

Der MVP wird vorläufig **local-first** umgesetzt. Das bedeutet: Die App kann zunächst mit Mock-Daten, In-Memory-Daten oder später optional lokaler Persistenz arbeiten. Gleichzeitig soll die Architektur so getrennt werden, dass eine spätere reale Backend-Anbindung möglich bleibt.

Die geplante Struktur trennt:

- UI mit Jetpack Compose
- Datenmodell
- Repository/Datenzugriff
- Bewertungs- und Rankinglogik
- spätere Datenquellen wie Backend, Firebase oder REST-API

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

## Ziel

place2be soll Menschen helfen, schöne öffentliche Orte zu entdecken, bessere Freizeitentscheidungen zu treffen und gemeinsam eine aktuelle Karte lebenswerter öffentlicher Orte aufzubauen.
