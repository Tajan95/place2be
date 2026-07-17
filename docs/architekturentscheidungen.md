# Architekturentscheidungen

Dieses Dokument hält die vorläufigen technischen und fachlichen Entscheidungen für den place2be-MVP fest. Die Entscheidungen sind bewusst pragmatisch formuliert und können durch das Team angepasst werden.

## ADR-001: Kotlin als Programmiersprache

**Status:** entschieden

place2be wird in Kotlin entwickelt. Kotlin ist für moderne Android-Entwicklung gut geeignet, reduziert Boilerplate-Code gegenüber Java und passt zur aktuellen Android-Studio- und Jetpack-Compose-Entwicklung.

**Konsequenz:** Neue App-Logik wird in Kotlin geschrieben.

## ADR-002: Jetpack Compose statt XML-Layouts

**Status:** entschieden

Die Benutzeroberfläche wird mit Jetpack Compose umgesetzt. Dadurch kann UI deklarativ in Kotlin beschrieben werden. Klassische XML-Layouts werden im MVP vermieden.

**Konsequenz:** Screens werden als Composable-Funktionen umgesetzt. UI-Komponenten sollen möglichst wiederverwendbar bleiben.

## ADR-003: Material 3 als UI-Grundlage

**Status:** entschieden

Für das visuelle Design wird Material 3 verwendet. Das reduziert Designaufwand und sorgt für ein konsistentes Android-typisches Erscheinungsbild.

**Konsequenz:** Buttons, Cards, Bottom-Sheets, Listen, Chips, Scaffold-Strukturen und Farbschema orientieren sich an Material 3.

## ADR-004: Local-first-MVP statt sofortigem Backend

**Status:** vorläufige Empfehlung

Der MVP wird lokal mit JSON-Mock-Daten umgesetzt. Orte, Bewertungen, Textrezensionen, Bookmarks und Nutzer werden als Seed-Dateien ausgeliefert und beim ersten Start in den internen App-Speicher kopiert. Eine zentrale Mock-Datenquelle kapselt dort sämtliche CRUD-Operationen. Die Architektur bleibt so getrennt, dass später eine Backend-, Firebase- oder REST-Anbindung möglich ist.

**Begründung:** Ein echtes Backend mit Nutzerkonten, zentraler Datenbank, Manipulationsschutz und Synchronisation erhöht den Aufwand stark. Für den aktuellen Projektumfang ist eine stabile, erklärbare und demo-fähige App wichtiger.

**Konsequenz:** Die UI greift über Repository-Interfaces auf eine gemeinsame `MockPlaceDataSource` zu. Assets werden nie direkt verändert; Schreiboperationen betreffen ausschließlich die internen Arbeitskopien der JSON-Dateien. Eine spätere Remote-Datenquelle kann hinter denselben Interfaces ergänzt werden.

## ADR-005: Standortbestätigung zunächst simuliert oder vereinfacht

**Status:** vorläufige Empfehlung

Die App-Idee sieht vor, dass Nutzerinnen und Nutzer per Standort bestätigen, dass sie tatsächlich vor Ort sind. Für den MVP kann diese Prüfung vereinfacht oder simuliert werden. Der Bewertungsbereich bleibt sichtbar, ist aber deaktiviert bzw. ausgegraut, wenn die Vor-Ort-Bestätigung nicht erfüllt ist. Für eine spätere echte Prüfung ist zusätzlich eine Mindestaufenthaltsdauer vorgesehen, damit kurze Vorbeifahrten nicht als belastbarer Besuch gelten.

**Begründung:** Echte GPS-Prüfung bringt zusätzliche Komplexität durch Permissions, Datenschutz, Emulator-Setup, ungenaue Standortdaten und Edge Cases.

