# Architekturentscheidungen

Dieses Dokument hält die verbindlichen technischen und fachlichen Entscheidungen für den place2be-MVP fest. Die Entscheidungen sind bewusst pragmatisch und auf eine stabile, nachvollziehbare Demonstration am 27.07.2026 ausgerichtet.

## ADR-001: Kotlin als Programmiersprache

**Status:** entschieden und umgesetzt

place2be wird in Kotlin entwickelt. Kotlin passt zur modernen Android- und Jetpack-Compose-Entwicklung und reduziert Boilerplate-Code gegenüber Java.

**Konsequenz:** Neue App-Logik wird in Kotlin geschrieben.

## ADR-002: Jetpack Compose statt XML-Layouts

**Status:** entschieden und umgesetzt

Die Benutzeroberfläche wird deklarativ mit Jetpack Compose beschrieben. Klassische XML-Layouts werden im MVP vermieden.

**Konsequenz:** Screens und wiederverwendbare UI-Komponenten werden als Composable-Funktionen umgesetzt.

## ADR-003: Material 3 als UI-Grundlage

**Status:** entschieden und umgesetzt

Material 3 bildet die Grundlage für Buttons, Cards, Bottom-Sheets, Dialoge, Listen, Chips und das Farbschema.

**Konsequenz:** Die Oberfläche bleibt Android-typisch und visuell konsistent, ohne ein vollständiges eigenes Komponenten-Framework zu benötigen.

## ADR-004: Local-first-MVP statt sofortigem Backend

**Status:** entschieden für den MVP und umgesetzt

Orte, Bewertungen, Textrezensionen, Nutzer und Bookmarks werden als versionierte JSON-Seed-Dateien ausgeliefert. Beim ersten Start kopiert `MockPlaceDataSource` diese Dateien in den internen App-Speicher. Review-Reaktionen und der Onboarding-Abschluss werden ebenfalls app-intern persistiert.

**Begründung:** Ein echtes Backend mit Nutzerkonten, zentraler Datenbank, Synchronisation, Moderation und Manipulationsschutz erhöht den Projektumfang stark. Für die Fallstudie ist eine stabile, offline demonstrierbare App wichtiger.

**Konsequenz:** Die UI greift über Repository-Interfaces auf lokale Datenquellen zu. Eine spätere Remote-Datenquelle kann hinter denselben fachlichen Verträgen ergänzt werden. Die Live-Demo benötigt keinen Netzwerkzugriff.

## ADR-005: Standortbestätigung wird im MVP simuliert

**Status:** entschieden für den MVP und umgesetzt

Die Produktidee bindet Bewertungen an einen tatsächlichen Vor-Ort-Aufenthalt. Im MVP wird dieser Zustand über `LocationConfirmationState.SIMULATED_CONFIRMED` simuliert. Die Architektur enthält Regeln für bestätigte und nicht bestätigte Zustände; eine echte GPS- und Mindestaufenthaltsprüfung wird nicht ausgeführt.

**Begründung:** Echte Standortprüfung erfordert Permissions, Datenschutzkonzept, Emulator-Setup, Toleranzen für ungenaue Messungen und Schutz vor Manipulation.

**Konsequenz:** Die Demo kann den Bewertungsflow zuverlässig zeigen. Der simulierte Zustand ist ausdrücklich kein Produktionszustand.

## ADR-006: Bewertungsalter als Kernbestandteil des Orts-Rankings

**Status:** entschieden und umgesetzt

Vibes, Sicherheit und Erreichbarkeit werden separat zeitgewichtet. Der Gesamtwert ist anschließend der Mittelwert der drei gewichteten Kriterienwerte.

```text
Gewicht = 1 / (1 + Alter_in_Tagen × 0,05)
```

Eine neue Bewertung hat das Gewicht `1,0`; eine 30 Tage alte Bewertung noch `0,4`. Alte Bewertungen bleiben erhalten, beeinflussen den aktuellen Score aber zunehmend schwächer.

**Begründung:** Öffentliche Orte verändern sich. Aktuelle Eindrücke sollen deshalb stärker zählen als historische Bewertungen.

