# Record API Test Client

Ein interaktiver Test-Client für die Record REST API, entwickelt in Kotlin mit Ktor Client.

## Übersicht

Dieser Test-Client ermöglicht es, alle CRUD-Operationen der Record API zu testen. Der Client bietet eine benutzerfreundliche Kommandozeilen-Schnittstelle zum Erstellen, Lesen, Aktualisieren und Löschen von Schallplatten-Datensätzen.

## Features

- **Vollständige CRUD-Operationen**
  - ✅ Records erstellen (POST)
  - ✅ Alle Records anzeigen (GET)
  - ✅ Record nach ID suchen (GET)
  - ✅ Record aktualisieren (PUT)
  - ✅ Record löschen (DELETE)

- **Testdaten-Management**
  - Vordefinierte Testdaten einfügen
  - CSV-Dateien importieren
  - Batch-Upload mit Fortschrittsanzeige

- **Benutzerfreundlichkeit**
  - Interaktives Menü
  - Eingabevalidierung
  - Detaillierte Fehlermeldungen
  - Status-Code-Behandlung

## Datenmodell

```kotlin
data class Album(
    val owner: String,    // Besitzer des Records
    val title: String,    // Titel des Albums
    val artist: String,   // Künstler
    val year: Int        // Erscheinungsjahr
)
```

## Voraussetzungen

- Kotlin 1.8+
- JVM 11+
- Laufende Record REST API (standardmäßig auf `http://127.0.0.1:8100`)

## Installation & Setup

1. **Projekt klonen/herunterladen**
2. **Dependencies installieren** (automatisch über Gradle/Maven)
3. **API-URL konfigurieren** (optional):
   ```kotlin
   const val BASE_URL = "http://127.0.0.1:8100"  // In der Datei anpassen
   ```

## Verwendung

### Programm starten
```bash
kotlin MainKt
# oder
./gradlew run
```

### Hauptmenü
```
=== Record API Test Client ===
1. Record erstellen (POST)
2. Alle Records anzeigen (GET)
3. Record nach ID suchen (GET)
4. Record aktualisieren (PUT)
5. Record löschen (DELETE)
6. Datenbank mit Testdaten füllen
7. Testdaten aus CSV-Datei laden
8. Beenden
```

### Beispiel-Workflow

#### 1. Record erstellen
```
Owner: Max
Titel: Dark Side of the Moon
Artist: Pink Floyd
Jahr: 1973
```

#### 2. Record aktualisieren
- Benötigt die Record-ID
- Owner muss mit ursprünglichem Owner übereinstimmen
- Alle Felder werden aktualisiert

#### 3. Record löschen
- Benötigt Record-ID und Owner zur Authentifizierung
- Nur der Owner kann seinen Record löschen

## CSV-Import

### CSV-Format
Erstellen Sie eine CSV-Datei im `src/main/resources/`-Verzeichnis:

```csv
owner,title,artist,year
"Max","The Dark Side of the Moon","Pink Floyd",1973
"Anna","Abbey Road","The Beatles",1969
"Peter","Rumours","Fleetwood Mac",1977
```

### CSV-Regeln
- Erste Zeile kann Header enthalten (wird automatisch erkannt)
- Anführungszeichen um Werte sind optional
- Mindestens 4 Spalten erforderlich: `owner,title,artist,year`
- Ungültige Zeilen werden übersprungen mit Warnung

## Vordefinierte Testdaten

Der Client enthält 15 vordefinierte Album-Datensätze von klassischen Alben:
- Pink Floyd - The Dark Side of the Moon (1973)
- The Beatles - Abbey Road (1969)
- Fleetwood Mac - Rumours (1977)
- Eagles - Hotel California (1976)
- Led Zeppelin - Led Zeppelin IV (1971)
- Michael Jackson - Thriller (1982)
- AC/DC - Back in Black (1980)
- Pink Floyd - The Wall (1979)
- Nirvana - Nevermind (1991)
- Radiohead - OK Computer (1997)
- Guns N' Roses - Appetite for Destruction (1987)
- Prince - Purple Rain (1984)
- Bruce Springsteen - Born to Run (1975)
- Steely Dan - Aja (1977)
- The Beach Boys - Pet Sounds (1966)

## API-Endpunkte

| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| GET | `/records` | Alle Records abrufen |
| GET | `/records/{id}` | Record nach ID abrufen |
| POST | `/records` | Neuen Record erstellen |
| PUT | `/records/{id}` | Record aktualisieren |
| DELETE | `/records/{id}?owner={owner}` | Record löschen |

## Fehlerbehandlung

Der Client behandelt verschiedene HTTP-Status-Codes:

- **200 OK**: Erfolgreiche Operation
- **201 Created**: Record erfolgreich erstellt
- **204 No Content**: Record erfolgreich gelöscht
- **400 Bad Request**: Ungültige Anfrage
- **403 Forbidden**: Keine Berechtigung (falscher Owner)
- **404 Not Found**: Record nicht gefunden
- **500 Internal Server Error**: Server-Fehler

## Abhängigkeiten

```kotlin
// Ktor Client
implementation("io.ktor:ktor-client-core:2.x.x")
implementation("io.ktor:ktor-client-cio:2.x.x")
implementation("io.ktor:ktor-client-content-negotiation:2.x.x")
implementation("io.ktor:ktor-serialization-jackson:2.x.x")

// Jackson für JSON
implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.x.x")
```

## Entwicklung

### Projekt-Struktur
```
src/
├── main/
│   ├── kotlin/
│   │   └── Main.kt          # Haupt-Client-Code
│   └── resources/           # CSV-Dateien für Import
│       └── *.csv           # Testdaten-Dateien
```

### Code-Struktur
- **Data Classes**: `Album`, `AlbumResponse`
- **HTTP Client**: Ktor mit Jackson JSON-Serialisierung
- **Menu System**: Interaktive Konsolen-Navigation
- **File Operations**: CSV-Import und Validierung
- **Error Handling**: Umfassende Exception-Behandlung

## Troubleshooting

### Häufige Probleme

**Verbindungsfehler**
- Überprüfen Sie, ob die Record API läuft
- Verifizieren Sie die BASE_URL-Konfiguration
- Prüfen Sie Firewall/Netzwerk-Einstellungen

**CSV-Import-Probleme**
- Stellen Sie sicher, dass das `src/main/resources/`-Verzeichnis existiert
- Überprüfen Sie das CSV-Format
- Achten Sie auf korrekte Encoding (UTF-8)

**Berechtigungsfehler**
- Owner-Name muss exakt übereinstimmen
- Case-sensitive Vergleich

## Lizenz

Bitte beachten Sie die Lizenzbestimmungen Ihres Projekts.

## Beitragen

Verbesserungsvorschläge und Bug-Reports sind willkommen!
