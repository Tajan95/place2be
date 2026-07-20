# Demo-Runbook für die Präsentation am 27.07.2026

Dieses Runbook beschreibt den gemeinsamen Präsentations- und Live-Demo-Ablauf für den place2be-MVP. Es wurde nach dem Blindtest und Abschlussmeeting vom 20.07.2026 gestrafft.

Die ausführlichen Meeting-Ergebnisse stehen in [`meeting-notes-2026-07-20.md`](meeting-notes-2026-07-20.md).

## 1. Ziel und roter Faden

Die Präsentation soll nicht nur die fertige Oberfläche zeigen, sondern den nachvollziehbaren Weg von der Idee über Issues und Prototypen bis zur Android-Implementierung.

Der rote Faden lautet:

```text
GitHub und Prototypen
        ↓
README und Produktidee
        ↓
Onboarding und fachliche Kernlogik
        ↓
kompakte Live-Demo
        ↓
technische Architektur
        ↓
Limitationen und Ausblick
```

Die App-Demo selbst soll möglichst geradlinig bleiben. Detailwissen, alternative Stationen und Fallbacks stehen weiter unten als Nachschlagewerk.

## 2. Kompakter Hauptablauf

### Phase A: GitHub, Issues und Prototypen

**Ziel:** zeigen, wie das Team Anforderungen, Bugs und Entscheidungen organisiert hat.

**Zu zeigen:**

- Repository-Startseite,
- Issue-Liste,
- ein abgeschlossenes Feature-Issue,
- ein Bug- oder Nachbesserungs-Issue,
- handschriftliche Prototypen beziehungsweise Mockup-Notizen,
- Commit- oder Pull-Request-Verlauf nur kurz.

**Kernaussage:**

> Wir haben die App iterativ entwickelt: aus Meeting- und Mockup-Notizen wurden Issues, aus Issues getrennte Branches und aus getesteten Branches nachvollziehbare Pull Requests.

### Phase B: Android Studio und Produktidee

**Zu zeigen:**

- Android Studio mit geöffneter Projektstruktur,
- `README.md`,
- optional kurz `docs/architekturentscheidungen.md`.

**Kernaussagen:**

- place2be hilft, öffentliche und niedrigschwellig nutzbare Orte zu entdecken,
- bewertet werden Vibes, Sicherheit und Erreichbarkeit,
- aktuelle Eindrücke zählen stärker als alte,
- der MVP ist local-first und offline demonstrierbar.

Danach die App starten.

### Phase C: Onboarding und Bewertungslogik

Die vier Onboarding-Seiten nacheinander zeigen, ohne jede Textzeile vorzulesen.

**Erklären:**

- Produktzweck,
- Mock-Map und Bottom-Sheet,
- Vibes, Sicherheit und Erreichbarkeit,
- Bewertungsalterung,
- simulierte Vor-Ort-Bestätigung.

**Zeitgewichtung:**

```text
Gewicht = 1 / (1 + Alter_in_Tagen × 0,05)
```

Beispiele:

- neue Bewertung: Gewicht `1,0`,
- 30 Tage alte Bewertung: Gewicht `0,4`.

Alte Bewertungen werden nicht gelöscht, sondern beeinflussen das aktuelle Bild schwächer.

**Schutz vor einfacher Score-Maximierung:**

- 24-Stunden-Sperre pro Nutzer und Ort,
- abnehmender Bonus für immer weitere Orte,
- Begrenzung naher Ortscluster,
- Tagesgrenzen für Reaktionsaktivität,
- logarithmisch wachsende Reputation,
- Obergrenze pro hilfreicher Rezension.

### Phase D: Kompakte Live-Demo

Der bevorzugte Kernflow lautet:

```text
Mock-Map
  → Ort auswählen
  → Bookmark setzen
  → Detail und Kriterien zeigen
  → Bewertung mit Text speichern
  → Reviews und Reaktion zeigen
  → eigenes und öffentliches Profil vergleichen
```

Nicht in jeder Station lange verweilen. Die fachliche Wirkung der Aktion ist wichtiger als das vollständige Vorlesen aller UI-Texte.

### Phase E: Technische Architektur

Nach dem fachlichen Flow übernimmt Morris den Architekturteil.

**Empfohlener Übergabesatz:**

> Damit haben wir gezeigt, was place2be fachlich leistet und wie wir veraltete oder leicht manipulierbare Bewertungen begrenzen. Morris erklärt jetzt, wie wir diese Funktionen mit Jetpack Compose, einer MVVM-orientierten Struktur und Repository-Grenzen technisch aufgebaut haben.