**Konsequenz:** `CalculatePlaceScoreUseCase` liefert ein gemeinsames `PlaceScoreResult` für Kartenmarker, Mini-Preview und Detailansicht. Der sichtbare Aktivitätszähler betrachtet separat ein rollierendes Ein-Jahres-Fenster.

## ADR-007: Feature- und MVVM-orientierte Struktur

**Status:** entschieden und im MVP pragmatisch umgesetzt

Größere Funktionsbereiche besitzen Compose-Screens und ViewModel-artige Klassen. Fachliche Berechnungen liegen in `domain/usecase`, Datenzugriffe hinter Repository-Interfaces.

Die aktuelle Implementierung verwendet bewusst schlanke Kotlin-Klassen statt durchgängig von `androidx.lifecycle.ViewModel` abzuleiten. App-Navigation wird über speicherbaren Compose-Zustand statt Navigation Compose gesteuert.

### Zustand in Jetpack Compose

Zustand wird nach Möglichkeit nach oben verlagert (**State Hoisting**). Untergeordnete Composables erhalten vorbereitete Daten und melden Aktionen über Callbacks zurück.

- **Stateless Composables** erhalten ihren sichtbaren Zustand vollständig über Parameter und greifen nicht selbst auf Repositories oder fachliche Datenquellen zu.
- **Stateful Composables** halten oder koordinieren UI-Zustand, beispielsweise über `remember`, `rememberSaveable` oder ViewModel-artige State-Holder.
- `Place2BeApp` bildet die stateful Composition Root für Repository-Zusammensetzung, Zielzustände, ausgewählten Ort, betrachtetes Profil und Datenrevisionen.
- Kleinere Darstellungskomponenten wie Filterzeilen, Rating-Pills und Listenzeilen bleiben möglichst stateless.
- Im integrierten Map-/Detail-Flow verbleibt bewusst lokaler UI-Zustand für Slider, Rezensionstext, Sortierung, Aufklappen und Scrollposition.

**Begründung:** Die Trennung macht Score- und Regelwerke testbar, ohne für den begrenzten MVP zusätzlich ein vollständiges Dependency-Injection-, Navigation- und Lifecycle-Setup einzuführen. State Hoisting reduziert gleichzeitig unnötige Kopplung und erleichtert die Wiederverwendung kleiner UI-Komponenten.

**Konsequenz:** Eine spätere technische Härtung kann native Lifecycle-ViewModels, `SavedStateHandle`, Navigation Compose und Dependency Injection ergänzen. Komplexe Screens können weiter in State-Holder und stateless Unterkomponenten zerlegt werden. Die fachlichen Use Cases und Repository-Grenzen bleiben davon weitgehend unabhängig.

## ADR-008: Mock-Map und zusammenhängendes Bottom-Sheet

**Status:** entschieden für den MVP und umgesetzt

Der MVP verwendet eine stilisierte Mock-Map. Ohne ausgewählten Ort zeigt sie Schnellzugriffe. Nach Auswahl eines Markers erscheint eine Mini-Preview, die zur Detail-, Bewertungs- und Review-Ansicht erweitert wird.

Die Bewertung ist keine separate Vollbildseite. Map, Preview, Detailinformationen, Slider, optionaler Rezensionstext und vorhandene Reviews bilden einen räumlich zusammenhängenden Flow.

**Begründung:** Die Produktidee wird ohne Karten-SDK, API-Key und Live-Geodaten klar demonstriert. Gleichzeitig bleibt der räumliche Kontext erhalten.

**Konsequenz:** Eine echte Kartenintegration bleibt Ausblick. Der aktive Demo-Flow liegt vor allem in `MapScreen` und `MapScreenWithRatingEntry`.

## ADR-009: Profil, Nutzer-Score, Textrezensionen, Reaktionen und Bookmarks gehören zum MVP

**Status:** entschieden und umgesetzt

Der MVP enthält:

- eigenes und öffentliches Profil,
- dynamischen Nutzer-Score aus Aktivität und Reputation,
- Textrezensionen,
- accountgebundene Likes und Dislikes,
- persistente Bookmarks.