**Konsequenz:** Die Architektur ermöglicht eine spätere echte Standortprüfung und Mindestaufenthaltsdauer. Auf dem Feature-Branch für Issue #6 wird `SIMULATED_CONFIRMED` ausschließlich zur IDE- und MVP-Demonstration gesetzt; dies ist kein Produktionszustand und kein Defekt der Vor-Ort-Regel.

## ADR-006: Bewertungsalter als Kernbestandteil des Ranking-Systems

**Status:** entschieden und im MVP umgesetzt

Bewertungen sollen nicht dauerhaft gleich stark zählen. Ältere Bewertungen verlieren mit der Zeit an Gewicht, damit Orte nicht langfristig durch veraltete positive oder negative Eindrücke geprägt bleiben. Die Zeitgewichtung wird für **Vibes**, **Sicherheit** und **Erreichbarkeit** jeweils separat berechnet. Der Gesamtwert ist anschließend der Mittelwert dieser drei gewichteten Einzelwerte.

Die im MVP verwendete Gewichtungsformel lautet:

```text
Gewicht = 1 / (1 + Alter_in_Tagen × 0,05)
```

Eine neue Bewertung besitzt damit das Gewicht `1,0`. Eine 30 Tage alte Bewertung besitzt noch das Gewicht `0,4`. Alte Bewertungen verschwinden nicht vollständig, beeinflussen das aktuelle Bild eines Ortes aber zunehmend schwächer.

**Begründung:** Öffentliche Orte können sich verändern. Eine gute oder schlechte Bewertung von vor Monaten sollte weniger relevant sein als aktuelles Feedback. Gleichzeitig bleiben ältere Erfahrungen als abgeschwächte Langzeitinformation erhalten.

**Konsequenz:** `CalculatePlaceScoreUseCase` liefert ein gemeinsames `PlaceScoreResult` mit Gesamtwert, den drei gewichteten Kriterien, der gesamten Bewertungsanzahl und der Bewertungsanzahl im rollierenden Ein-Jahres-Fenster. Kartenmarker, Preview und Detailansicht verwenden ausschließlich dieses Ergebnisobjekt. Die Orts-Score-Zerfallslogik bleibt klar von der Popularitäts-Penalty sichtbarer Textrezensionen aus ADR-011 bzw. Issue #19 getrennt.

## ADR-007: MVVM- und Feature-orientierte Struktur

**Status:** entschieden

Jeder größere Screen wird als Feature betrachtet. Pro Feature gibt es eine `Screen`-Datei für Compose-UI und eine `ViewModel`-Datei für UI-Zustand und Screen-Logik. Fachliche Kernlogik liegt nicht in der UI, sondern in `domain/usecase`.

**Konsequenz:** Score-Berechnung, Reputationslogik und weitere fachliche Regeln bleiben testbar und präsentationsfreundlich erklärbar.

## ADR-008: Mock-Map und zusammenhängendes Bottom-Sheet statt echter Kartenintegration

**Status:** entschieden für MVP

Der MVP verwendet eine Mock-Map. Ohne ausgewählten Ort zeigt die Map Default-Shortcuts. Nach Auswahl eines Ortes öffnet sich eine Mini-Preview bzw. Schnellübersichtsleiste, die per Swipe zur Detailansicht erweitert wird. Map, Mini-Preview, Detailinformationen und Bewertung bilden bewusst einen räumlich zusammenhängenden Flow.

Die Bewertung ist im regulären App-Flow **keine separate Vollbildseite**. Im kompakten Peek-Zustand werden Ortsname, Kurzinfo und aggregierte Werte angezeigt. Erst nach dem Hochziehen erscheinen die Schnellbewertung über die drei Slider, ein optional aufklappbares Textrezensionsfeld sowie darunter die bestehenden Rezensionen.

**Begründung:** Die App soll die Produktidee klar zeigen, ohne API-Keys, Karten-SDKs oder Live-Geodaten integrieren zu müssen. Der zusammenhängende Bottom-Sheet-Flow erhält außerdem die visuelle Kontinuität zwischen Kartenauswahl, Detailansicht und Bewertung und verhindert konkurrierende Buttons mit ähnlicher Bedeutung.