Zu behandeln sind:

- Android- und Kotlin-Konventionen,
- Jetpack Compose,
- MVVM-orientierte Struktur,
- Repository Pattern,
- Domain-/Data-Trennung,
- stateful und stateless Composables,
- bekannte technische MVP-Kompromisse.

### Phase F: Limitationen, Ausblick und Abschluss

Bewusst nicht umgesetzt:

- Live-Karte,
- echte GPS- und Mindestaufenthaltsprüfung,
- produktive Registrierung und Kontoverwaltung,
- Backend und Mehrgeräte-Synchronisation,
- serverseitige Moderation und Manipulationsschutz,
- vollständige Navigation-Compose- und Lifecycle-ViewModel-Integration.

Diese Punkte als bewusste Scope-Entscheidungen darstellen, nicht als überraschend fehlende Funktionen.

## 3. Vorläufige Rollenverteilung

Die Rollen können in der Generalprobe noch angepasst werden.

### Tajan

- Produktidee und Zielgruppe,
- README und Übergang in die App,
- Onboarding,
- Zeitverfall und Schutz vor Score-Maximierung,
- Moderation oder Bedienung des fachlichen Kernflows.

### Morris

- Android-Konventionen,
- Jetpack Compose,
- MVVM-orientierte Struktur,
- Repository Pattern,
- Domain-/Data-Trennung,
- stateful und stateless Composables,
- technische Kompromisse.

### Artem

Geeignete Blöcke:

- GitHub, Issues und Entwicklungsprozess,
- Prototypen und Übergang vom Mockup zur App,
- Limitationen und Ausblick,
- alternativ Unterstützung bei Live-Bedienung und Fallback-Material.

Eine Person sollte die App durchgehend bedienen. Sprecherwechsel erfolgen an fachlichen Übergängen, nicht während Eingaben oder Animationen.

## 4. Vorbereitung am Präsentationstag

### 4.1 Repository und Tests

```powershell
git checkout main
git pull origin main
.\gradlew.bat testDebugUnitTest
```

Erwartung: `BUILD SUCCESSFUL`.

Optional zusätzlich ein Debug-APK vorbereiten:

```powershell
.\gradlew.bat assembleDebug
```

Erwarteter Pfad:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Das APK zusätzlich außerhalb des Projektordners sichern.

### 4.2 Emulator

Erprobter Zustand:

- Pixel 9,
- API 36,
- Emulator vor dem ADB-Befehl vollständig gestartet,
- Android Studio und PowerShell geöffnet.

ADB prüfen:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

Erwartung:

```text
List of devices attached
emulator-5554    device
```

