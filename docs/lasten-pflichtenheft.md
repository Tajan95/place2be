# Lasten- und Pflichtenheft: place2be MVP

## 1. Projektüberblick

place2be ist eine Android-App, die Nutzerinnen und Nutzern hilft, öffentliche und niedrigschwellig nutzbare Orte zu entdecken. Im Mittelpunkt stehen Orte, an denen man sich aufhalten kann, ohne zwingend Geld ausgeben zu müssen, zum Beispiel Parks, öffentliche Plätze, Promenaden, Einkaufsmeilen oder andere urbane Treffpunkte.

Die App soll Community-Feedback nutzen, um die Aufenthaltsqualität solcher Orte dynamisch einzuschätzen. Bewertungen sollen zeitabhängig gewichtet werden, damit Orte nicht dauerhaft durch veraltete Eindrücke geprägt bleiben.

## 2. Zielgruppe

Die App richtet sich vor allem an Menschen, die spontan öffentliche Aufenthaltsorte suchen und wissen möchten, ob sich ein Besuch lohnt. Besonders relevant ist die Idee für junge Menschen oder Gruppen, die sich außerhalb der eigenen Wohnung treffen möchten, ohne kommerzielle Angebote nutzen zu müssen.

## 3. Problemstellung

Viele Orte im öffentlichen Raum sind schwer einschätzbar: Ein Ort kann heute angenehm, sicher und lebendig sein, aber in einigen Monaten unattraktiv wirken. Klassische statische Bewertungen altern schlecht und bilden Veränderungen nur unzureichend ab.

place2be soll dieses Problem lösen, indem Orte regelmäßig neu bewertet werden und aktuelle Rückmeldungen stärker zählen als alte Bewertungen.

## 4. Lastenheft: fachliche Anforderungen

### 4.1 Muss-Anforderungen für den MVP

- Nutzerinnen und Nutzer können eine Liste öffentlicher Orte sehen.
- Jeder Ort besitzt mindestens Name, Kategorie, Beschreibung und aktuellen Score.
- Nutzerinnen und Nutzer können einen Ort auswählen und Details ansehen.
- Nutzerinnen und Nutzer können einen Ort anhand weniger Kriterien bewerten.
- Neue Bewertungen beeinflussen den Score eines Ortes sichtbar.
- Ältere Bewertungen werden in der Score-Berechnung schwächer gewichtet als neue.
- Die App ist als Android-App mit Kotlin und Jetpack Compose umgesetzt.

### 4.2 Soll-Anforderungen

- Die App nutzt Material 3 für ein konsistentes UI.
- Die App trennt UI, Datenmodell, Datenzugriff und Bewertungslogik.
- Die Datenhaltung ist so gekapselt, dass später eine echte Datenquelle angebunden werden kann.
- Die Standortbestätigung wird im MVP zumindest konzeptionell oder vereinfacht dargestellt.

### 4.3 Kann-Anforderungen / Nice-to-have

- Punkte für abgegebene Bewertungen.
- Einfache Gamification-Elemente.
- Echte GPS-basierte Standortprüfung.
- Vorschlagen neuer Orte durch Nutzerinnen und Nutzer.
- Backend, Firebase oder REST-API.
- Kartenansicht.
- Nutzerkonten und Ranglisten.

## 5. Pflichtenheft: technische Umsetzung im MVP

### 5.1 Technologie

- Android Studio als Entwicklungsumgebung
- Kotlin als Programmiersprache
- Jetpack Compose für UI
- Material 3 für UI-Komponenten
- Gradle/Kotlin DSL als Build-System

### 5.2 Vorgeschlagene Architektur

Die App wird in einfache Schichten gegliedert:

- **UI-Schicht:** Compose-Screens wie Orteliste, Detailansicht und Bewertungsformular.
- **State-/Logik-Schicht:** Hält UI-Zustand und verarbeitet Nutzeraktionen.
- **Repository-Schicht:** Kapselt Zugriff auf Orte und Bewertungen.
- **Datenmodell:** Definiert zentrale Datenobjekte wie `Place`, `Review` und `PlaceCategory`.
- **Score-Logik:** Berechnet den dynamischen Ort-Score inklusive Zeitverfall.

