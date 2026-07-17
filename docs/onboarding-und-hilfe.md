# Onboarding und erneut aufrufbare Hilfe

## Ziel

Beim ersten Start erklaert place2be in vier kurzen Schritten die Produktidee und die wichtigsten Regeln. Nach dem Abschluss startet die App bei spaeteren Aufrufen direkt auf der Mock-Map. Derselbe Erklaerfluss kann im eigenen Profil jederzeit erneut als Hilfe geoeffnet werden.

## Inhalte

1. Zweck der App: oeffentliche, niedrigschwellige Aufenthaltsorte entdecken.
2. Mock-Map: Marker, Kurzlisten und das erweiterbare Orts-Bottom-Sheet.
3. Bewertung: Vibes, Sicherheit und Erreichbarkeit sowie optionale Textrezensionen.
4. Aktualitaet und Standort: neuere Bewertungen zaehlen staerker; die Vor-Ort-Bestaetigung ist im MVP simuliert und fuer spaetere Standort-APIs vorbereitet.

## Persistenz

`SharedPreferencesOnboardingCompletionStore` speichert ausschliesslich den booleschen Abschlussstatus in app-internen SharedPreferences. Es werden dabei keine Profil-, Standort- oder Bewertungsdaten abgelegt.

- fehlender Status: Onboarding erscheint beim ersten App-Start,
- abgeschlossener Status: App startet direkt auf der Karte,
- das erneute Oeffnen im Hilfe-Modus veraendert den Abschlussstatus nicht.

## Navigation

- Beim ersten Start fuehrt die letzte Seite mit `Karte oeffnen` zur Mock-Map.
- Im Hilfe-Modus kann der Erklaerfluss ueber `Schliessen`, die Android-Zuruecknavigation oder `Zurueck zum Profil` beendet werden.
- Innerhalb des Erklaerflusses springt die Android-Zuruecknavigation zunaechst zur vorherigen Seite.
- Der ausfuehrliche Hilfezugang ist nur im eigenen Profil sichtbar. Oeffentliche Community-Profile erhalten weder Hilfe noch Einstellungen.
- Map, ausgewaehlter Ort und Profilkontext bleiben unter der Vollbildschicht komponiert.

## Demo und Test

Fuer einen erneuten echten Erststart kann der lokale App-Speicher des Emulators geloescht werden:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell pm clear de.place2be
```

Danach muss das Onboarding erneut erscheinen. Nach vollstaendigem Abschluss und App-Neustart muss die App direkt auf der Karte starten. Der runde `?`-Button oben rechts im eigenen Profil oeffnet den vollstaendigen Erklaerfluss erneut.
