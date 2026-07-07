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

**Konsequenz:** Buttons, Cards, Listen, Scaffold-Strukturen und Farbschema orientieren sich an Material 3.

## ADR-004: Local-first-MVP statt sofortigem Backend

**Status:** vorläufige Empfehlung

Der MVP wird zunächst lokal bzw. prototypisch umgesetzt. Orte und Bewertungen können über Mock-Daten, In-Memory-Repositories oder später eine lokale Persistenz verwaltet werden. Die Architektur soll jedoch so getrennt werden, dass eine spätere Backend-, Firebase- oder REST-Anbindung möglich bleibt.

**Begründung:** Ein echtes Backend mit Nutzerkonten, zentraler Datenbank, Manipulationsschutz und Synchronisation erhöht den Aufwand stark. Für den aktuellen Projektumfang ist eine stabile, erklärbare und demo-fähige App wichtiger.

**Konsequenz:** Die UI greift nicht direkt auf hart codierte Daten zu, sondern über eine Repository-Schicht. Eine spätere Remote-Datenquelle kann dort ergänzt werden.

## ADR-005: Standortbestätigung zunächst simuliert oder vereinfacht

**Status:** vorläufige Empfehlung

Die App-Idee sieht vor, dass Nutzerinnen und Nutzer per Standort bestätigen, dass sie tatsächlich vor Ort sind. Für den MVP kann diese Prüfung vereinfacht oder simuliert werden, etwa durch einen Button „Ich bin vor Ort“ oder eine Demo-Logik.

**Begründung:** Echte GPS-Prüfung bringt zusätzliche Komplexität durch Permissions, Datenschutz, Emulator-Setup, ungenaue Standortdaten und Edge Cases.

**Konsequenz:** Die Architektur sollte eine spätere echte Standortprüfung ermöglichen, aber die Live-Demo nicht davon abhängig machen.

## ADR-006: Zeitverfall als Kernbestandteil des Ranking-Systems

**Status:** entschieden

Bewertungen sollen nicht dauerhaft gleich stark zählen. Ältere Bewertungen verlieren mit der Zeit an Gewicht, damit Orte nicht langfristig durch veraltete positive oder negative Eindrücke geprägt bleiben.

**Begründung:** Öffentliche Orte können sich verändern. Eine gute Bewertung von vor Monaten sollte weniger relevant sein als aktuelles Feedback.

**Konsequenz:** Die Score-Berechnung wird als eigene Logik gekapselt und muss aktuelle Bewertungen stärker gewichten als alte.

## ADR-007: MVP vor Feature-Fülle

**Status:** entschieden

Der MVP konzentriert sich auf Orteliste, Detailansicht, Bewertung und dynamischen Score. Gamification, Nutzerkonten, neue Orte vorschlagen, echte Karte und Backend-Anbindung bleiben zunächst Erweiterungen.

**Konsequenz:** Neue Features werden nur aufgenommen, wenn der MVP stabil funktioniert und die Live-Demo nicht gefährdet wird.
