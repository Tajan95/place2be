# Demo-Runbook für die Präsentation am 27.07.2026

Dieses Runbook beschreibt einen reproduzierbaren Live-Demo-Ablauf für den place2be-MVP. Es ergänzt die fachliche Präsentation und soll verhindern, dass wichtige Funktionen, Einschränkungen oder Datenschutzentscheidungen im Vortrag vergessen werden.

## 1. Ziel der Demo

Die Demo soll in etwa 7 bis 9 Minuten zeigen:

1. was place2be lösen soll,
2. wie ein neuer Nutzer die App versteht,
3. wie Orte entdeckt, gespeichert und bewertet werden,
4. wie aktuelle Bewertungen stärker zählen,
5. wie Reviews und Reaktionen funktionieren,
6. wie Nutzer-Score und Profile umgesetzt sind,
7. welche Architektur- und Datenschutzentscheidungen getroffen wurden,
8. welche Funktionen bewusst außerhalb des MVP liegen.

## 2. Empfohlene Rollenverteilung

Die genaue Zuordnung kann das Team vor der Präsentation festlegen.

- **Moderation / Produktidee:** Problem, Zielgruppe, Onboarding und Übergänge.
- **Live-Bedienung:** Map, Ortsdetail, Bookmark, Bewertung, Reviews und Profile.
- **Technik / Architektur:** Local-first, Schichten, Score-Logik, Datenschutz und Ausblick.

Eine Person sollte die App durchgehend bedienen. Sprecherwechsel erfolgen an klaren fachlichen Übergängen, nicht während kritischer Eingaben.

## 3. Vorbereitung am Präsentationstag

### 3.1 Repository und Tests

```powershell
git checkout main
git pull origin main
.\gradlew.bat testDebugUnitTest
```

Erwartung: `BUILD SUCCESSFUL`.

Optional zusätzlich ein installierbares Debug-APK vorbereiten:

```powershell
.\gradlew.bat assembleDebug
```

Erwarteter Pfad:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Das APK vor der Präsentation zusätzlich außerhalb des Projektordners sichern.

### 3.2 Emulator

Empfohlener, bereits erprobter Zustand:

- Pixel 9
- API 36
- Emulator vollständig gestartet
- Android Studio und PowerShell geöffnet

ADB-Verbindung prüfen:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

Erwartung:

```text
List of devices attached
emulator-5554    device
```

### 3.3 Reproduzierbaren Ausgangszustand herstellen

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
- vorhandene Seed-Orte, Seed-Reviews und fremde Community-Profile.

### 3.4 Präsentationsmaterial als Fallback

Vorher vorbereiten:

- Bildschirmaufnahme des vollständigen Demo-Flows,
- Screenshots jeder Runbook-Station,
- Debug-APK,
- lokale Kopie des Repositories,
- Folie mit Architekturübersicht,
- Folie oder Screenshot der Score-Formel,
- Folie mit MVP-Abgrenzung.

Die App ist local-first und benötigt für den normalen Flow kein Internet.

## 4. Live-Demo-Ablauf

## Station 1: Erststart und Onboarding

**Aktion**

App nach dem Daten-Reset starten und die vier Seiten nacheinander öffnen.

**Sprechtext / Kernaussagen**

- place2be hilft, öffentliche und niedrigschwellige Orte zu entdecken.
- Die Karte ist für den MVP bewusst stilisiert.
- Bewertet werden Vibes, Sicherheit und Erreichbarkeit.
- Aktuelle Bewertungen zählen stärker als alte.
- Eine echte Standortprüfung ist fachlich vorgesehen, im MVP aber simuliert.

**Zu zeigen**

- Fortschrittsanzeige,
- `Weiter` und `Zurück`,
- abschließender Button `Karte öffnen`.

**Optionaler Technikhinweis**

Der Abschluss wird als boolescher Wert in app-internen `SharedPreferences` gespeichert. Beim nächsten App-Start bleibt das Onboarding aus; derselbe Inhalt ist später im eigenen Profil als Hilfe erreichbar.

## Station 2: Mock-Map und Default-Schnellzugriffe

**Aktion**

Onboarding abschließen. Auf der Karte kurz Marker und Bottom-Sheet zeigen.

**Sprechtext / Kernaussagen**

- Die Mock-Map demonstriert den mobilen Interaktionsfluss ohne Karten-SDK und API-Key.
- Die Default-Ansicht bietet Filter, beliebte Orte und gespeicherte Orte.
- Marker und Listen führen in dieselbe Ortsansicht.

**Zu zeigen**

- einen oder zwei Marker,
- Schnellzugriffe,
- leere Liste unter `Gespeicherte Orte`.

**Wichtiger Hinweis**

Die leere Favoritenliste ist beabsichtigt. Sie zeigt, dass der Demo-Nutzer als neuer Nutzer startet und Bookmarks erst während der Demo erzeugt.

## Station 3: Ort auswählen und Mini-Preview

**Empfohlener Ort:** `Goetheplatz` oder alternativ `Mainufer`.

