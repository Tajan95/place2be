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

**Status:** entschieden / teilweise noch zu konkretisieren

Profilseite, Nutzer-Score, Textrezensionen, Likes/Dislikes und Bookmarks sind nach den Mockup-Notizen MVP-relevant oder mindestens MVP-nah vorzubereiten. Der Nutzer-Score soll Aktivität und Reputation abbilden.

**Offen:** Die genaue Normalisierung des Nutzer-Scores und die öffentliche Sichtbarkeit der Bewertungs-Historie müssen noch entschieden werden.

**Konsequenz:** Diese Konzepte werden in Issues und Dokumentation aufgenommen, können aber bei Zeitmangel zunächst mit Mock-Daten oder vereinfachter Logik demonstriert werden.

## ADR-010: Datenschutzbewusste Profilansicht

**Status:** offen / zu entscheiden

Die eigene Bewertungs-Historie kann für den Nutzer selbst hilfreich sein. Eine vollständig öffentliche Historie könnte aber Rückschlüsse auf Identität, Bewegungsmuster oder häufig besuchte Orte erlauben.

**Konsequenz:** Es muss entschieden werden, ob fremde Nutzer nur aggregierte Profilinformationen sehen, während die vollständige Historie privat bleibt.

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

## ADR-013: Textrezensionslisten sind auf 50 Einträge begrenzt

**Status:** Anzeige entschieden, persistente Bereinigung noch umzusetzen

Sowohl „Rezent“ als auch „Beliebt“ zeigen pro Ort höchstens 50 Textrezensionen. Dadurch bleibt die Detailansicht bei stark frequentierten Orten performant und übersichtlich.

Für „Rezent“ werden die 50 neuesten Texte angezeigt. Für „Beliebt“ werden die 50 höchsten zeitabhängigen Popularitätswerte angezeigt. Sobald die persistente Bereinigung umgesetzt wird, darf bei verdrängten Einträgen ausschließlich der Rezensionstext entfernt werden. Die numerischen Werte für Vibes, Sicherheit und Erreichbarkeit sowie der Zeitstempel bleiben erhalten und fließen weiterhin in den zeitlich gewichteten Orts-Score ein.

**Konsequenz:** Die UI begrenzt die sichtbaren Listen bereits auf 50 Einträge. Das tatsächliche Entfernen verdrängter Texte aus der lokalen Datenhaltung wird in Issue #18 separat umgesetzt, damit keine numerischen Bewertungsdaten verloren gehen.

## ADR-014: Bewertungsanzahl als rollierender Ein-Jahres-Ausschnitt

**Status:** entschieden für MVP

Die Detailansicht zeigt unterhalb der aggregierten Kriterien den Hinweis `Im letzten Jahr insgesamt X Bewertungen`. Gezählt werden ausschließlich Bewertungen mit einem Zeitstempel innerhalb der vergangenen 365 Tage. Ältere Bewertungen bleiben weiterhin in der zeitlich gewichteten Score-Berechnung erhalten, werden aber nicht mehr im sichtbaren Aktivitätszähler geführt.

**Begründung:** Die gesamte historische Bewertungsanzahl kann bei lange bestehenden Orten einen falschen Eindruck aktueller Community-Aktivität vermitteln. Der rollierende Jahreswert ist als Referenzpunkt für die Belastbarkeit und Aktualität des sichtbaren Scores aussagekräftiger.

**Konsequenz:** Eine neu abgegebene Bewertung erhöht den Zähler unmittelbar um eins. Orte ohne Bewertungen werden in der Detailansicht als `Noch nicht bewertet.` gekennzeichnet. Für die MVP-Demo enthalten alle fünf Seed-Orte sechs Bewertungen von unterschiedlichen Mock-Nutzern; fünf liegen innerhalb des letzten Jahres und eine ältere Bewertung demonstriert, dass historische Zahlen weiterhin abgeschwächt in den Score einfließen, ohne im Jahreszähler zu erscheinen. Der Demo-Nutzer selbst besitzt keine Seed-Bewertung, damit das Hinzufügen und Hochzählen in der Präsentation nachvollziehbar getestet werden kann.