### 5.3 Vorläufiges Datenmodell

#### Place

- `id`: eindeutige ID
- `name`: Name des Ortes
- `category`: Kategorie, z. B. Park, Platz, Promenade, Einkaufsmeile
- `description`: kurze Beschreibung
- `latitude` / `longitude` oder vereinfachte Ortsangabe
- `reviews`: zugehörige Bewertungen oder referenzierte Review-Liste
- `score`: berechneter aktueller Wert

#### Review

- `id`: eindeutige ID
- `placeId`: Zuordnung zum Ort
- `atmosphereRating`: Bewertung der Atmosphäre
- `safetyRating`: Bewertung der Sicherheit
- `qualityRating`: Bewertung der Aufenthaltsqualität
- `createdAt`: Zeitstempel der Bewertung

#### PlaceCategory

- Park
- Platz
- Promenade
- Einkaufsmeile
- Sonstiger öffentlicher Ort

### 5.4 Score-Berechnung

Der Score eines Ortes ergibt sich aus den Bewertungen der Nutzerinnen und Nutzer. Neue Bewertungen zählen stärker als alte Bewertungen. Dadurch entsteht ein dynamisches Ranking.

Vereinfachte Formelidee:

```text
gewicht = 1 / (1 + alter_in_tagen * decay_faktor)
gewichtete_bewertung = durchschnitt_der_kriterien * gewicht
score = summe(gewichtete_bewertungen) / summe(gewichte)
```

Diese Formel ist bewusst einfach, damit sie im Vortrag nachvollziehbar erklärt und im Code leicht getestet werden kann.

### 5.5 Standortbestätigung im MVP

Die echte GPS-basierte Standortprüfung wird zunächst nicht als Muss-Anforderung behandelt. Für den MVP reicht eine vereinfachte Bestätigung, zum Beispiel ein Button „Ich bin vor Ort“. Die Architektur soll aber so gestaltet sein, dass später eine echte Standortprüfung über Android Location APIs ergänzt werden kann.

### 5.6 Datenhaltung

Für den MVP wird eine lokale/prototypische Datenhaltung empfohlen. Möglich sind:

- feste Mock-Daten für Orte,
- In-Memory-Speicherung während der Demo,
- optional lokale Persistenz über Room/SQLite, falls der Scope es erlaubt.

Wichtig ist, dass die UI nicht direkt von hart codierten Daten abhängt, sondern über eine Repository-Schicht arbeitet.

## 6. Abgrenzung des MVP

Nicht Teil des MVP sind:

- echtes Login-System,
- zentrale Nutzerverwaltung,
- produktives Backend,
- globale Synchronisation,
- vollständiger Manipulationsschutz,
- echte Kartenintegration,
- vollständige GPS-Verifikation.

Diese Punkte können in der Fallstudie als Ausblick oder Erweiterungsmöglichkeiten beschrieben werden.

## 7. Demo-Szenario

Ein mögliches Demo-Szenario für die Präsentation:

1. App starten.
2. Liste öffentlicher Orte anzeigen.
3. Einen Ort auswählen.
4. Detailansicht mit aktuellem Score zeigen.
5. Bewertung abgeben.
6. Aktualisierten Score anzeigen.
7. Kurz erklären, dass alte Bewertungen über Zeitverfall weniger Gewicht erhalten.

## 8. Bewertung der Designentscheidung

Die Local-first-Entscheidung ist pragmatisch: Sie reduziert technische Risiken und ermöglicht eine stabile Demo. Gleichzeitig bleibt die Architektur offen für spätere reale Datenquellen. Damit passt der MVP gut zum Modulziel, eine Android-App nachvollziehbar zu konzipieren und funktional umzusetzen.