**Konsequenz:** Echte Kartenintegration bleibt Ausblick. Die Mock-Map muss aber strukturell so gebaut sein, dass eine echte Karte später angebunden werden kann. Das Bottom-Sheet trägt die Verantwortung für Peek-, Detail-, Bewertungs- und Review-Zustand. Wenn die Detailansicht wieder auf den Peek-Zustand verkleinert wird, springt ihr interner Scrollzustand auf den Anfang zurück, damit erneut die kompakte Ortsvorschau sichtbar ist.

## ADR-009: Profil, Nutzer-Score und Review-Reaktionen als MVP-nahe Bestandteile

**Status:** entschieden; Profil-UI noch umzusetzen

Profilseite, Nutzer-Score, Textrezensionen, Likes/Dislikes und Bookmarks sind nach den Mockup-Notizen MVP-relevant oder mindestens MVP-nah vorzubereiten. Der Nutzer-Score bildet Aktivität und Reputation getrennt ab und wird dynamisch aus dem aktuellen lokalen Datenstand berechnet. Die Regeln zur Normalisierung und Begrenzung gegen Score-Farming sind in `docs/nutzer-score-regeln.md` dokumentiert.

Die öffentliche Sichtbarkeit der Bewertungs-Historie ist in ADR-010 verbindlich festgelegt.

**Konsequenz:** Issue #15 setzt die Profiloberfläche auf Basis der bereits vorhandenen Score- und Repository-Strukturen um. Eigene und fremde Profilansichten müssen dabei fachlich unterscheidbar bleiben.

## ADR-010: Datenschutzbewusste Trennung zwischen eigenem und öffentlichem Profil

**Status:** entschieden für den Local-first-MVP

Die vollständige Bewertungs- und Rezensionshistorie ist ausschließlich im **eigenen Profil** sichtbar. Dort darf der angemeldete Nutzer seine eigenen Einträge mit Ort, Datum, numerischen Kriterien und vorhandenem Rezensionstext nachvollziehen. Diese Ansicht dient als private persönliche Historie und wird nicht als öffentliches Aktivitätsprotokoll angeboten.

Ein **fremdes öffentliches Profil** zeigt ausschließlich aggregierte Informationen:

- Pseudonym und neutrales Profil-Icon,
- Gesamt-Score,
- Aktivitäts- und Reputationspunkte,
- Anzahl abgegebener Bewertungen bzw. Textrezensionen,
- aggregierte Anzahl hilfreicher positiver Reaktionen.

Eine chronologische Liste besuchter oder bewerteter Orte wird fremden Nutzern nicht angezeigt. Einzelne Rezensionen bleiben weiterhin beim jeweils bewerteten Ort sichtbar, werden aber im öffentlichen Profil nicht zu einer nutzerbezogenen Orts- oder Bewegungschronologie zusammengeführt.

Einstellungen, Hilfe und die vollständige eigene Historie sind nur im eigenen Profil erreichbar. Die UI soll deshalb zwischen `OWN` und `PUBLIC` beziehungsweise einer vergleichbaren Profilansicht unterscheiden können, auch wenn der Local-first-MVP zunächst nur den Demo-Nutzer vollständig interaktiv zeigt.

**Datenschutz- und Sicherheitsbegründung:** Eine öffentliche vollständige Historie könnte durch Zeitstempel, wiederkehrende Orte und Bewertungsmuster Rückschlüsse auf Wohnort, Routinen, Aufenthaltszeiten, soziale Gewohnheiten oder die Identität einer Person ermöglichen. Die Trennung folgt dem Prinzip der Datenminimierung: Öffentlich werden nur die für Reputation und Community-Vertrauen erforderlichen aggregierten Werte dargestellt.

