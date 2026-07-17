# Lasten- und Pflichtenheft: place2be MVP

## 1. Projektüberblick

place2be ist eine Android-App zum Entdecken und Bewerten öffentlicher, niedrigschwellig nutzbarer Orte. Im Mittelpunkt stehen Treffpunkte wie Parks, Plätze und Promenaden, an denen Menschen Zeit verbringen können, ohne zwingend Geld auszugeben.

Community-Feedback soll nicht als dauerhaft statischer Durchschnitt verstanden werden. Aktuelle Bewertungen zählen stärker als ältere, damit sich Veränderungen eines Ortes im sichtbaren Score widerspiegeln.

## 2. Zielgruppe

Die App richtet sich an Menschen, die spontan einen öffentlichen Aufenthaltsort suchen und einschätzen möchten, ob sich ein Besuch aktuell lohnt. Besonders relevant ist die Idee für junge Menschen und Gruppen, die sich außerhalb der eigenen Wohnung treffen möchten, ohne kommerzielle Angebote nutzen zu müssen.

## 3. Problemstellung

Öffentliche Orte sind schwer langfristig einzuschätzen. Atmosphäre, Sicherheit, Zugang und Nutzung können sich abhängig von Zeit, Umfeld und Entwicklung verändern. Klassische Bewertungen altern schlecht und können einen längst überholten Eindruck konservieren.

place2be begegnet diesem Problem durch:

- getrennte Bewertungen für Vibes, Sicherheit und Erreichbarkeit,
- stärkere Gewichtung aktueller Eindrücke,
- ergänzende Textrezensionen,
- Reaktionen auf hilfreiche Rezensionen,
- einen Nutzer-Score aus Aktivität und Reputation,
- datenschutzbewusste öffentliche Profile.

## 4. Lastenheft: fachliche Anforderungen

### 4.1 Muss-Anforderungen für den MVP

- Beim ersten Start erscheint ein Onboarding.
- Nach abgeschlossenem Onboarding startet die App direkt auf einer Map-/Dashboard-Ansicht.
- Die Karte wird im MVP als stilisierte Mock-Map umgesetzt.
- Ohne ausgewählten Ort zeigt die Map Schnellzugriffe für Filter, beliebte und gespeicherte Orte.
- Nutzer können Orte über Marker oder Listen auswählen.
- Nach Auswahl erscheint eine Mini-Preview, die zur Detailansicht erweitert werden kann.
- Die Detailansicht zeigt Name, Kategorie, Beschreibung, Ortsangabe, Tags und aggregierte Kriterienwerte.
- Orte können persistent als Bookmark gespeichert und wieder entfernt werden.
- Ein frischer Demo-Nutzer startet ohne gespeicherte Orte.
- Nutzer können Vibes, Sicherheit und Erreichbarkeit jeweils von 1 bis 5 bewerten.
- Eine kurze Textrezension ist optional.
- Neue Bewertungen werden sofort in der Ortsansicht sichtbar.
- Derselbe Nutzer darf denselben Ort erst nach 24 Stunden erneut bewerten.
- Aktuelle Bewertungen zählen stärker als ältere Bewertungen.
- Die sichtbare Aktivitätszahl pro Ort betrachtet die vergangenen 365 Tage.
- Textrezensionen können nach **Rezent** und **Beliebt** sortiert werden.
- Lange Rezensionen können ein- und ausgeklappt werden.
- Likes und Dislikes sind accountgebunden, persistent und umschaltbar.
- Eigene Rezensionen können nicht selbst bewertet werden.
- Pro Ort werden höchstens 50 nichtleere Rezensionstexte gespeichert; numerische Daten verdrängter Texte bleiben erhalten.
- Eine Profilseite zeigt Pseudonym, neutrales Icon und dynamischen Nutzer-Score.
- Das eigene Profil zeigt eine private Bewertungshistorie.
- Öffentliche Profile zeigen nur aggregierte Kennzahlen und keine chronologische Ortshistorie.
- Autoren fremder Rezensionen sind anklickbar und führen zum öffentlichen Profil.
- Der vollständige Onboarding-Inhalt ist später im eigenen Profil als Hilfe erneut aufrufbar.
- Die Bewertung ist fachlich an einen Vor-Ort-Aufenthalt gebunden; im MVP wird dieser Zustand simuliert.
- Die App ist mit Kotlin und Jetpack Compose umgesetzt.

### 4.2 Soll-Anforderungen

- Material 3 sorgt für ein konsistentes Android-UI.
- UI, fachliche Modelle, Datenzugriff und Berechnungen sind getrennt.
- Die Struktur ist Feature- und MVVM-orientiert.
- Score- und Regelwerke sind unabhängig von Compose testbar.
- Datenzugriff erfolgt über Repository-Interfaces.
- Der Local-first-MVP funktioniert ohne Netzwerkzugriff.
- Bestehender Map-, Orts- und Review-Kontext bleibt beim Öffnen eines Profils oder der Hilfe erhalten.
- Datenschutzentscheidungen werden ausdrücklich dokumentiert.
- Der Ausgangszustand der Demo ist reproduzierbar.