### 4.3 Frischen Ausgangszustand herstellen

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell pm clear de.place2be
```

Erwartung: `Success`.

Danach die App über Android Studio starten.

Der frische Demo-Zustand besitzt:

- noch nicht abgeschlossenes Onboarding,
- keine eigenen Bewertungen,
- Nutzer-Score `0`,
- leere private Historie,
- keine gespeicherten Orte,
- Seed-Orte, Seed-Reviews und fremde Community-Profile.

### 4.4 Präsentationsmaterial als Fallback

Vorher vorbereiten:

- Bildschirmaufnahme des vollständigen Flows,
- Screenshots der wichtigsten Stationen,
- Debug-APK,
- lokale Repository-Kopie,
- Architekturübersicht,
- Score-Formel,
- MVP-Abgrenzung,
- Bilder der Prototypen.

Die App benötigt im normalen Local-first-Flow kein Internet.

## 5. Detaillierter App-Demo-Ablauf

## Station 1: Erststart und Onboarding

**Aktion:** App nach dem Daten-Reset starten und die vier Seiten zügig durchgehen.

**Zu zeigen:**

- Fortschrittsanzeige,
- `Weiter` und `Zurück`,
- `Karte öffnen`.

**Technikhinweis:** Der Abschluss wird als boolescher Wert in app-internen `SharedPreferences` gespeichert. Derselbe Inhalt ist später im eigenen Profil als Hilfe erreichbar.

## Station 2: Mock-Map und Bookmark

**Aktion:** Onboarding abschließen, `Gespeicherte Orte` kurz leer zeigen, dann einen Ort auswählen und das Herz aktivieren.

**Empfohlener Ort:** `Goetheplatz` oder `Mainufer`.

**Kernaussagen:**

- Die Mock-Map zeigt den mobilen Interaktionsfluss ohne Karten-SDK und API-Key.
- Marker und Listen führen in denselben Ortskontext.
- Der frische Demo-Nutzer beginnt absichtlich ohne Bookmarks.
- Das gesetzte Bookmark wird lokal persistent gespeichert.

## Station 3: Ortsdetail und Bewertung

**Aktion:** Bottom-Sheet erweitern, Beschreibung, Tags und drei Kriterien zeigen. Danach zum Bewertungsbereich scrollen.

Beispielwerte:

- Vibes: `4`,
- Sicherheit: `3`,
- Erreichbarkeit: `5`.

Optionaler Text:

```text
Angenehmer Treffpunkt mit guter Anbindung.
```

**Erwartung nach dem Speichern:**

- Rezension erscheint ohne Neustart,
- Orts- und Kriterienwerte werden neu gelesen,
- Jahreszähler erhöht sich,
- 24-Stunden-Sperre wird sichtbar,
- Nutzer-Score erhält Aktivitätspunkte.

## Station 4: Reviews und Reaktionen

**Aktion:**

- zwischen zeitbasierter Sortierung und `Beliebt` wechseln,
- einen langen Text auf- und einklappen,
- bei einer fremden Rezension Like oder Dislike setzen,
- dieselbe Reaktion erneut antippen oder wechseln.

Nach Umsetzung von Issue #36 heißt die zeitbasierte Sortierung `Zuletzt`; davor kann in einem Zwischenstand noch `Rezent` sichtbar sein.

**Kernaussagen:**

- Zeitbasierte Sortierung ordnet nach neuestem Zeitstempel.
- `Beliebt` berücksichtigt Netto-Reaktionen und Alter.
- Pro Nutzer und Review ist höchstens eine Reaktion möglich.
- Eigene Reviews können nicht selbst bewertet werden.
- Reaktionen bleiben persistent.
- Pro Ort werden höchstens 50 nichtleere Rezensionstexte gespeichert; numerische Bewertungsdaten bleiben erhalten.

## Station 5: Öffentliches Profil

**Aktion:** Autor-Icon oder Pseudonym einer fremden Rezension antippen.

**Zu zeigen:**

- Gesamt-Score,
- Aktivität,
- Reputation,
- aggregierte Bewertungs- und Reaktionszahlen,
- keine chronologische Ortshistorie.

**Kernaussage:** Einzelne Rezensionen bleiben beim Ort sichtbar, werden aber nicht zu einem öffentlichen Bewegungsprofil zusammengeführt.

Danach zurückgehen. Der Orts- und Review-Kontext soll erhalten bleiben.

## Station 6: Eigenes Profil und Hilfe

**Aktion:** Eigenes Profil öffnen und anschließend den vollständigen Hilfefluss kurz aufrufen.

**Zu zeigen:**

- dynamischer Gesamt-Score,
- Aktivität und Reputation,
- Punkteaufschlüsselung,
- private Bewertungshistorie,
- neu erstellten Eintrag,
- Hilfe und vorbereitete Einstellungen.

**Account-Abgrenzung:**

> Für den MVP verwenden wir einen bereits angemeldeten, pseudonymisierten Demo-Nutzer. Registrierung, Login und Profilerstellung sind bewusst nicht umgesetzt. Der Schwerpunkt liegt auf Ortsentdeckung, Bewertungen, Scores, Reviews und Datenschutz.

## 6. Architekturblock für Morris

Empfohlene Dauer: etwa zwei Minuten.

### 6.1 Jetpack Compose und Android-Konventionen

- Deklarative Kotlin-UI statt XML-Layouts.
- Material 3 für konsistente Android-Komponenten.
- UI-Zustand und Aktionen werden möglichst über Parameter und Callbacks weitergegeben.
- Fachliche Regeln liegen nicht direkt in Composables.

### 6.2 MVVM-orientierte Struktur

```text
Compose-Screens
      ↓
ViewModel-artige Aufbereitung
      ↓
Domain-Use-Cases und Repository-Interfaces
      ↓