**Aktion**

Ort über Marker oder Liste auswählen.

**Sprechtext / Kernaussagen**

- Die Karte bleibt sichtbar.
- Eine kompakte Mini-Preview zeigt den wichtigsten Kontext.
- Das Bottom-Sheet kann zur vollständigen Detailansicht erweitert werden.

**Zu zeigen**

- Name und Ortsangabe,
- aggregierten Score,
- Übergang vom Peek-Zustand zur Detailansicht.

## Station 4: Ortsdetail und Bookmark

**Aktion**

Detailansicht erweitern und das Herz-/Bookmark-Icon aktivieren.

**Sprechtext / Kernaussagen**

- Der Ort enthält Beschreibung, Tags und getrennte Kriterienwerte.
- Bookmark-Aktionen werden lokal persistent gespeichert.
- Der ausgewählte Ort erscheint anschließend unter `Gespeicherte Orte`.

**Zu zeigen**

- Beschreibung und Ortseigenschaften,
- Vibes, Sicherheit und Erreichbarkeit,
- Bookmark-Zustandswechsel,
- optional Rückkehr zur gespeicherten Liste.

## Station 5: Bewertungslogik erklären

**Aktion**

Zum Bewertungsbereich scrollen, aber noch nicht sofort speichern.

**Sprechtext / Kernaussagen**

- Jedes Kriterium wird von 1 bis 5 bewertet.
- Die drei Kriterien werden getrennt zeitgewichtet.
- Der Gesamtwert ist der Mittelwert der gewichteten Kriterienwerte.
- Eine Bewertung ist fachlich nur vor Ort zulässig.
- Für die stabile Demo ist die Vor-Ort-Bestätigung simuliert.

**Formel**

```text
Gewicht = 1 / (1 + Alter_in_Tagen × 0,05)
```

**Beispiel**

- neue Bewertung: Gewicht `1,0`,
- 30 Tage alte Bewertung: Gewicht `0,4`.

Alte Bewertungen werden nicht gelöscht, sondern beeinflussen das aktuelle Bild schwächer.

## Station 6: Bewertung und Textrezension abgeben

**Aktion**

Beispielwerte auswählen, etwa:

- Vibes: `4`,
- Sicherheit: `3`,
- Erreichbarkeit: `5`.

Optionalen Text mit mindestens 20 Zeichen eingeben, zum Beispiel:

```text
Angenehmer Treffpunkt mit guter Anbindung.
```

Bewertung speichern.

**Erwartetes Ergebnis**

- neue Rezension erscheint ohne App-Neustart,
- Kriterienwerte und Orts-Score werden aus dem Repository neu gelesen,
- rollierender Jahreszähler erhöht sich,
- Bewertungsformular zeigt anschließend die 24-Stunden-Sperre,
- eigener Nutzer-Score erhält Aktivitätspunkte.

**Sprechtext / Kernaussagen**

- Zahlenbewertungen funktionieren auch ohne Text.
- Ein ausreichend langer Text gibt einen kleinen zusätzlichen Aktivitätsbonus.
- Die 24-Stunden-Regel begrenzt kurzfristiges Spam-Verhalten.

## Station 7: Reviews, Sortierung und Reaktionen

**Aktion**

- zwischen `Rezent` und `Beliebt` wechseln,
- einen langen Text auf- und einklappen,
- bei einer fremden Rezension Like oder Dislike antippen,
- dieselbe Reaktion erneut antippen oder zur Gegenseite wechseln.

**Sprechtext / Kernaussagen**

- Rezent sortiert nach Zeitstempel.
- Beliebt berücksichtigt Netto-Reaktionen und Alter.
- Pro Nutzer und Review ist höchstens eine Reaktion möglich.
- Eigene Reviews können nicht selbst bewertet werden.
- Reaktionen bleiben nach App-Neustart erhalten.

**Aufbewahrungshinweis**

Pro Ort bleiben höchstens 50 nichtleere Rezensionstexte gespeichert. Wird ein Text verdrängt, bleiben Kriterienwerte, Nutzerbezug und Zeitstempel erhalten und fließen weiterhin in die Scores ein.

## Station 8: Öffentliches Profil eines Review-Autors

**Aktion**

Icon oder Pseudonym eines fremden Review-Autors antippen.

**Zu zeigen**

- Community-Profil,
- Gesamt-Score,
- Aktivität,
- Reputation,
- aggregierte Bewertungs- und Reaktionszahlen,
- Datenschutzhinweis,
- keine chronologische Historie.

**Sprechtext / Kernaussagen**

- Einzelne Reviews bleiben am jeweiligen Ort sichtbar.
- Das öffentliche Profil führt diese Orte nicht zu einem Bewegungsprofil zusammen.
- Die sichtbaren Daten sind auf Community-Vertrauen und Reputation beschränkt.

**Navigationstest**

Zurück zur Ortsansicht gehen. Der vorherige Ort und Review-Kontext sollen erhalten bleiben.

## Station 9: Eigenes Profil und Nutzer-Score