### 4.3 Kann-Anforderungen und spätere Erweiterungen

- echte GPS- und Mindestaufenthaltsprüfung,
- echte Kartenintegration,
- produktive Registrierung und Kontoverwaltung,
- Profilbearbeitung und frei wählbare Profilbilder,
- Backend, Firebase oder REST-API,
- Mehrgeräte-Synchronisation,
- serverseitige Moderation und Manipulationserkennung,
- Vorschlagen neuer Orte,
- Ranglisten und soziale Kontaktfunktionen,
- zeitabhängige Events,
- empirische Feinjustierung der Nutzer-Score-Konstanten,
- Navigation Compose, native Lifecycle-ViewModels und Dependency Injection.

## 5. Pflichtenheft: technische Umsetzung

### 5.1 Technologie

- Android Studio
- Kotlin
- Jetpack Compose
- Material 3
- Gradle Kotlin DSL
- Gson für JSON-Verarbeitung
- JUnit für lokale Unit-Tests

### 5.2 Architektur

Die App ist in folgende Verantwortungsbereiche gegliedert:

- **Feature/UI:** Compose-Screens und UI-Zustände für Map, Bewertung, Profil und Onboarding.
- **Domain:** fachliche Modelle, Repository-Verträge und Use Cases.
- **Data:** JSON-Mock-Daten, lokale Arbeitskopien und Repository-Implementierungen.
- **Core:** featureübergreifende Regeln, insbesondere Standort- und Bewertungsberechtigung.
- **App:** Zusammensetzung der Repositories, ViewModel-artigen Klassen und Navigation des Demo-Flows.

Die MVP-Implementierung ist MVVM-orientiert, verwendet aber bewusst schlanke Kotlin-Klassen statt durchgängig native `androidx.lifecycle.ViewModel`-Basisklassen. Die Navigation wird über speicherbaren Compose-Zustand gesteuert. Eine spätere technische Härtung kann Navigation Compose, Lifecycle-ViewModels, `SavedStateHandle` und Dependency Injection ergänzen.

### 5.3 Datenmodell

#### Place

- `uuid`
- `name`
- `category`
- `description`
- `imageReference`
- `latitude` / `longitude`
- `locationHint`
- `attributes`
- `initialScore` als Fallback

#### Review

- `uuid`
- `placeUuid`
- `userUuid`
- `vibe`
- `safety`
- `accessibility`
- `timestampMillis`
- `text`
- `likes`
- `dislikes`

#### Bookmark

- `uuid`
- `userUuid`
- `placeUuid`
- `createdAtMillis`

#### ReviewReaction

- Nutzerbezug
- Review-Bezug
- Typ `LIKE` oder `DISLIKE`
- Zeitstempel

Pro Nutzer und Review existiert höchstens eine aktive Reaktion.

#### User

- `uuid`
- `displayName`
- vorbereiteter Seed-Score als Datenfeld

Der tatsächlich sichtbare Nutzer-Score wird dynamisch aus Reviews und Reaktionen berechnet.

#### UserScoreResult

- Gesamt-Score
- Aktivität
- Reputation
- Teilwerte für Bewertungen, Texte und Reaktionen

### 5.4 Orts-Score

Vibes, Sicherheit und Erreichbarkeit werden separat gewichtet:

```text
gewicht = 1 / (1 + alter_in_tagen × 0,05)
gewichteter_kriterienwert = summe(wert × gewicht) / summe(gewichte)
gesamtwert = mittelwert(vibes, sicherheit, erreichbarkeit)
```

Die Berechnung liegt in `CalculatePlaceScoreUseCase`. Alte Bewertungen werden nicht gelöscht, verlieren aber an Einfluss. Der rollierende Ein-Jahres-Zähler ist davon getrennt.

### 5.5 Nutzer-Score

Der Nutzer-Score ist umgesetzt und bildet Aktivität sowie Reputation getrennt ab.

Berücksichtigt werden unter anderem:

- Bewertung neuer Orte,
- abnehmender Erkundungsbonus,
- Begrenzung naher Ortscluster,
- ausreichend lange Textrezensionen,
- begrenzte Reaktionen auf fremde Reviews,
- positive Netto-Reaktionen auf eigene Texte,
- logarithmisches Reputationswachstum,
- Aktivitätsfaktor des bewerteten Ortes,
- Obergrenze pro Rezension.

