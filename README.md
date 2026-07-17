# place2be

place2be ist eine Android-App zum Entdecken und Bewerten öffentlicher, niedrigschwellig nutzbarer Orte. Im Mittelpunkt stehen frei zugängliche Treffpunkte wie Parks, Plätze und Promenaden, an denen Menschen Zeit verbringen können, ohne zwingend Geld auszugeben.

## Projektkontext

Das Projekt entsteht im Modul **Mobile Software-Entwicklung am Beispiel der Android-Plattform**. Ziel ist ein nachvollziehbarer und stabil demonstrierbarer Android-MVP, der mobile UX, Jetpack Compose, Zustands- und Datenmanagement, eine geschichtete Architektur sowie grundlegende Datenschutz- und Sicherheitsentscheidungen praktisch verbindet.

## Produktidee

place2be hilft dabei, spontan einzuschätzen, ob sich ein öffentlicher Ort aktuell für einen Besuch eignet. Die Community bewertet Orte anhand von drei Kriterien:

1. **Vibes** – Atmosphäre, Stimmung und Aufenthaltsgefühl.
2. **Sicherheit** – subjektives Sicherheitsgefühl am Ort.
3. **Erreichbarkeit** – Zugang, Anbindung und praktische Erreichbarkeit.

Tags wie Sitzmöglichkeiten, Schatten oder öffentliche Toiletten beschreiben dagegen eher objektive Ortseigenschaften und bleiben von den drei subjektiven Bewertungskriterien getrennt.

Ein Kernmerkmal ist die **Bewertungsalterung**: Aktuelle Eindrücke zählen stärker als ältere. Dadurch kann ein Ort seinen sichtbaren Score im Laufe der Zeit sowohl verbessern als auch verschlechtern, statt dauerhaft von veralteten Bewertungen geprägt zu bleiben.

## Umgesetzter MVP

Der aktuelle Local-first-MVP umfasst:

- ein vierseitiges Onboarding beim ersten App-Start,
- eine später erneut aufrufbare Hilfe im eigenen Profil,
- eine stilisierte Mock-Map mit Markern und Default-Schnellzugriffen,
- Filter, beliebte Orte und gespeicherte Orte,
- eine Mini-Preview und eine erweiterbare Ortsdetailansicht im Bottom-Sheet,
- persistente Bookmarks mit leerem Ausgangszustand für einen frischen Demo-Nutzer,
- Bewertungen von 1 bis 5 für Vibes, Sicherheit und Erreichbarkeit,
- optionale Textrezensionen,
- eine 24-Stunden-Sperre pro Nutzer und Ort,
- simulierte Vor-Ort-Bestätigung für den Demo-Betrieb,
- zeitlich gewichtete Orts-Scores und einen rollierenden Ein-Jahres-Zähler,
- die Sortierungen **Rezent** und **Beliebt** für Textrezensionen,
- accountgebundene, persistente Likes und Dislikes,
- persistente Begrenzung auf 50 gespeicherte Rezensionstexte pro Ort,
- einen dynamischen Nutzer-Score aus Aktivität und Reputation,
- ein eigenes Profil mit privater Bewertungs- und Rezensionshistorie,
- datenschutzreduzierte öffentliche Profile, die über Rezensionen erreichbar sind.

## Profil und Datenschutz

Die öffentliche Sichtbarkeit ist bewusst begrenzt:

- Das **eigene Profil** zeigt Score-Aufteilung, aggregierte Kennzahlen, Hilfe, vorbereitete Einstellungen und die vollständige private Bewertungshistorie.
- Ein **fremdes Profil** zeigt nur Pseudonym, neutrales Icon, Gesamt-Score, Aktivität, Reputation und aggregierte Kennzahlen.
- Eine chronologische Liste besuchter oder bewerteter Orte wird öffentlich nicht zusammengeführt.

Einzelne Rezensionen bleiben am jeweiligen Ort sichtbar. Dadurch bleibt Community-Feedback nachvollziehbar, ohne aus mehreren Rezensionen ein öffentliches Bewegungsprofil zu erzeugen.

## Demo-Nutzer statt Account-Erstellung

Der MVP verwendet einen vorbereiteten, pseudonymisierten Demo-Nutzer, der innerhalb der App als bereits angemeldet behandelt wird. Registrierung, Login, Logout, Passwortverwaltung sowie das Erstellen oder Bearbeiten eines Profils sind bewusst nicht Teil des Projektumfangs.

