# Bereinigte Bild-/Mock-up-Notizen vom 12.07.2026

Diese Datei systematisiert die handschriftlichen App-Mockups aus dem In-Person-Meeting vom 12.07.2026. Sie ergänzt die schriftlichen Meeting-Notizen und dient als Grundlage für UI-Issues, MVP-Abgrenzung und spätere professionelle Mockups.

## 1. Überblick

Die drei Skizzen beschreiben vor allem drei App-Bereiche:

1. Profil-/Nutzerseite
2. Dashboard bzw. MapScreen als Hauptansicht
3. Detailansicht eines Ortes

Zusätzlich werden mehrere Querschnittskonzepte sichtbar:

- Nutzer-Score / Reputation / Gamification
- Bookmarks bzw. gespeicherte Orte
- Textrezensionen
- Likes/Dislikes bzw. Reaktionen auf Rezensionen
- Bewerten nur vor Ort
- Shortcut-Buttons auf der Map
- Mini-Preview bzw. Schnellübersichtsleiste für ausgewählte Orte
- Review-Sortierung nach beliebt, rezent und später weiteren Filtern

## 2. Profil-/Nutzerseite

### Geplante Inhalte

Die Profilseite zeigt grundsätzlich:

- Profilbild oder Nutzer-Icon
- Nutzer-Pseudonym
- Nutzer-Score / Reputation
- ggf. Einstellungen als separaten Button im Profil, nicht auf dem Dashboard
- Rezensionen des Nutzers als Verlauf bzw. Bewertungs-Historie

### Datenschutzfrage

Noch offen ist, ob alle Rezensionen eines Nutzers öffentlich im Profil sichtbar sein sollen. Eine zu detaillierte öffentliche Rezension-Historie könnte Rückschlüsse auf Identität, Bewegungsmuster oder häufig besuchte Orte ermöglichen.

Mögliche Varianten:

1. **Private Verlaufssicht:** Nur der Nutzer selbst sieht die vollständige eigene Bewertungs-Historie.
2. **Anonymisierte öffentliche Profilansicht:** Andere Nutzer sehen nur aggregierte Werte wie Nutzer-Score, Anzahl hilfreicher Rezensionen oder Badges.
3. **Teilweise öffentliche Reviews:** Einzelne Rezensionen sind am Ort sichtbar, aber das Profil zeigt nicht chronologisch alle Aktivitäten.

Diese Entscheidung ist für Datenschutz und Sicherheit relevant und sollte im Sicherheitskonzept erwähnt werden.

### Nutzer-Score

Der Nutzer-Score soll Aktivität belohnen und zugleich Reputation bzw. glaubwürdige/populäre Meinungen abbilden.

Punktequellen:

- Bewertung eines Ortes abgeben
- zusätzliche Textrezension schreiben
- Likes auf eigene Rezensionen erhalten
- ggf. minimale Punkte für das Liken/Disliken anderer Rezensionen, weil Beteiligung am demokratischen Bewertungsprozess belohnt wird

Dabei soll das Erhalten eines Likes auf eine eigene Rezension stärker gewichtet werden als das reine Liken fremder Rezensionen.

### Offene Score-Frage

Noch offen ist, ob der Nutzer-Score absolut oder relativ dargestellt wird.

- **Absoluter Score:** z. B. ein nach oben offener Integer-Wert.
- **Relativer Score:** z. B. 0 bis 5 Sterne oder ein Level.

Aktuelle Tendenz: absoluter Score. Gleichzeitig muss verhindert werden, dass Nutzer an sehr beliebten Orten durch viele Likes unverhältnismäßig schnell einen sehr hohen Score farmen. Mögliche Lösung: Normalisierung oder Deckelung abhängig von Ortsaktivität, Review-Anzahl oder Engagement-Verteilung.

## 3. Dashboard / MapScreen

<img width="1200" height="1600" alt="WhatsApp Image 2026-07-12 at 20 49 56 1" src="https://github.com/user-attachments/assets/a58e417c-6cb9-4273-8fc8-9c349d55fd7f" />

### Default-Zustand ohne ausgewählten Ort

Nach dem Onboarding startet die App auf einer Kartenansicht. Wenn kein Ort ausgewählt ist, erscheinen unten feste Shortcut-Buttons oder eine Bottom-Bar mit Schnellzugriffen.

Geplante Shortcuts:

- Orte nach Kategorie finden
- beliebte Orte in deiner Nähe
- gespeicherte Orte / Bookmarks / Favoriten

Ob diese Shortcuts als schwebende Buttons oder als Inhalt einer unteren Leiste umgesetzt werden, ist UI-seitig noch offen.

### Profilzugang

Oben rechts befindet sich ein Profil-Button. Das ursprünglich angedachte Zahnrad/Einstellungs-Icon auf dem Dashboard soll entfallen, um den Homescreen zu decluttern. Einstellungen sollen stattdessen über die Profilseite erreichbar sein.

### Auswahl eines Ortes auf der Karte

Ein Ort soll direkt auf der Map auswählbar sein. Nach Auswahl öffnet sich unten eine Mini-Preview bzw. Schnellübersichtsleiste.

Diese Schnellübersichtsleiste enthält voraussichtlich:

- 1–2 Mini-Vorschaubilder
- Name des Ortes
- kurze Ortsinformation
- drei Mini-Bewertungszusammenfassungen für Vibes, Sicherheit und Erreichbarkeit
- Button „Bewerten“

Der Bewertungsbutton ist immer sichtbar, aber deaktiviert/ausgegraut, wenn der Nutzer nicht vor Ort ist.

Mögliche Hinweisformulierung:

- „Bewertungen sind nur vor Ort möglich.“
- freundlicher: „Geh hin und sag uns, was du denkst!“

### Erweiterbare Detailansicht über der Karte

Die Schnellübersichtsleiste soll durch eine Swipe-Bewegung nach oben erweitert werden. Dadurch öffnet sich die Detailansicht über der Karte, ohne die Karte vollständig zu verdecken. Ziel ist ein Gefühl von Kontinuität zwischen Map, Mini-Preview und Detailansicht.

Beim Erweitern:

- Mini-Bilder werden größer
- Detailinformationen werden sichtbar
- darunter erscheinen schriftliche Rezensionen

Die Rezensionen können entweder als Endlos-Scroll oder zunächst als Top-10-Vorschau angezeigt werden.

### Tooltips / Marker-Zusammenfassungen

Wenn der Nutzer Shortcuts wie „beliebte Orte in deiner Nähe“ oder „Orte nach Kategorien suchen“ nutzt, können passende Orte auf der Karte mit Tooltips hervorgehoben werden. Diese Tooltips können kleine Bewertungszusammenfassungen zeigen, z. B. die drei Kriterienwerte.

## 4. Detailansicht eines Ortes

### Geplante Elemente

Die Detailansicht enthält:

- Zurück-Pfeil
- Bookmark-/Favorit-Icon
- großes Bild des Ortes
- Adresse bzw. Ortsangabe
- Titel / Name des Ortes
- drei aggregierte Kriterienwerte
- Bewertungsbutton
- Review-/Rezensionsbereich

Der handschriftliche Hinweis „Nutzer-Score“ auf der Detail-Skizze war nur ein konzeptioneller Hinweis und kein zwingendes Detailseiten-UI-Element.

### Kriterienwerte

Die drei Kriterien werden einzeln angezeigt und pro Ort aggregiert:

- Vibes
- Sicherheit
- Erreichbarkeit

Denkbar sind passende Icons, z. B. Schloss für Sicherheit, Zug/Rad für Erreichbarkeit und ein App-/Stimmungsicon für Vibes.

Ein zusätzlicher aggregierter Gesamtwert kann später ergänzt werden, ist aber nur bedingt aussagekräftig und deshalb nicht zwingend zentral.

### Bewertungsbutton

Der Bewertungsbutton bleibt immer sichtbar, ist aber nur aktiv, wenn der Nutzer vor Ort ist bzw. die Vor-Ort-Bestätigung erfüllt ist. Andernfalls ist er ausgegraut und zeigt einen erklärenden Hinweis.

### Textrezensionen

Textbewertungen sind MVP-relevant. Neben numerischen Kriterien sollen Nutzer einen kurzen Text verfassen können.

Textrezensionen können zunächst eingeklappt angezeigt werden, z. B. mit 2–3 Zeilen Vorschau. Längere Rezensionen können per Pfeil/Dreieck aufgeklappt werden.

### Review-Sortierung und Reaktionen

Im Review-Bereich sollen Nutzer zwischen mindestens zwei Sortierungen wechseln können:

- beliebt / geliked
- rezent

Likes/Dislikes auf Reviews sind MVP-relevant, können bei Zeitmangel aber zunächst mit Mock-Daten simuliert werden.

Weitere Filter, z. B. nach Tags, sind zunächst eher Nice-to-have.

## 5. Bookmarks / gespeicherte Orte

Bookmark, gespeicherte Orte und Favoriten werden synonym verwendet. Nutzer sollen Orte speichern und später über den Dashboard-Shortcut „gespeicherte Orte“ wiederfinden können.

## 6. MVP-Einordnung

### MVP-relevant

- Profilseite als MVP-Bestandteil, zunächst mit begrenzter Ausbaustufe
- Nutzer-Score als Aktivitäts- und Reputationssignal
- MapScreen im Default-Zustand mit Shortcut-Buttons
- auswählbare Orte auf der Mock-Map
- Mini-Preview / Schnellübersichtsleiste am unteren Rand
- erweiterbare Detailansicht über der Karte
- Bookmark-/Favoritenfunktion
- aggregierte Kriterienwerte pro Ort
- Bewertung nur vor Ort bzw. deaktivierter Bewertungsbutton bei fehlender Standortbestätigung
- Textrezensionen
- Likes/Dislikes auf Reviews, mindestens über Mock-Daten
- Sortierung der Reviews nach beliebt und rezent

### Nice-to-have / spätere Erweiterung

- komplexe weitere Review-Filter
- öffentlich sichtbare vollständige Nutzerhistorie, falls Datenschutzentscheidung positiv ausfällt
- Ranking/Level-System mit ausgefeilter Normalisierung
- echte Kartenintegration
- echte GPS-Distanzprüfung
- echtes Backend / echte Nutzerkonten

## 7. Offene Entscheidungen

- Wie öffentlich ist die Bewertungs-Historie eines Profils?
- Wie wird der Nutzer-Score gegen Farming/Verzerrung normalisiert?
- Wird der Nutzer-Score absolut oder relativ dargestellt?
- Werden Detailseiten-Reviews als Endlos-Scroll oder Top-10-Vorschau gezeigt?
- Werden Shortcut-Buttons auf dem Dashboard frei schwebend oder in einer Bottom-Bar umgesetzt?
- Welche Icons repräsentieren Vibes, Sicherheit und Erreichbarkeit?

## 8. Spätere Mockup-Erstellung

Diese handschriftlichen Skizzen sollten später als professionelle Mock-up-Bilder erzeugt werden, sobald die UI-Struktur finaler abgestimmt ist.