Die verbindlichen Regeln stehen in `docs/nutzer-score-regeln.md`. Die Normalisierung ist nicht mehr offen; lediglich eine spätere empirische Feinjustierung anhand realer Nutzungsdaten bleibt Ausblick.

### 5.6 Rezensionen und Aufbewahrung

- Neue Reviews werden über das Repository gespeichert und anschließend erneut gelesen.
- **Rezent** sortiert absteigend nach Zeitstempel.
- **Beliebt** berücksichtigt Netto-Reaktionen und eine Altersstrafe.
- Pro Ort sind höchstens 50 Texte sichtbar und persistent gespeichert.
- Beim Verdrängen wird nur `text` entfernt.
- Kriterienwerte, Nutzer, Ort, Zeitstempel und Reaktionszähler bleiben erhalten.

### 5.7 Standortbestätigung

Die Fachlogik unterscheidet bestätigte und nicht bestätigte Standortzustände. Im Demo-MVP wird `SIMULATED_CONFIRMED` verwendet, damit der Bewertungsflow zuverlässig demonstriert werden kann.

Eine reale Version müsste zusätzlich behandeln:

- Laufzeitberechtigungen,
- Messungenauigkeit,
- Mindestaufenthaltsdauer,
- Datenschutz und Zweckbindung,
- Manipulationsschutz.

### 5.8 Local-first-Datenhaltung

Die Seed-Dateien unter `app/src/main/data/mockdata` enthalten Orte, Reviews, Nutzer und den initialen Bookmark-Zustand. `MockPlaceDataSource` kopiert diese Daten beim ersten Start in den internen App-Speicher und bearbeitet ausschließlich die Arbeitskopien.

Zusätzlich werden lokal gespeichert:

- Review-Reaktionen,
- Onboarding-Abschlussstatus,
- durch Nutzer erstellte Bewertungen und Bookmarks.

Die UI greift nicht direkt auf JSON-Dateien zu, sondern auf Repository-Interfaces.

### 5.9 Onboarding und Hilfe

Das Onboarding erklärt in vier Schritten:

1. Zweck der App,
2. Mock-Map und Bottom-Sheet,
3. Vibes, Sicherheit und Erreichbarkeit,
4. Bewertungsalterung und Standortbezug.

Der Abschlussstatus wird als boolescher Wert in app-internen `SharedPreferences` gespeichert. Derselbe Inhalt ist im eigenen Profil erneut als Hilfe erreichbar.

### 5.10 Profil, Datenschutz und Account-Abgrenzung

Der MVP verwendet einen festen pseudonymisierten Demo-Nutzer und behandelt ihn als bereits angemeldet.

Nicht umgesetzt werden:

- Registrierung,
- Login und Logout,
- Passwortverwaltung,
- Profil-Erstellung,
- Profilbearbeitung,
- Kontowiederherstellung und Kontolöschung.

Diese Abgrenzung ist bewusst: Der fachliche MVP demonstriert Score, private Historie, öffentliche Reputation und Datenschutz, nicht einen produktiven Account-Lifecycle.

Das eigene Profil zeigt vollständige private Daten. Öffentliche Profile zeigen nur aggregierte Werte. Diese Trennung darf durch eine spätere Backend-Anbindung nicht stillschweigend aufgehoben werden.

## 6. Abgrenzung des MVP

Nicht Teil des MVP sind:

- echte Live-Karte,
- produktive GPS-Verifikation,
- Registrierung und vollständige Kontoverwaltung,
- zentrales Backend,
- globale Synchronisation,
- serverseitige Moderation,
- vollständiger Schutz vor manipulierten Clients,
- produktive Datenschutz- und Einwilligungsverwaltung,
- Navigation Compose und native ViewModel-Lifecycle-Integration.

Diese Punkte werden als Ausblick dargestellt. Sie sind keine Voraussetzung dafür, die Kernidee und die umgesetzten Android-Konzepte zu demonstrieren.

## 7. Demo-Szenario

Der verbindliche Ablauf für die Präsentation am 27.07.2026 steht in:

[`docs/demo-runbook-2026-07-27.md`](demo-runbook-2026-07-27.md)

Der Ablauf umfasst Vorbereitung, Erststart, Map, Bookmark, Ortsdetail, Bewertung, Review-Reaktionen, öffentliche Profile, eigenes Profil, Architektur, Einschränkungen und ein Fallback-Szenario.

## 8. Bewertung der Designentscheidung

Die Local-first-Entscheidung reduziert technische Risiken und ermöglicht eine reproduzierbare Offline-Demo. Gleichzeitig zeigen Repository-Grenzen, Domain-Use-Cases und getrennte Profilmodi, wie eine spätere produktive Datenquelle integriert werden könnte.

Der MVP ist bewusst kein vollständiges Plattformprodukt. Er konzentriert sich auf die fachlich unterscheidbaren und für das Modul relevanten Kernmechanismen.