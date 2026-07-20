# Meeting- und Blindtest-Notizen vom 20.07.2026

## 1. Kontext

Am 20.07.2026 haben Tajan, Artem und Morris den aktuellen place2be-MVP gemeinsam geprüft und den Ablauf für die Präsentation am 27.07.2026 besprochen.

Morris testete die App dabei bewusst mit möglichst wenig Vorwissen über die seit dem früheren Dashboard-Stand hinzugekommenen Funktionen. Der Blindtest sollte vor allem zeigen:

- welche Interaktionen ohne zusätzliche Erklärung verständlich sind,
- welche Begriffe oder Symbole Missverständnisse erzeugen,
- welche Fehler vor der Präsentation reproduziert werden müssen,
- welche Teile der Architektur im Vortrag erklärt werden sollen,
- wie sich GitHub, Prototypen, Android Studio und Live-Demo zu einem gemeinsamen roten Faden verbinden lassen.

## 2. Beobachtungen aus dem Blindtest

### 2.1 Inaktive Menü-Ikone

Im Kopfbereich der Karte wird links eine Hamburger-/Menü-Ikone angezeigt, obwohl sie im MVP keine Aktion besitzt. Das Symbol erzeugt dadurch eine falsche Erwartung.

**Entscheidung:** Die funktionslose Ikone soll vor der Präsentation entfernt werden. Der dadurch frei werdende Platz kann später neu bewertet werden; ein rotierender Hinweis- oder Werbebereich gehört nicht mehr zum notwendigen Prüfungsumfang.

Verfolgt in Issue #36.

### 2.2 Begriff „Rezent“

Die Sortierbezeichnung `Rezent` war im Blindtest nicht unmittelbar verständlich.

**Entscheidung:** Die sichtbare Beschriftung soll in `Zuletzt` geändert werden. Der interne Enum-Wert `RECENT` und die bestehende Sortierung nach absteigendem Zeitstempel können unverändert bleiben.

Verfolgt in Issue #36.

### 2.3 Filter und Marker

Beim Testen entstand der Eindruck, dass die Filterfunktion in einzelnen Zuständen nicht eindeutig oder fehlerhaft arbeitet. Zusätzlich können sich Marker beziehungsweise deren breite Ortsübersichten bei nahen Mock-Koordinaten überlagern.

Die Beobachtung ist noch keine hinreichend präzise Fehlerbeschreibung. Vor einer Änderung müssen daher exakte Reproduktionsschritte festgehalten werden.

Zu prüfen sind insbesondere:

- einzelne und mehrere Kategorien,
- Kombination mehrerer Ortseigenschaften,
- Zurücksetzen der Filter,
- Übereinstimmung von Trefferzähler, Karte und Listen,
- ausgewählte Orte während einer Filteränderung,
- Kollisionen zwischen Markern und Infokarten.

Verfolgt in Issue #37.

### 2.4 Englische Kategorienamen

Die Domain-Enums und JSON-Werte verwenden bewusst englische technische Bezeichnungen wie `PARK`, `SQUARE` oder `SHOPPING_STREET`. Die sichtbare Oberfläche soll diese Werte über deutsche Labels darstellen.

**Bewertung:** Englische Variablen- und Enum-Namen sind kein Bug. Ein Bug liegt nur vor, wenn ein technischer Rohwert in der Benutzeroberfläche sichtbar wird. Diese Prüfung ist Teil von Issue #37.

## 3. Ideen für den späteren Backlog

### 3.1 Weitere Mock-Orte

Bis zu fünf zusätzliche Orte könnten mehr Kategorien und Ortseigenschaften demonstrieren. Diese Erweiterung soll erst nach Stabilisierung von Filter und Markerpositionierung erfolgen, damit zusätzliche Daten bestehende Überlagerungsprobleme nicht verschärfen.

Verfolgt in Issue #38.

### 3.2 Nutzer-Titel und Achievements

Neben dem numerischen Nutzer-Score wurden spielerische Titel beziehungsweise Achievements diskutiert, beispielsweise für unterschiedliche besuchte Kategorien, hilfreiche Rezensionen oder regelmäßige Aktivität.

Die Titel sollen später:

- verschiedene Nutzungsweisen belohnen,
- nicht direkt weitere Score-Punkte erzeugen,
- Score-Farming nicht verstärken,
- datenschutzbewusst zwischen eigener und öffentlicher Ansicht unterscheiden.

Verfolgt in Issue #39.

### 3.3 Rotierender Header-Bereich

Als spätere Idee wurde ein rotierender Bereich zwischen App-Identität und Profilzugang diskutiert. Für eine MVP-nahe Variante wären lokale Community- oder Feature-Hinweise geeigneter als echte Werbung.

Echte Werbung würde zusätzliche Entscheidungen zu Kennzeichnung, Tracking, Datenschutz, Barrierefreiheit, Rotation und Moderation erfordern und gehört nicht mehr zum Prüfungsumfang.

Verfolgt in Issue #40.

## 4. Vereinbarter Präsentationsbogen

Der Vortrag soll nicht unmittelbar mit der laufenden App beginnen, sondern zunächst zeigen, wie das Team gearbeitet und Entscheidungen nachvollziehbar festgehalten hat.

### Phase 1: GitHub und Entwicklungsprozess

- Repository kurz zeigen,
- Issues und Bug-Tickets als agile Arbeitsgrundlage erläutern,
- ein oder zwei aussagekräftige abgeschlossene Issues öffnen,
- gezeichnete Prototypen beziehungsweise Mockup-Notizen zeigen,
- Übergang vom Entwurf zur implementierten Oberfläche erklären.

