# Lasten- und Pflichtenheft: place2be MVP

## 1. Projektüberblick

place2be ist eine Android-App, die Nutzerinnen und Nutzern hilft, öffentliche und niedrigschwellig nutzbare Orte zu entdecken. Im Mittelpunkt stehen Orte, an denen man sich aufhalten kann, ohne zwingend Geld ausgeben zu müssen, zum Beispiel Parks, öffentliche Plätze, Promenaden, Einkaufsmeilen oder andere urbane Treffpunkte.

Die App soll Community-Feedback nutzen, um die Aufenthaltsqualität solcher Orte dynamisch einzuschätzen. Bewertungen sollen zeitabhängig gewichtet werden, damit Orte nicht dauerhaft durch veraltete Eindrücke geprägt bleiben.

## 2. Zielgruppe

Die App richtet sich vor allem an Menschen, die spontan öffentliche Aufenthaltsorte suchen und wissen möchten, ob sich ein Besuch lohnt. Besonders relevant ist die Idee für junge Menschen oder Gruppen, die sich außerhalb der eigenen Wohnung treffen möchten, ohne kommerzielle Angebote nutzen zu müssen.

## 3. Problemstellung

Viele Orte im öffentlichen Raum sind schwer einschätzbar: Ein Ort kann heute angenehm, sicher und lebendig sein, aber in einigen Monaten unattraktiv wirken. Klassische statische Bewertungen altern schlecht und bilden Veränderungen nur unzureichend ab.

place2be soll dieses Problem lösen, indem Orte regelmäßig neu bewertet werden und aktuelle Rückmeldungen stärker zählen als alte Bewertungen. Zusätzlich sollen Textrezensionen und Review-Reaktionen helfen, besonders hilfreiche Einschätzungen sichtbar zu machen.

## 4. Lastenheft: fachliche Anforderungen

### 4.1 Muss-Anforderungen für den MVP

- Nutzerinnen und Nutzer sehen nach dem Onboarding eine Map-/Dashboard-Ansicht.
- Die Map wird im MVP als Mock-Map umgesetzt, nicht als echte Live-Karte.
- Ohne ausgewählten Ort zeigt die Map Schnellzugriffe, z. B. Kategorien, beliebte Orte in der Nähe und gespeicherte Orte.
- Nutzerinnen und Nutzer können einen Ort auf der Map auswählen.
- Nach Auswahl eines Ortes erscheint eine Mini-Preview bzw. Schnellübersichtsleiste.
- Die Mini-Preview kann zur Detailansicht erweitert werden oder ist dafür vorbereitet.
- Jeder Ort besitzt mindestens Name, Kategorie, Beschreibung, Ortsangabe, Tags und aggregierte Kriterienwerte.
- Nutzerinnen und Nutzer können einen Ort anhand von Vibes, Sicherheit und Erreichbarkeit bewerten.
- Nutzerinnen und Nutzer können zusätzlich eine kurze Textrezension verfassen.
- Neue Bewertungen beeinflussen den Score eines Ortes sichtbar oder die Logik dafür ist vorbereitet.
- Ältere Bewertungen werden in der Score-Berechnung schwächer gewichtet als neue.
- Likes/Dislikes auf Reviews sind modelliert oder über Mock-Daten demonstrierbar.
- Nutzerinnen und Nutzer können Orte als gespeicherte Orte/Bookmarks/Favoriten markieren oder die Funktion ist vorbereitet.
- Eine Profilseite mit Pseudonym/Icon und Nutzer-Score ist MVP-Bestandteil, zunächst ggf. in begrenzter Ausbaustufe.
- Bewertung ist nur vor Ort möglich oder im MVP über eine vereinfachte/simulierte Standortbestätigung abgebildet.
- Die App ist als Android-App mit Kotlin und Jetpack Compose umgesetzt.

### 4.2 Soll-Anforderungen

- Die App nutzt Material 3 für ein konsistentes UI.
- Die App trennt UI, Datenmodell, Datenzugriff und Bewertungslogik.
- Die App folgt einer MVVM- und Feature-orientierten Struktur.
- Die Datenhaltung ist so gekapselt, dass später eine echte Datenquelle angebunden werden kann.
- Standortberechtigung und Datenschutz werden im MVP zumindest konzeptionell berücksichtigt.
- Die öffentliche Profilansicht wird datenschutzbewusst gestaltet oder als offene Entscheidung dokumentiert.

### 4.3 Kann-Anforderungen / Nice-to-have

- Echte GPS-basierte Standortprüfung.
- Echte Kartenintegration.
- Komplexere Filteransicht nach Tags.
- Ausgereifte Normalisierung des Nutzer-Scores gegen Score-Farming.
- Vorschlagen neuer Orte durch Nutzerinnen und Nutzer.
- Backend, Firebase oder REST-API.
- Nutzerkonten und Ranglisten.
- Zeitabhängige Events, z. B. Wochenmarkt nur zu bestimmten Zeiten.

## 5. Pflichtenheft: technische Umsetzung im MVP

### 5.1 Technologie

- Android Studio als Entwicklungsumgebung
- Kotlin als Programmiersprache
- Jetpack Compose für UI
- Material 3 für UI-Komponenten
- Gradle/Kotlin DSL als Build-System

### 5.2 Vorgeschlagene Architektur

Die App wird in einfache Schichten gegliedert:

