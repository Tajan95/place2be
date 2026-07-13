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

Die App-Idee sieht vor, dass Nutzerinnen und Nutzer per Standort bestätigen, dass sie tatsächlich vor Ort sind. Für den MVP kann diese Prüfung vereinfacht oder simuliert werden. Der Bewertungsbutton bleibt sichtbar, ist aber deaktiviert bzw. ausgegraut, wenn die Vor-Ort-Bestätigung nicht erfüllt ist.

**Begründung:** Echte GPS-Prüfung bringt zusätzliche Komplexität durch Permissions, Datenschutz, Emulator-Setup, ungenaue Standortdaten und Edge Cases.

**Konsequenz:** Die Architektur sollte eine spätere echte Standortprüfung ermöglichen, aber die Live-Demo nicht davon abhängig machen.

## ADR-006: Bewertungsalter als Kernbestandteil des Ranking-Systems

**Status:** entschieden

Bewertungen sollen nicht dauerhaft gleich stark zählen. Ältere Bewertungen verlieren mit der Zeit an Gewicht, damit Orte nicht langfristig durch veraltete positive oder negative Eindrücke geprägt bleiben.

**Begründung:** Öffentliche Orte können sich verändern. Eine gute Bewertung von vor Monaten sollte weniger relevant sein als aktuelles Feedback.

**Konsequenz:** Die Score-Berechnung wird als eigene Logik gekapselt und muss aktuelle Bewertungen stärker gewichten als alte.

## ADR-007: MVVM- und Feature-orientierte Struktur

**Status:** entschieden

Jeder größere Screen wird als Feature betrachtet. Pro Feature gibt es eine `Screen`-Datei für Compose-UI und eine `ViewModel`-Datei für UI-Zustand und Screen-Logik. Fachliche Kernlogik liegt nicht in der UI, sondern in `domain/usecase`.

**Konsequenz:** Score-Berechnung, Reputationslogik und weitere fachliche Regeln bleiben testbar und präsentationsfreundlich erklärbar.

## ADR-008: Mock-Map und Bottom-Sheet statt echter Kartenintegration

**Status:** entschieden für MVP

Der MVP verwendet eine Mock-Map. Ohne ausgewählten Ort zeigt die Map Default-Shortcuts. Nach Auswahl eines Ortes öffnet sich eine Mini-Preview bzw. Schnellübersichtsleiste, die später per Swipe zur Detailansicht erweitert werden kann.

**Begründung:** Die App soll die Produktidee klar zeigen, ohne API-Keys, Karten-SDKs oder Live-Geodaten integrieren zu müssen.

**Konsequenz:** Echte Kartenintegration bleibt Ausblick. Die Mock-Map muss aber strukturell so gebaut sein, dass eine echte Karte später angebunden werden kann.

## ADR-009: Profil, Nutzer-Score und Review-Reaktionen als MVP-nahe Bestandteile

**Status:** entschieden / teilweise noch zu konkretisieren

Profilseite, Nutzer-Score, Textrezensionen, Likes/Dislikes und Bookmarks sind nach den Mockup-Notizen MVP-relevant oder mindestens MVP-nah vorzubereiten. Der Nutzer-Score soll Aktivität und Reputation abbilden.

**Offen:** Die genaue Normalisierung des Nutzer-Scores und die öffentliche Sichtbarkeit der Bewertungs-Historie müssen noch entschieden werden.

**Konsequenz:** Diese Konzepte werden in Issues und Dokumentation aufgenommen, können aber bei Zeitmangel zunächst mit Mock-Daten oder vereinfachter Logik demonstriert werden.

## ADR-010: Datenschutzbewusste Profilansicht

**Status:** offen / zu entscheiden

Die eigene Bewertungs-Historie kann für den Nutzer selbst hilfreich sein. Eine vollständig öffentliche Historie könnte aber Rückschlüsse auf Identität, Bewegungsmuster oder häufig besuchte Orte erlauben.

**Konsequenz:** Es muss entschieden werden, ob fremde Nutzer nur aggregierte Profilinformationen sehen, während die vollständige Historie privat bleibt.