### Phase 2: Android Studio und Produktidee

- in die IDE wechseln,
- Projektstruktur nur kurz einordnen,
- anhand des README Problem, Zielgruppe und MVP-Idee erklären,
- anschließend die App starten.

### Phase 3: Onboarding und fachliche Kernlogik

- Onboarding-Seiten demonstrieren,
- Vibes, Sicherheit und Erreichbarkeit erklären,
- Bewertungsalterung erläutern,
- Maßnahmen gegen einfache Score-Maximierung einordnen,
- danach in die eigentliche App-Demo übergehen.

### Phase 4: Kompakte Live-Demo

Der Kernflow soll bewusst schlank bleiben:

```text
Mock-Map
  → Ort auswählen
  → Bookmark setzen
  → Detail und Kriterien zeigen
  → Bewertung mit Text speichern
  → Reviews und Reaktion zeigen
  → eigenes und öffentliches Profil vergleichen
```

### Phase 5: Technische Architektur

Nach der fachlichen Demonstration folgt Morris’ Architekturteil.

Zu behandeln sind:

- Android- und Kotlin-Konventionen,
- Jetpack Compose,
- MVVM-orientierte Struktur,
- Repository Pattern,
- Trennung von Domain und Data,
- stateful und stateless Composables,
- bekannte pragmatische MVP-Kompromisse.

### Phase 6: Einschränkungen und Ausblick

Zum Abschluss werden bewusst nicht umgesetzte Punkte transparent benannt:

- Mock-Map statt Live-Karte,
- simulierte Standortbestätigung,
- fester Demo-Nutzer statt produktivem Account-Lifecycle,
- lokale Daten statt Backend und Synchronisation,
- keine produktive Moderation,
- Navigation Compose und native Lifecycle-ViewModels als technischer Ausbaupfad.

## 5. Vorläufige Rollen und Übergaben

Die endgültige Rollenverteilung kann in der Generalprobe angepasst werden.

### Tajan

- Produktidee und Zielgruppe,
- README und Übergang in die App,
- Onboarding,
- Bewertungsalterung,
- Schutz vor einfacher Score-Maximierung,
- Bedienung oder Moderation des fachlichen Kernflows.

### Morris

- Android-Konventionen,
- Jetpack Compose,
- MVVM-orientierte Architektur,
- Repository Pattern,
- Domain-/Data-Trennung,
- stateful und stateless Composables,
- technische MVP-Kompromisse.

### Artem

Als sinnvoller Block bieten sich an:

- GitHub, Issues und Entwicklungsprozess,
- Prototypen und Übergang vom Mockup zur App,
- bekannte Einschränkungen und Ausblick,
- alternativ Unterstützung bei Live-Bedienung und Fallback-Material.

## 6. Empfohlener Übergabesatz an Morris

> Damit haben wir gezeigt, was place2be fachlich leistet und wie wir veraltete oder leicht manipulierbare Bewertungen begrenzen. Morris erklärt jetzt, wie wir diese Funktionen mit Jetpack Compose, einer MVVM-orientierten Struktur und Repository-Grenzen technisch aufgebaut haben.

## 7. Architektur-Kernaussagen für Morris

### Android- und Compose-Konventionen

- UI wird deklarativ mit Jetpack Compose statt XML beschrieben.
- Zustand und Aktionen werden möglichst über Parameter und Callbacks weitergegeben.
- Fachliche Regeln liegen nicht direkt in Composables.
- Material 3 liefert konsistente Android-nahe Komponenten.

### MVVM-orientierte Struktur

- Screens stellen den UI-Zustand dar.
- ViewModel-artige Klassen bereiten Daten und Interaktionen auf.
- Use Cases enthalten testbare fachliche Regeln.
- Der MVP verwendet aus Zeitgründen nicht durchgängig native Lifecycle-`ViewModel`-Klassen.

### Repository Pattern

- UI und ViewModel-artige Klassen kennen keine JSON-Dateien.
- Repository-Interfaces bilden die fachlichen Datenzugriffsverträge.
- Local-first-Implementierungen können später durch Remote-Repositories ergänzt werden.

### Stateful und stateless Composables

- Stateless Composables erhalten Daten und Callbacks von außen und besitzen möglichst keine eigene fachliche Zustandsquelle.
- Stateful Composables koordinieren Zustand, beispielsweise mit `remember` oder `rememberSaveable`.
- `Place2BeApp` bildet die stateful Composition Root für Navigation, Auswahlzustände und Repository-Zusammensetzung.
- In integrierten Screens verbleibt bewusst lokaler UI-Zustand, etwa für Slider, Textfeld, Sortierung und Scrollposition.

## 8. Abgeleitete Issues

### Vor der Präsentation

- #35 Präsentationsablauf und Dokumentation finalisieren
- #36 Inaktive Menü-Ikone entfernen und `Rezent` verständlicher benennen
- #37 Filter- und Marker-Verhalten reproduzieren und absichern

### Nach Stabilisierung beziehungsweise nach der Prüfung

- #38 Mock-Ortsdaten erweitern
- #39 Nutzer-Titel und Achievements konzipieren
- #40 Rotierenden Header-Banner evaluieren

## 9. Nächste Schritte

1. Dokumentationsbranch zu #35 prüfen und mergen.
2. #36 als kleinen, risikoarmen UX-Fix umsetzen.
3. Für #37 gemeinsam exakte Reproduktionsschritte sammeln.
4. Vor dem Prüfungstag eine vollständige Generalprobe durchführen.
5. Bildschirmaufnahme, Screenshots und Debug-APK als Fallback bereithalten.