**Aktion**

Eigenes Profil über das Profil-Icon öffnen.

**Zu zeigen**

- Pseudonym und neutrales Icon,
- Gesamt-Score,
- Aktivität und Reputation,
- Aufschlüsselung nach Bewertung, Text und Reaktion,
- private Bewertungshistorie,
- gerade erstellten Eintrag,
- Hilfe und vorbereitete Einstellungen.

**Sprechtext / Kernaussagen**

- Der Nutzer-Score ist kein statisches Seed-Feld, sondern wird dynamisch berechnet.
- Erkundungsboni nehmen ab.
- nahe Ortscluster und massenhafte Reaktionen werden begrenzt.
- Reputation wächst logarithmisch aus hilfreichen Reaktionen auf eigene Texte.
- Das eigene Profil darf die vollständige private Historie zeigen; öffentliche Profile nicht.

## Station 10: Hilfe erneut öffnen

**Aktion**

Im eigenen Profil den vollständigen Hilfefluss öffnen und direkt wieder schließen.

**Sprechtext / Kernaussagen**

- Onboarding-Inhalte bleiben später erreichbar.
- Hilfe und Einstellungen sind private Profilaktionen.
- Öffentliche Profile zeigen diese Funktionen nicht.

## Station 11: Account-Abgrenzung erklären

**Sprechtext**

> Für den MVP verwenden wir einen bereits angemeldeten, pseudonymisierten Demo-Nutzer. Registrierung, Login und Profilerstellung sind bewusst nicht umgesetzt. Der Schwerpunkt liegt auf Ortsentdeckung, Bewertungen, Scores, Reviews und Datenschutz. Ein produktiver Account-Lifecycle würde zusätzlich Backend, Authentifizierung, Wiederherstellung, Einwilligungen, Kontolöschung und Synchronisation erfordern.

**Wichtig**

Diese Aussage als bewusste Scope-Entscheidung formulieren, nicht als überraschend fehlende Funktion.

## Station 12: Architektur kurz erklären

Empfohlene Dauer: 60 bis 90 Sekunden.

**Schichten**

```text
Compose-Screens
      ↓
ViewModel-artige Aufbereitung
      ↓
Domain-Use-Cases und Repository-Interfaces
      ↓
Local-first-Repositories und JSON-Arbeitskopien
```

**Konkrete Beispiele**

- `CalculatePlaceScoreUseCase` enthält die Bewertungsalterung.
- `CalculateUserScoreUseCase` enthält Aktivität und Reputation.
- `ReviewSubmissionCooldownPolicy` enthält die 24-Stunden-Regel.
- `ProfileViewModel` trennt `OWN` und `PUBLIC`.
- `MockPlaceDataSource` kapselt persistente JSON-Arbeitskopien und Textaufbewahrung.

**Pragmatische technische Entscheidungen**

- Navigation über Compose-Zustand statt Navigation Compose,
- ViewModel-artige Kotlin-Klassen statt durchgängiger nativer Lifecycle-ViewModels,
- manuelle Zusammensetzung statt Dependency-Injection-Framework.

Diese Punkte sind bekannte MVP-Kompromisse und mögliche Refactoring-Schritte.

## 5. Ehrlich zu benennende Einschränkungen

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

## 6. Fallback-Szenarien

### A. Emulator wird nicht erkannt

1. Device Manager öffnen und Emulator starten.
2. ADB prüfen:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

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

APK installieren:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
```

### D. App stürzt während der Demo ab

1. App einmal neu öffnen.
2. Nicht sofort Daten löschen, damit bereits erzeugte Zustände erhalten bleiben.
3. Falls der Fehler reproduzierbar bleibt, zur Bildschirmaufnahme wechseln.
4. Kurz transparent sagen, dass der gezeigte Flow lokal bereits getestet wurde.

### E. Einzelne Aktion funktioniert nicht

Nicht lange in der Oberfläche suchen. Zum nächsten fachlichen Punkt wechseln und die vorbereiteten Screenshots oder die Aufnahme verwenden.

## 7. Kurzfassung für den Notfall

Falls nur noch drei Minuten verfügbar sind:

1. Onboarding-Seite mit Kriterien zeigen.
2. Karte öffnen und einen Ort auswählen.
3. Detailansicht mit Bookmark und Reviews zeigen.
4. Bewertung mit Text speichern.
5. eigenes Profil mit Score und Historie zeigen.
6. fremdes Profil öffnen und Datenschutzbegrenzung erklären.
7. Local-first, Mock-Map, simulierten Standort und Demo-Nutzer als Abgrenzung nennen.

## 8. Abschlussbotschaft

> place2be demonstriert nicht nur eine Oberfläche, sondern einen vollständigen Local-first-Flow: öffentliche Orte entdecken, aktuelle Eindrücke bewerten, hilfreiche Reviews einordnen und Reputation sichtbar machen – mit bewusst begrenzten öffentlichen Profildaten und klar dokumentiertem Ausbaupfad zu Karte, Standort, Backend und echten Accounts.