**Konsequenz:** Issue #15 implementiert die private eigene Profilansicht und bereitet eine begrenzte öffentliche Variante strukturell vor. Eine spätere Backend- oder Kontenlösung darf die öffentliche Historie nicht allein deshalb erweitern, weil mehr Daten technisch verfügbar sind; für zusätzliche Sichtbarkeit wäre eine neue bewusste Datenschutzentscheidung erforderlich.

## ADR-011: Gespeicherte Rezensionen werden direkt in der Detailansicht sichtbar

**Status:** entschieden für MVP

Nach dem Speichern einer Bewertung soll die Repository-Schicht erneut gelesen werden. Eine eingegebene Textrezension erscheint dadurch unmittelbar im Review-Bereich desselben Ortes. Der Bereich liegt unterhalb des Bewertungsformulars im erweiterten Bottom-Sheet.

Mindestens die Sortierungen **Rezent** und **Beliebt** werden sichtbar angeboten. „Rezent“ sortiert absteigend nach Zeitstempel. „Beliebt“ verwendet vorläufig die Netto-Reaktionen `likes - dislikes` und eine logarithmisch wachsende Altersstrafe. Die Strafe ist anfangs deutlich und flacht mit zunehmendem Alter ab. So können aktuelle populäre Meinungen aufsteigen, ohne dass ältere hilfreiche Rezensionen zwangsläufig vollständig bedeutungslos werden.

**Konsequenz:** Issue #6 verantwortet Eingabe, Speicherung und unmittelbar sichtbare Rückmeldung. Ausklappbare Review-Texte, Reaktionsinteraktionen und weiterführende Filter bleiben mit den Issues #18 und #19 abgestimmt.

## ADR-012: Eine Bewertung pro Nutzer, Ort und 24-Stunden-Zeitraum

**Status:** entschieden für MVP

Nach einer abgegebenen Bewertung darf derselbe Nutzer denselben Ort erst 24 Stunden später erneut bewerten. Die Sperre gilt sowohl für reine Zahlenbewertungen als auch für Bewertungen mit Textrezension.

**Begründung:** Ohne Sperrfrist könnten einzelne Nutzer den Orts-Score durch viele unmittelbar aufeinanderfolgende Bewertungen manipulieren. Die Regel begrenzt Spam, ohne regelmäßige neue Eindrücke grundsätzlich auszuschließen.

**Konsequenz:** Die Regel liegt als `ReviewSubmissionCooldownPolicy` in der Domain-Schicht. Während der Sperrfrist werden Slider und Textfeld deaktiviert. Der Speichern-Button ist nicht klickbar, zeigt die verbleibende Zeit an und verwendet zur freundlichen Kennzeichnung einen Curry-/Goldton.

## ADR-013: Textrezensionslisten und gespeicherte Texte sind auf 50 Einträge begrenzt

**Status:** entschieden und für den Local-first-MVP persistent umgesetzt

Sowohl „Rezent“ als auch „Beliebt“ zeigen pro Ort höchstens 50 Textrezensionen. Dadurch bleibt die Detailansicht bei stark frequentierten Orten performant und übersichtlich.

Die lokale Datenhaltung bewahrt pro Ort ausschließlich die 50 neuesten nichtleeren Rezensionstexte auf. Sobald eine neuere Textrezension die Grenze überschreitet, wird beim ältesten verdrängten Eintrag ausschließlich `text` auf `null` gesetzt. UUID, Ort, Nutzerzuordnung, Vibes, Sicherheit, Erreichbarkeit, Zeitstempel sowie Like- und Dislike-Zähler bleiben unverändert gespeichert. Reine Zahlenbewertungen werden von der Textgrenze nicht berührt.

Die Sortierung „Beliebt“ arbeitet innerhalb der noch gespeicherten Textrezensionen. Bei identischen Zeitstempeln wird deterministisch der später gespeicherte Eintrag bevorzugt. Bereits vorhandene lokale Datenbestände mit mehr als 50 Texten pro Ort werden beim Initialisieren der `MockPlaceDataSource` nach derselben Regel bereinigt.

