# Backlog / geplante GitHub-Issues

Die folgenden Aufgaben sind als vorbereitete GitHub-Issues formuliert. Falls Issues im Repository deaktiviert sind, können sie aus diesem Dokument übernommen werden.

---

## 1. Projektstruktur für place2be-MVP definieren

### Ziel
Eine klare, prüfungsfreundliche Projektstruktur für den place2be-MVP anlegen, damit UI, Datenmodell, Geschäftslogik und Datenquelle sauber getrennt sind.

### Hintergrund
Die App soll funktional werden, aber der Scope muss bis zur Präsentation am 27.07.26 kontrollierbar bleiben. Eine einfache Architektur mit klaren Paketen erleichtert Entwicklung, Erklärung im Vortrag und spätere Erweiterungen.

### Vorschlag für Paketstruktur
- `de.place2be.model` – Datenklassen wie `Place`, `Review`, `PlaceCategory`
- `de.place2be.data` – Mock-Daten und spätere Datenquellen
- `de.place2be.repository` – Zugriffsschicht für Orte und Bewertungen
- `de.place2be.ui` – Compose-Screens und UI-Komponenten
- `de.place2be.ui.theme` – bestehendes Theme

### Akzeptanzkriterien
- [ ] Eine nachvollziehbare Paketstruktur ist angelegt.
- [ ] `MainActivity.kt` bleibt schlank und delegiert UI-Logik an eigene Composables/Screens.
- [ ] UI, Datenmodell und Datenzugriff sind nicht alles in einer Datei vermischt.
- [ ] Die Struktur lässt später eine echte Backend-/Firebase-/REST-Anbindung zu, ohne die UI komplett umzubauen.
- [ ] Die Struktur kann in der Präsentation kurz und verständlich erklärt werden.

---

## 2. Datenmodell für Orte und Bewertungen erstellen

### Ziel
Zentrale Datenklassen für Orte, Kategorien und Bewertungen definieren.

### Akzeptanzkriterien
- [ ] `Place` enthält mindestens ID, Name, Kategorie, Beschreibung, Koordinaten/Ortshinweis und Score.
- [ ] `Review` enthält mindestens Place-ID, Bewertungskriterien und Zeitstempel.
- [ ] Bewertungskriterien sind fachlich nachvollziehbar, z. B. Atmosphäre, Sicherheit und Aufenthaltsqualität.
- [ ] Optional existiert ein `PlaceCategory`-Typ für Kategorien wie Park, Platz, Promenade oder Einkaufsmeile.
- [ ] Das Datenmodell passt zu MVP und Projektdokumentation.

---

## 3. Mock-Daten für Beispielorte anlegen

### Ziel
Demo-fähige Beispielorte bereitstellen, damit die App ohne Backend sinnvoll gezeigt werden kann.

### Akzeptanzkriterien
- [ ] Mindestens 5 öffentliche Beispielorte existieren.
- [ ] Jeder Ort hat Name, Kategorie, Beschreibung und Startbewertung oder initiale Reviews.
- [ ] Die Daten werden zentral verwaltet, nicht direkt in UI-Komponenten hart codiert.
- [ ] Die Demo wirkt ohne Backend plausibel.
- [ ] Die Mock-Daten lassen sich später durch eine echte Datenquelle ersetzen.

---

## 4. Startscreen mit Orteliste bauen

### Ziel
Nutzerinnen und Nutzer sehen beim Start empfohlene Orte.

### Akzeptanzkriterien
- [ ] Orte werden in einer Compose-Liste angezeigt.
- [ ] Jeder Listeneintrag zeigt Name, Kategorie und aktuellen Score.
- [ ] Die Liste ist mobilfreundlich und mit Material 3 umgesetzt.
- [ ] Sortierung nach Score ist möglich oder vorbereitet.
- [ ] Klick auf einen Ort führt zur Detailansicht oder bereitet diese Navigation vor.