Die Regeln zur Normalisierung und Begrenzung gegen Score-Farming sind in `docs/nutzer-score-regeln.md` dokumentiert.

**Konsequenz:** Das Dashboard bleibt auf die Karte fokussiert. Profil und Score sind über das Profil-Icon erreichbar; öffentliche Profile werden über Autorenbereiche in Rezensionen geöffnet.

## ADR-010: Datenschutzbewusste Trennung zwischen eigenem und öffentlichem Profil

**Status:** entschieden und umgesetzt

Das **eigene Profil** zeigt die vollständige private Bewertungs- und Rezensionshistorie mit Ort, Datum, Kriterienwerten und vorhandenem Text.

Ein **fremdes öffentliches Profil** zeigt ausschließlich:

- Pseudonym und neutrales Profil-Icon,
- Gesamt-Score,
- Aktivitäts- und Reputationspunkte,
- Anzahl der Bewertungen und Textrezensionen,
- aggregierte hilfreiche positive Reaktionen.

Eine chronologische Liste bewerteter Orte wird öffentlich nicht zusammengeführt. Einzelne Reviews bleiben nur im Kontext des jeweiligen Ortes sichtbar.

**Begründung:** Zeitstempel und wiederkehrende Orte könnten Rückschlüsse auf Wohnort, Routinen, Aufenthaltszeiten und Identität erlauben. Die Trennung folgt dem Prinzip der Datenminimierung.

**Konsequenz:** `ProfileViewModel` unterscheidet zwischen `OWN` und `PUBLIC`. Hilfe, Einstellungen und private Historie sind nur im eigenen Profil erreichbar. Mehr technisch verfügbare Daten dürfen in einer späteren Backend-Version nicht automatisch öffentlich werden.

## ADR-011: Gespeicherte Rezensionen erscheinen direkt in der Ortsdetailansicht

**Status:** entschieden und umgesetzt

Nach dem Speichern wird die Repository-Schicht erneut gelesen. Eine neue Textrezension erscheint dadurch unmittelbar im Review-Bereich desselben Bottom-Sheets.

Die Sortierung **Rezent** ordnet nach Zeitstempel. **Beliebt** verwendet Netto-Reaktionen und eine logarithmisch wachsende Altersstrafe. Die sichtbare Bezeichnung `Rezent` wird mit Issue #36 in das verständlichere `Zuletzt` geändert; die interne Semantik bleibt identisch.

**Konsequenz:** Eingabe, sichtbare Rückmeldung, Auf- und Einklappen, Reaktionen sowie Sortierung bilden einen zusammenhängenden Review-Flow.

## ADR-012: Eine Bewertung pro Nutzer, Ort und 24-Stunden-Zeitraum

**Status:** entschieden und umgesetzt

Derselbe Nutzer darf denselben Ort erst 24 Stunden nach seiner letzten Bewertung erneut bewerten. Die Sperre gilt für reine Zahlenbewertungen und Bewertungen mit Text.

**Begründung:** Die Regel begrenzt kurzfristigen Spam und Manipulation, ohne neue Eindrücke zu einem späteren Zeitpunkt auszuschließen.

**Konsequenz:** `ReviewSubmissionCooldownPolicy` berechnet die Verfügbarkeit. Während der Sperre sind Slider und Textfeld deaktiviert; der Button zeigt die verbleibende Zeit.

## ADR-013: Pro Ort werden höchstens 50 Rezensionstexte gespeichert

**Status:** entschieden und persistent umgesetzt

Die zeitbasierte Ansicht und **Beliebt** zeigen pro Ort höchstens 50 Textrezensionen. Die lokale Datenhaltung bewahrt ebenfalls nur die 50 neuesten nichtleeren Texte auf.

Wird ein älterer Text verdrängt, wird ausschließlich `text` auf `null` gesetzt. UUID, Ort, Nutzer, Kriterienwerte, Zeitstempel und Reaktionszähler bleiben erhalten. Bereits vorhandene übergroße lokale Datenbestände werden beim Initialisieren normalisiert.