- **Feature/UI-Schicht:** Compose-Screens wie MapScreen, Detailansicht, Bewertungsformular, Onboarding und Profil.
- **ViewModel-Schicht:** Hält UI-Zustand und verarbeitet Screen-Interaktionen.
- **Domain-Schicht:** Enthält fachliche Modelle und Use Cases, z. B. Score-Berechnung.
- **Repository-Schicht:** Kapselt Zugriff auf Orte, Bewertungen, Bookmarks und Nutzerinformationen.
- **Data-Schicht:** Enthält Mock-Daten und local-first Repository-Implementierungen.

### 5.3 Vorläufiges Datenmodell

#### Place

- `uuid`: eindeutige UUID
- `name`: Name des Ortes
- `category`: Kategorie, z. B. Park, Platz, Promenade, Einkaufsmeile
- `description`: kurze Beschreibung
- `latitude` / `longitude` oder vereinfachte Ortsangabe
- `locationHint`: lesbare Ortsangabe
- `attributes`: Tags/Ortseigenschaften
- `initialScore`: Startwert/Fallback für Mock-Daten

#### Review

- `uuid`: eindeutige UUID
- `placeUuid`: Zuordnung zum Ort über dessen UUID
- `userUuid`: Zuordnung zur bewertenden Person über deren UUID
- `vibe`: Bewertung für Vibes
- `safety`: Bewertung für Sicherheit
- `accessibility`: Bewertung für Erreichbarkeit
- `timestampMillis`: Zeitstempel der Bewertung
- `text`: optionale Textrezension
- `likes`: Anzahl positiver Reaktionen
- `dislikes`: Anzahl negativer Reaktionen

#### User

- `uuid`: eindeutige UUID
- `displayName`: Pseudonym
- `userScore`: Aktivitäts-/Reputationswert oder vorbereitete Struktur dafür

#### PlaceCategory

- Park
- Platz
- Promenade
- Einkaufsmeile
- Shopping-Center
- Stadtviertel
- Sonstiger öffentlicher Ort

#### PlaceAttribute

- öffentliche Toiletten
- Barrierefreiheit
- Schatten
- Sitzmöglichkeiten
- Essen/Trinken
- überdacht
- klimatisiert
- Sprache/Kultur
- lokale Events
- zeitabhängige Events

### 5.4 Score-Berechnung

Der Score eines Ortes ergibt sich aus den Bewertungen der Nutzerinnen und Nutzer. Neue Bewertungen zählen stärker als alte Bewertungen. Dadurch entsteht ein dynamisches Ranking.

Vereinfachte Formelidee:

```text
gewicht = 1 / (1 + alter_in_tagen * decay_faktor)
review_score = durchschnitt(vibes, sicherheit, erreichbarkeit)
gewichtete_bewertung = review_score * gewicht
score = summe(gewichtete_bewertungen) / summe(gewichte)
```

Diese Formel ist bewusst einfach, damit sie im Vortrag nachvollziehbar erklärt und im Code leicht getestet werden kann.

### 5.5 Nutzer-Score

Der Nutzer-Score soll Aktivität und Reputation abbilden.

Mögliche Punktequellen:

- Bewertung abgeben
- Textrezension verfassen
- Likes auf eigene Rezensionen erhalten
- optional minimale Punkte für das Liken/Disliken fremder Rezensionen

Offen bleibt, wie stark der Score normalisiert oder gedeckelt wird, damit beliebte Orte nicht zu leicht zum Score-Farming genutzt werden können.

### 5.6 Standortbestätigung im MVP

Die echte GPS-basierte Standortprüfung wird zunächst nicht als harte Muss-Anforderung behandelt. Für den MVP reicht eine vereinfachte oder simulierte Bestätigung. Der Bewertungsbutton soll aber sichtbar machen, dass Bewertungen nur vor Ort möglich sind: Er bleibt sichtbar, ist jedoch deaktiviert/ausgegraut, wenn keine Vor-Ort-Bestätigung vorliegt.

### 5.7 Datenhaltung

Für den MVP wird eine lokale/prototypische Datenhaltung empfohlen. Möglich sind:

- feste Mock-Daten für Orte,
- Mock-Daten für Reviews, Likes/Dislikes, Bookmarks und Nutzer-Score,
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
- vollständige GPS-Verifikation,
- finale Normalisierung des Nutzer-Scores,
- komplexe Filter- und Rankinglogik.

Diese Punkte können in der Fallstudie als Ausblick oder Erweiterungsmöglichkeiten beschrieben werden.

## 7. Demo-Szenario

Ein mögliches Demo-Szenario für die Präsentation:

1. App starten.
2. Onboarding kurz zeigen oder erklären.
3. Mock-Map mit Default-Shortcuts anzeigen.
4. Einen Ort auf der Map auswählen.
5. Mini-Preview/Schnellübersichtsleiste zeigen.
6. Detailansicht mit Bild, Bookmark, Kriterienwerten und Reviews öffnen.
7. Bewertung mit Vibes, Sicherheit, Erreichbarkeit und optionaler Textrezension abgeben.
8. Aktualisierten Score bzw. vorbereitete Score-Änderung anzeigen.
9. Bewertungsalterung erklären.
10. Profil/Nutzer-Score, Bookmarks und Review-Reaktionen zumindest anhand von Mock-Daten erklären.

## 8. Bewertung der Designentscheidung

Die Local-first-Entscheidung ist pragmatisch: Sie reduziert technische Risiken und ermöglicht eine stabile Demo. Gleichzeitig bleibt die Architektur offen für spätere reale Datenquellen. Damit passt der MVP gut zum Modulziel, eine Android-App nachvollziehbar zu konzipieren und funktional umzusetzen.