**Konsequenz:** `MockPlaceDataSource` erzwingt die Aufbewahrungsgrenze beim Erstellen und Aktualisieren von Rezensionen sowie beim App-Start. Die verdrängten numerischen Bewertungsdaten bleiben vollständig erhalten und fließen weiterhin in `CalculatePlaceScoreUseCase`, Jahreszähler, Nutzer-Score und private Bewertungshistorie ein; lediglich der nicht mehr aufbewahrte Freitext entfällt.

## ADR-014: Bewertungsanzahl als rollierender Ein-Jahres-Ausschnitt

**Status:** entschieden für MVP

Die Detailansicht zeigt unterhalb der aggregierten Kriterien den Hinweis `Im letzten Jahr insgesamt X Bewertungen`. Gezählt werden ausschließlich Bewertungen mit einem Zeitstempel innerhalb der vergangenen 365 Tage. Ältere Bewertungen bleiben weiterhin in der zeitlich gewichteten Score-Berechnung erhalten, werden aber nicht mehr im sichtbaren Aktivitätszähler geführt.

**Begründung:** Die gesamte historische Bewertungsanzahl kann bei lange bestehenden Orten einen falschen Eindruck aktueller Community-Aktivität vermitteln. Der rollierende Jahreswert ist als Referenzpunkt für die Belastbarkeit und Aktualität des sichtbaren Scores aussagekräftiger.

**Konsequenz:** Eine neu abgegebene Bewertung erhöht den Zähler unmittelbar um eins. Orte ohne Bewertungen werden in der Detailansicht als `Noch nicht bewertet.` gekennzeichnet. Für die MVP-Demo enthalten alle fünf Seed-Orte sechs Bewertungen von unterschiedlichen Mock-Nutzern; fünf liegen innerhalb des letzten Jahres und eine ältere Bewertung demonstriert, dass historische Zahlen weiterhin abgeschwächt in den Score einfließen, ohne im Jahreszähler zu erscheinen. Der Demo-Nutzer selbst besitzt keine Seed-Bewertung, damit das Hinzufügen und Hochzählen in der Präsentation nachvollziehbar getestet werden kann.

## ADR-015: Review-Reaktionen sind accountgebunden und umschaltbar

**Status:** entschieden und für den Local-first-MVP umgesetzt

Likes und Dislikes werden als eigenständige `ReviewReaction` mit Nutzer-, Review-, Reaktions- und Zeitbezug gespeichert. Pro Nutzer und Rezension ist höchstens eine Reaktion zulässig. Das erste Antippen legt eine Reaktion an, erneutes Antippen derselben Reaktion entfernt sie, und das Antippen der jeweils anderen Reaktion wechselt zwischen Like und Dislike. Eigene Rezensionen können nicht bewertet werden.

Die vorhandenen Like-/Dislike-Werte der Seed-Reviews repräsentieren einen bereits bestehenden Community-Stand. Neue Reaktionen des Demo-Nutzers werden zusätzlich in `review_reactions.json` persistiert und verändern die aggregierten Zähler der betroffenen Rezension unmittelbar.

**Begründung:** Eine reine Änderung an zwei Zählerfeldern würde Mehrfach-Reaktionen desselben Accounts erlauben und könnte später nicht sauber für Reputation oder Moderation ausgewertet werden. Der explizite Reaktionsdatensatz schafft die notwendige Nachvollziehbarkeit und verhindert einen einfachen Score-Maximierungsweg über eigene Rezensionen.

**Konsequenz:** Der aktuelle Reaktionszustand wird in der UI hervorgehoben, bleibt nach einem App-Neustart erhalten und beeinflusst unmittelbar die Sortierung `Beliebt`. Die Reaktionsdaten fließen außerdem in die mit Issue #16 umgesetzte dynamische Berechnung von Aktivitäts- und Reputationspunkten ein.