Der fachliche Schwerpunkt liegt auf dem Entdecken, Bewerten und Speichern von Orten sowie auf Score-, Review- und Datenschutzlogik. Ein produktiver Account-Lifecycle würde zusätzlich Backend, Authentifizierung, Wiederherstellung, Einwilligungen, Kontolöschung und Synchronisation erfordern, ohne die Kernidee des MVP wesentlich besser zu demonstrieren.

## Orts- und Nutzer-Score

Der Orts-Score wird für Vibes, Sicherheit und Erreichbarkeit getrennt zeitgewichtet. Die verwendete Gewichtung lautet:

```text
Gewicht = 1 / (1 + Alter_in_Tagen × 0,05)
```

Der Gesamtwert ist der Mittelwert der drei gewichteten Kriterienwerte.

Der Nutzer-Score setzt sich aus **Aktivität** und **Reputation** zusammen. Er berücksichtigt unter anderem neue bewertete Orte, ausreichend lange Textrezensionen und begrenzte Community-Reaktionen. Abnehmende Erkundungsboni, Distanz- und Tagesgrenzen sowie logarithmisch wachsende Reputation begrenzen einfache Score-Farming-Strategien. Die vollständigen Regeln stehen in [`docs/nutzer-score-regeln.md`](docs/nutzer-score-regeln.md).

## Architektur

Der MVP ist **local-first**. Versionierte JSON-Seed-Dateien unter `app/src/main/data/mockdata` werden beim ersten Start in den internen App-Speicher kopiert und dort über Datenquellen und Repository-Interfaces bearbeitet. Die App benötigt für die Demo weder Netzwerkzugriff noch externe APIs.

Die Struktur ist Feature- und MVVM-orientiert:

- `feature/*/*Screen.kt` enthält Compose-Oberflächen,
- `feature/*/*ViewModel.kt` bereitet UI-Zustand und Interaktionen auf,
- `domain/model` enthält fachliche Modelle,
- `domain/usecase` enthält testbare Score- und Regelwerke,
- `domain/repository` definiert Datenzugriffsverträge,
- `data/mock` und `data/repository` implementieren die lokale Datenhaltung,
- `core` enthält featureübergreifende technische Regeln.

Die aktive Demo-Navigation wird pragmatisch über App-Zustand gesteuert. AndroidX Navigation Compose, native Lifecycle-`ViewModel`-Basisklassen und `SavedStateHandle` bleiben mögliche technische Weiterentwicklungen nach dem MVP.

## Bewusste MVP-Abgrenzung

Nicht umgesetzt sind insbesondere:

- produktive Registrierung und Kontoverwaltung,
- Backend, zentrale Datenbank und Mehrgeräte-Synchronisation,
- echte Kartenintegration,
- echte GPS- und Mindestaufenthaltsprüfung,
- serverseitiger Manipulations- und Moderationsschutz,
- Passwortwiederherstellung, Kontolöschung und Einwilligungsverwaltung,
- Ranglisten und soziale Kontaktfunktionen,
- Navigation Compose und vollständige native ViewModel-Lifecycle-Integration.

Diese Punkte sind Ausblick und keine fehlenden Voraussetzungen für die Demonstration der fachlichen Kernidee.

## Lokaler Test

```powershell
.\gradlew.bat testDebugUnitTest
```

Für einen reproduzierbaren frischen Demo-Zustand bei laufendem Emulator:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell pm clear de.place2be
```

Danach startet die App erneut mit Onboarding, leerer eigener Historie, Nutzer-Score `0` und ohne gespeicherte Orte.

## Dokumentation

- [`docs/architekturentscheidungen.md`](docs/architekturentscheidungen.md)
- [`docs/lasten-pflichtenheft.md`](docs/lasten-pflichtenheft.md)
- [`docs/projektstruktur.md`](docs/projektstruktur.md)
- [`docs/nutzer-score-regeln.md`](docs/nutzer-score-regeln.md)
- [`docs/onboarding-und-hilfe.md`](docs/onboarding-und-hilfe.md)
- [`docs/meeting-notes-2026-07-12.md`](docs/meeting-notes-2026-07-12.md)
- [`docs/mockup-notes-2026-07-12.md`](docs/mockup-notes-2026-07-12.md)
- [`docs/demo-runbook-2026-07-27.md`](docs/demo-runbook-2026-07-27.md)

## Ziel

place2be soll Menschen helfen, öffentliche Orte besser einzuschätzen und gemeinsam ein aktuelles, datenschutzbewusstes Bild lebenswerter Treffpunkte aufzubauen.