---

## 5. Detailansicht für einen Ort bauen

### Ziel
Nutzerinnen und Nutzer können Informationen zu einem Ort ansehen.

### Akzeptanzkriterien
- [ ] Detailansicht zeigt Name, Kategorie, Beschreibung und Score.
- [ ] Aktuelle Bewertungsbasis oder Kurzstatus ist sichtbar.
- [ ] Button/Call-to-Action zum Bewerten ist vorhanden.
- [ ] Ansicht ist mit Material 3/Compose visuell sauber umgesetzt.
- [ ] Die Detailansicht erhält ihre Daten über eine saubere Übergabe oder Repository-Abfrage.

---

## 6. Bewertungsformular erstellen

### Ziel
Nutzerinnen und Nutzer können einen Ort über wenige kurze Fragen bewerten.

### Akzeptanzkriterien
- [ ] Bewertung umfasst Atmosphäre, Sicherheit und Aufenthaltsqualität oder vergleichbare Kriterien.
- [ ] Eingaben sind einfach und mobilfreundlich, z. B. Slider, Buttons oder Auswahlchips.
- [ ] Bewertung kann gespeichert oder an Repository-Logik übergeben werden.
- [ ] Nach Bewertung ist sichtbar, dass sich der Ort bzw. Score aktualisiert hat.
- [ ] Ungültige/leere Eingaben werden verhindert oder sinnvoll behandelt.

---

## 7. Ranking-Score inklusive Zeitverfall berechnen

### Ziel
Die Kernlogik der App umsetzen: Bewertungen beeinflussen den Score, verlieren aber mit der Zeit an Gewicht.

### Akzeptanzkriterien
- [ ] Neue Bewertungen verändern den Ort-Score nachvollziehbar.
- [ ] Alte Bewertungen werden schwächer gewichtet als neue.
- [ ] Die Berechnung ist in einer eigenen Funktion/Klasse gekapselt.
- [ ] Die Logik ist einfach genug, um sie in der Präsentation erklären zu können.
- [ ] Mindestens ein Beispiel/Testfall zeigt den Zeitverfall.

---

## 8. Lokale Datenhaltung für MVP festlegen und umsetzen

### Ziel
Den MVP lokal-first bauen, aber spätere Backend-Anbindung ermöglichen.

### Akzeptanzkriterien
- [ ] Entscheidung „lokal-first für MVP“ ist im Code/README/docs nachvollziehbar.
- [ ] Repository-Schicht kapselt den Datenzugriff.
- [ ] UI hängt nicht direkt an Mock-Daten.
- [ ] Spätere Firebase-/REST-/Room-Anbindung wäre ohne kompletten UI-Umbau möglich.
- [ ] Für die Demo können neue Bewertungen im App-Zustand sichtbar werden.

---

## 9. README und Docs aktuell halten

### Ziel
Projektidee, MVP, Architekturentscheidungen und Abgrenzungen dokumentieren.

### Akzeptanzkriterien
- [ ] README beschreibt `place2be` präzise.
- [ ] Zeitverfall der Bewertungen ist erwähnt.
- [ ] `docs/architekturentscheidungen.md` existiert.
- [ ] `docs/lasten-pflichtenheft.md` existiert.
- [ ] MVP und Nice-to-have sind sauber getrennt.

---

## 10. Präsentations- und Demo-Szenario vorbereiten

### Ziel
Am 27.07.26 eine stabile Live-Demo zeigen können.

### Akzeptanzkriterien
- [ ] Konkreter Demo-Ablauf ist dokumentiert.
- [ ] Demo zeigt Orteliste → Detailansicht → Bewertung → Score-Änderung.
- [ ] Repo-Struktur und Designentscheidungen können erklärt werden.
- [ ] Bekannte Einschränkungen sind ehrlich benannt.
- [ ] Fallback-Szenario existiert, falls die Live-Demo Probleme macht.
