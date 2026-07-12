# Bereinigte Meeting-Notizen vom 12.07.2026

Diese Datei systematisiert die schriftlichen Notizen aus dem In-Person-Meeting vom 12.07.2026 zwischen Tajan, Morris und Artem. Sie dient als Arbeitsgrundlage für Architektur, MVP-Scope, Issues und spätere Mockups.

## 1. Neue fachliche Entscheidungen

### Mock-Map statt echter Live-Karte

Für den MVP wird keine echte Live-Kartenintegration umgesetzt. Stattdessen verwendet die App eine einheitliche, kontrollierte Mock-Map bzw. Kartenansicht. Dadurch kann die App visuell mit einer Karte starten, ohne dass Google Maps, OpenStreetMap, API-Keys, Abrechnung, echte Geodaten oder komplexe Karten-SDKs notwendig werden.

**Konsequenz:** Die App startet nach dem Onboarding auf einem `MapScreen`, dieser kann im MVP aber auf vorbereiteten Daten und einer vereinfachten Darstellung basieren.

### Bewertungskriterien

Die Bewertung eines Ortes soll über drei Kriterien erfolgen:

1. **Vibes** – sichtbarer Begriff in der App; meint Atmosphäre, Stimmung und Unterhaltungspotential.
2. **Sicherheit** – subjektive Einschätzung, ob der Ort aktuell sicher wirkt.
3. **Erreichbarkeit** – wie gut der Ort erreichbar bzw. zugänglich ist.

Der Begriff „Vibes“ wird bewusst als nutzerfreundlicher App-Begriff verwendet. In Onboarding, Dokumentation oder Präsentation kann er als Atmosphäre/Unterhaltungspotential erklärt werden.

### Tags / Filteroptionen / Ortseigenschaften

Zusätzlich zu den Bewertungskriterien soll es Eigenschaften geben, nach denen Orte beschrieben und später gefiltert werden können. Diese Eigenschaften sind nicht dasselbe wie Bewertungen.

Beispiele:

- öffentliche Toiletten
- Barrierefreiheit
- Schatten
- Sitzmöglichkeiten
- Essen/Trinken
- überdacht
- klimatisiert
- Sprache/Kultur
- ortsgebundene Events
- zeitabhängige Ereignisse, z. B. Wochenmarkt

**Konsequenz:** Im Datenmodell sollte es einen eigenen Typ für Ortseigenschaften geben, z. B. `PlaceAttribute`.

## 2. App-Ablauf

### Erstes Öffnen

Beim ersten Öffnen der App erscheint ein Onboarding bzw. Tutorial. Dieses erklärt:

- wofür place2be gedacht ist,
- wie die Kartenansicht zu verstehen ist,
- wie Orte bewertet werden,
- was „Vibes“, Sicherheit und Erreichbarkeit bedeuten,
- dass aktuelle Bewertungen stärker zählen als alte Bewertungen,
- warum Standortberechtigungen fachlich relevant sind.

Die Hilfsfunktion bzw. Hinweise sollen später erneut aufrufbar sein.

### Normaler App-Start

Nach abgeschlossenem Onboarding startet die App auf der Map-Ansicht. Wenn das App-Fenster verlassen, die App aber nicht vollständig geschlossen wird, soll die App beim erneuten Öffnen auf der zuletzt offenen Seite weiterlaufen.

### Navigation

Der Kernablauf für die Demo lautet:

1. App öffnen.
2. Onboarding kurz zeigen oder überspringen.
3. MapScreen mit markierten Orten anzeigen.
4. Ort auswählen.
5. Detailansicht öffnen.
6. Ort über Vibes, Sicherheit und Erreichbarkeit bewerten.
7. Aktualisierten Score anzeigen.
8. Bewertungsalterung bzw. Zeitgewichtung erklären.

## 3. Architekturentscheidung

Die App soll MVVM-orientiert und feature-basiert aufgebaut werden. Jeder größere Screen wird als Feature betrachtet. Pro Feature gibt es mindestens:

- eine `Screen`-Datei für die Compose-UI,
- eine `ViewModel`-Datei für UI-Zustand und Screen-Logik.

Fachliche Kernlogik, insbesondere die Score-Berechnung, gehört nicht in die UI und auch nicht direkt in einen Screen. Sie soll in der Domain-Schicht liegen, z. B. als `CalculatePlaceScoreUseCase`.

## 4. MVP-Abgrenzung

### MVP-relevant

- Onboarding beim ersten Start
- MapScreen als Mock-Map
- Ort auswählen
- Detailansicht
- Bewertung mit Vibes, Sicherheit und Erreichbarkeit
- Score-Berechnung mit stärkerer Gewichtung aktueller Bewertungen
- Tags/Ortseigenschaften im Datenmodell und möglichst in der UI sichtbar
- lokale/prototypische Datenhaltung
- Standortberechtigung fachlich berücksichtigen; echte Prüfung kann vereinfacht/simuliert sein

### Nice-to-have

- echte Kartenintegration
- echte GPS-Distanzprüfung
- Filteransicht nach Tags
- Profilseite
- Einstellungen
- Gamification/Punkte
- neue Orte vorschlagen
- Backend/Firebase/REST-API
- zeitlogische Events wie Wochenmarkt nur zu bestimmten Zeiten

## 5. Offene Punkte

- Ob zusätzlich zur Mock-Map eine klassische Listenansicht gebraucht wird, ist noch offen.
- Die Liste der Tags/Attribute kann später erweitert werden.
- Standortberechtigung soll im MVP sichtbar berücksichtigt werden; die technische Tiefe ist noch offen.
