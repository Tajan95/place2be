# Nutzer-Score und Gamification-Regeln

**Status:** Für den Local-first-MVP entschieden und in `CalculateUserScoreUseCase` umgesetzt.

Der öffentlich sichtbare Nutzer-Score ist ein nach oben offener Integer:

```text
totalScore = activityPoints + reputationPoints
```

Der Wert wird dynamisch aus den aktuell gespeicherten Bewertungen und Reaktionen berechnet. Punkte werden nicht dauerhaft hochgezählt. Entfernte Reaktionen, Datenänderungen und spätere Regelanpassungen bleiben dadurch konsistent.

## 1. Aktivitätspunkte für bewertete Vor-Ort-Besuche

Jede zulässige Ortsbewertung gibt mindestens einen Basispunkt. Ein „Besuch“ ist im MVP durch eine abgegebene Vor-Ort-Bewertung nachgewiesen.

```text
Erkundungsbonus = max(9 - Anzahl bisheriger Bewertungen dieses Ortes, 0)
Rohpunkte = 1 + Erkundungsbonus
```

| Bewerteter Besuch | Basis | Erkundungsbonus | Rohpunkte |
|---:|---:|---:|---:|
| 1. | 1 | 9 | 10 |
| 2. | 1 | 8 | 9 |
| 3. | 1 | 7 | 8 |
| ... | ... | ... | ... |
| 9. | 1 | 1 | 2 |
| ab 10. | 1 | 0 | 1 |

Damit lohnt sich das Erkunden neuer Orte besonders stark. Häufiges Bewerten eines leicht erreichbaren Ortes nähert sich dagegen schrittweise dem normalen Basispunkt an.

## 2. Nähe-Penalty innerhalb von 24 Stunden

Um das schnelle Abarbeiten dicht beieinanderliegender Orte zu begrenzen, werden zuvor bewertete Orte desselben Nutzers betrachtet, wenn sie:

- höchstens 24 Stunden zurückliegen,
- von einem anderen Ort stammen,
- höchstens 100 Meter vom aktuell bewerteten Ort entfernt liegen.

```text
Nähe-Penalty = min(Anzahl naher vorheriger Orte × 5, 10)
Bewertungspunkte = max(1, Rohpunkte - Nähe-Penalty)
```

Für drei erstmals bewertete Orte in demselben kleinen Cluster ergibt sich damit:

| Reihenfolge | Rohpunkte | Penalty | Ergebnis |
|---:|---:|---:|---:|
| erster Ort | 10 | 0 | 10 |
| zweiter Ort | 10 | 5 | 5 |
| dritter und weitere | 10 | 10 | mindestens 1 |

Legitime Bewertungen erzeugen niemals Negativpunkte.

## 3. Bonus für Textrezensionen

Eine Textrezension mit mindestens 20 Zeichen gibt zusätzlich:

```text
+2 activityPoints
```

Sehr kurze Eingaben erhalten keinen Textbonus. Die Textlänge wird nicht darüber hinaus belohnt; Qualität und Resonanz werden über Reputationspunkte abgebildet.

## 4. Aktivitätspunkte für Reaktionen auf fremde Rezensionen

Likes und Dislikes zählen gleichwertig als Beteiligung am Community-Prozess:

```text
+1 activityPoint pro aktuell bestehender Reaktion
```

Schutzmechanismen:

- höchstens drei belohnte Reaktionen pro Ort,
- höchstens sechs Reaktionspunkte pro Kalendertag,
- eigene Rezensionen sind nicht bewertbar,
- ein Wechsel zwischen Like und Dislike erzeugt keinen neuen Aktivitätszeitpunkt,
- das Entfernen einer Reaktion entfernt bei der dynamischen Neuberechnung auch ihren Aktivitätspunkt.

Weitere Reaktionen bleiben funktional und beeinflussen Autor sowie Popularitätssortierung, erhöhen aber nicht mehr den Aktivitätswert.

## 5. Reputation durch Reaktionen auf eigene Textrezensionen

Nur Textrezensionen können Reputationspunkte erzeugen. Pro Rezension gilt zunächst:

```text
Netto-Reaktionen = max(Likes - Dislikes, 0)
Grundwert = 3 × ln(1 + Netto-Reaktionen)
```

Der Grenznutzen zusätzlicher Likes nimmt logarithmisch ab. Dislikes können den Gewinn reduzieren, aber keine negativen Reputationspunkte erzeugen.

### Normalisierung nach Ortsaktivität

Die Aktivität eines Ortes wird über die Anzahl seiner Bewertungen der vergangenen 365 Tage angenähert:

```text
Ortsfaktor = clamp(
    sqrt(20 / Bewertungen_des_Ortes_im_letzten_Jahr),
    0,75,
    1,25
)
```

Damit erhalten Rezensionen kleinerer Orte einen moderaten Ausgleich, während extrem stark besuchte Orte leicht abgeschwächt werden.

```text
Reputationspunkte der Rezension =
min(10, gerundet(Grundwert × Ortsfaktor))
```

Eine einzelne Rezension kann maximal zehn Reputationspunkte erzeugen. Dadurch führt auch eine sehr populäre Rezension an einem stark frequentierten Ort nicht zu einem unverhältnismäßigen Score-Sprung.

## 6. Technische Konsequenzen

- `UserScoreResult` trennt Aktivität, Reputation und die Aktivitätsbestandteile.
- `CalculateUserScoreUseCase` liegt in der Domain-Schicht und ist unabhängig von Compose testbar.
- Die vorhandene Eigenschaft `User.userScore` ist im MVP nur ein Seed-/Kompatibilitätsfeld; maßgeblich ist die dynamische Berechnung.
- Der Dashboard-Header zeigt den aktuellen `totalScore` vorläufig als kleine Score-Anzeige.
- Issue #15 übernimmt später die ausführliche Profilansicht und kann Aktivität und Reputation getrennt erklären.

## Präsentations-Kurzform

> Neue Orte erkunden, regelmäßig beitragen und hilfreiche Meinungen verfassen lohnt sich. Wiederholtes Bewerten desselben Ortes, das Abarbeiten eines engen Ortsclusters und massenhaftes Reagieren werden dagegen gedeckelt.