**Konsequenz:** Numerische Bewertungen bleiben Teil von Orts-Score, Jahreszähler, Nutzer-Score und privater Historie; nur der verdrängte Freitext entfällt.

## ADR-014: Bewertungsanzahl als rollierender Ein-Jahres-Ausschnitt

**Status:** entschieden und umgesetzt

Die Detailansicht zeigt die Anzahl der Bewertungen aus den vergangenen 365 Tagen. Ältere Bewertungen bleiben in der zeitgewichteten Berechnung, erscheinen aber nicht im sichtbaren Aktivitätszähler.

**Begründung:** Ein reiner Gesamtzähler könnte hohe historische Aktivität fälschlich als aktuellen Community-Zustand darstellen.

**Konsequenz:** Die Seed-Daten enthalten pro Ort sowohl aktuelle als auch ältere Bewertungen, damit diese Trennung demonstriert werden kann.

## ADR-015: Review-Reaktionen sind accountgebunden und umschaltbar

**Status:** entschieden und umgesetzt

Likes und Dislikes werden als eigenständige `ReviewReaction` mit Nutzer-, Review-, Reaktions- und Zeitbezug gespeichert. Pro Nutzer und Rezension ist höchstens eine Reaktion zulässig.

- erstes Antippen legt die Reaktion an,
- erneutes Antippen entfernt sie,
- Antippen der Gegenseite wechselt zwischen Like und Dislike,
- eigene Rezensionen können nicht bewertet werden.

**Konsequenz:** Reaktionen bleiben nach App-Neustart erhalten, beeinflussen die Beliebt-Sortierung und fließen begrenzt in Aktivität und Reputation ein.

## ADR-016: Kein Registrierungs- und Profilerstellungsprozess im Local-first-MVP

**Status:** entschieden für den MVP

Der MVP verwendet einen vordefinierten, pseudonymisierten Demo-Nutzer, der innerhalb der App als bereits angemeldet behandelt wird. Registrierung, Login, Logout, Passwortverwaltung sowie das Erstellen oder Bearbeiten eines eigenen Profils werden nicht umgesetzt.

Die vorhandene Profilseite demonstriert die für die Produktidee relevanten Funktionen:

- dynamischen Nutzer-Score,
- Trennung von Aktivität und Reputation,
- private Bewertungs- und Rezensionshistorie,
- datenschutzreduzierte öffentliche Profile,
- Hilfe und vorbereitete Einstellungen.

**Begründung:** Der fachliche Kern besteht im Entdecken, Bewerten und Speichern öffentlicher Orte sowie in der zeitabhängigen Auswertung von Community-Feedback. Eine produktionsnahe Registrierung würde zusätzlich Authentifizierung, Kontosicherheit, Wiederherstellung, Einwilligungen, Profildatenverwaltung, Löschung, Synchronisation und ein Backend erfordern, ohne die Kernidee wesentlich besser zu demonstrieren.

**Konsequenz:** Nach dem Onboarding folgt direkt die Karte. Eine spätere Produktversion benötigt einen eigenen Account-Lifecycle und eine erneute Datenschutz- und Sicherheitsentscheidung.

## ADR-017: Onboarding-Abschluss wird lokal gespeichert und als Hilfe wiederverwendet

**Status:** entschieden und umgesetzt

Beim ersten Start erscheint ein vierseitiges Onboarding. Der Abschluss wird als einzelner boolescher Wert in app-internen `SharedPreferences` gespeichert. Spätere Starts öffnen direkt die Karte.

Derselbe Inhalt kann im eigenen Profil als Hilfe erneut geöffnet werden, ohne den Abschlussstatus zu verändern.

**Begründung:** Der Erststart erklärt Produktidee, Mock-Map, Kriterien, Bewertungsalterung und Standortbezug. Die Wiederverwendung vermeidet doppelte, auseinanderlaufende Hilfetexte.

**Konsequenz:** Öffentliche Profile erhalten keinen privaten Hilfe- oder Einstellungszugang. Ein Zurücksetzen der App-Daten stellt den reproduzierbaren Erststartzustand wieder her.