Local-first-Repositories und JSON-Arbeitskopien
```

Konkrete Beispiele:

- `CalculatePlaceScoreUseCase`: Bewertungsalterung,
- `CalculateUserScoreUseCase`: Aktivität und Reputation,
- `ReviewSubmissionCooldownPolicy`: 24-Stunden-Regel,
- `ProfileViewModel`: Trennung von `OWN` und `PUBLIC`,
- `MockPlaceDataSource`: persistente JSON-Arbeitskopien und Textaufbewahrung.

### 6.3 Repository Pattern

- UI und ViewModel-artige Klassen lesen keine JSON-Dateien direkt.
- Repository-Interfaces bilden fachliche Datenzugriffsverträge.
- Die Local-first-Implementierungen können später durch Remote-Repositories ergänzt werden.

### 6.4 Stateful und stateless Composables

- Stateless Composables erhalten sichtbaren Zustand und Callbacks von außen.
- Stateful Composables koordinieren Zustand mit `remember`, `rememberSaveable` oder über ViewModel-artige State-Holder.
- `Place2BeApp` ist die stateful Composition Root für Navigation, Auswahlzustände und Abhängigkeiten.
- Kleinere Darstellungskomponenten bleiben möglichst stateless.
- In `MapScreenWithRatingEntry` verbleibt aus pragmatischen MVP-Gründen lokaler UI-Zustand für Slider, Textfeld, Sortierung, Aufklappen und Scrollposition.

### 6.5 Bekannte technische Kompromisse

- Navigation über Compose-Zustand statt Navigation Compose,
- nicht durchgängig native Lifecycle-`ViewModel`-Basisklassen,
- manuelle Dependency-Zusammensetzung,
- simulierte Standortbestätigung,
- lokale statt synchronisierte Daten.

## 7. Ehrlich zu benennende Einschränkungen

- Mock-Map statt echter Live-Karte,
- simulierte Standortbestätigung,
- fester Demo-Nutzer statt Registrierung und Login,
- lokale Daten ohne zentrale Synchronisation,
- kein serverseitiger Manipulationsschutz,
- keine produktive Moderation,
- keine vollständige Account- und Einwilligungsverwaltung,
- Navigation Compose und native Lifecycle-ViewModels nicht integriert,
- Einstellungen nur vorbereitet,
- Score-Konstanten fachlich begründet, aber noch nicht mit realen Nutzungsdaten kalibriert.

Nicht mehr als offen darstellen:

- öffentliche Profilhistorie: datenschutzbewusst entschieden und umgesetzt,
- Nutzer-Score-Normalisierung: für den MVP umgesetzt,
- Textaufbewahrung: persistent umgesetzt,
- Review-Reaktionen: accountgebunden umgesetzt,
- Onboarding: integriert und persistent umgesetzt.

## 8. Fallback-Szenarien

### A. Emulator wird nicht erkannt

1. Device Manager öffnen und Emulator starten.
2. ADB prüfen.
3. Bei Bedarf ADB neu starten:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" kill-server
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" start-server
```

### B. Ausgangsdaten sind nicht frisch

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell pm clear de.place2be
```

Danach App erneut starten.

### C. Build schlägt kurz vor der Präsentation fehl

- keine Live-Codeänderungen mehr durchführen,
- vorbereitetes Debug-APK installieren,
- alternativ Bildschirmaufnahme verwenden,
- Unit-Test-Ergebnis und Architektur anhand der Folien erklären.

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
```

### D. App stürzt während der Demo ab

1. App einmal neu öffnen.
2. Nicht sofort Daten löschen, damit bereits erzeugte Zustände erhalten bleiben.
3. Falls der Fehler reproduzierbar bleibt, zur Bildschirmaufnahme wechseln.
4. Kurz transparent erklären, dass der Flow lokal getestet wurde.

### E. Einzelne Aktion funktioniert nicht

Nicht lange in der Oberfläche suchen. Zum nächsten fachlichen Punkt wechseln und Screenshots oder Aufnahme verwenden.

## 9. Kurzfassung für Zeitnot

1. GitHub-Issue und Prototyp zeigen.
2. README und Produktidee erklären.
3. Onboarding-Seite mit Kriterien und Alterung zeigen.
4. Karte öffnen und einen Ort auswählen.
5. Bookmark und Bewertung mit Text demonstrieren.
6. eigenes und öffentliches Profil vergleichen.
7. Morris erklärt Compose, MVVM, Repository und State.
8. Mock-Map, simulierten Standort und Demo-Nutzer als Abgrenzung nennen.

## 10. Abschlussbotschaft

> place2be demonstriert nicht nur eine Oberfläche, sondern einen vollständigen Local-first-Flow: öffentliche Orte entdecken, aktuelle Eindrücke bewerten, hilfreiche Reviews einordnen und Reputation sichtbar machen – mit bewusst begrenzten öffentlichen Profildaten und einem klar dokumentierten Ausbaupfad zu echter Karte, Standortprüfung, Backend und Accounts.
