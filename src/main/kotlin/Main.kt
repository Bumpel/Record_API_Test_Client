import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import java.io.File

// BASE_URL Variable - hier können Sie die URL ändern
const val BASE_URL = "http://127.0.0.1:8100"

data class Album(
    val owner: String,
    val title: String,
    val artist: String,
    val year: Int
)

data class AlbumResponse(
    val id: Int,
    val owner: String,
    val title: String,
    val artist: String,
    val year: Int
)

suspend fun main() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }

    println("=== Record API Test Client ===")
    println("Verbindung zu $BASE_URL")
    println()

    while (true) {
        showMenu()

        print("Wählen Sie eine Option: ")
        val choice = readLine()?.toIntOrNull()

        when (choice) {
            1 -> createRecord(client)
            2 -> getAllRecords(client)
            3 -> getRecordById(client)
            4 -> updateRecord(client)
            5 -> deleteRecord(client)
            6 -> fillDatabaseWithTestData(client)
            7 -> fillDatabaseFromFile(client)
            8 -> {
                println("Auf Wiedersehen!")
                break
            }
            else -> println("Ungültige Auswahl. Bitte versuchen Sie es erneut.")
        }

        println("\n" + "=".repeat(50) + "\n")
    }

    client.close()
}

fun showMenu() {
    println("1. Record erstellen (POST)")
    println("2. Alle Records anzeigen (GET)")
    println("3. Record nach ID suchen (GET)")
    println("4. Record aktualisieren (PUT)")
    println("5. Record löschen (DELETE)")
    println("6. Datenbank mit Testdaten füllen")
    println("7. Testdaten aus CSV-Datei laden")
    println("8. Beenden")
    println()
}

suspend fun createRecord(client: HttpClient) {
    println("=== Record erstellen ===")

    print("Owner: ")
    val owner = readLine() ?: return

    print("Titel: ")
    val title = readLine() ?: return

    print("Artist: ")
    val artist = readLine() ?: return

    print("Jahr: ")
    val year = readLine()?.toIntOrNull() ?: run {
        println("Ungültiges Jahr eingegeben.")
        return
    }

    val newAlbum = Album(
        owner = owner,
        title = title,
        artist = artist,
        year = year
    )

    try {
        val response: HttpResponse = client.post("$BASE_URL/records") {
            contentType(ContentType.Application.Json)
            setBody(newAlbum)
        }

        println("Status: ${response.status}")
        println("Response: ${response.bodyAsText()}")
    } catch (e: Exception) {
        println("Fehler beim Erstellen: ${e.message}")
    }
}

suspend fun getAllRecords(client: HttpClient) {
    println("=== Alle Records abrufen ===")

    try {
        val response: HttpResponse = client.get("$BASE_URL/records")
        println("Status: ${response.status}")
        println("Response: ${response.bodyAsText()}")
    } catch (e: Exception) {
        println("Fehler beim Abrufen: ${e.message}")
    }
}

suspend fun getRecordById(client: HttpClient) {
    println("=== Record nach ID suchen ===")

    print("Record ID: ")
    val id = readLine()?.toIntOrNull() ?: run {
        println("Ungültige ID eingegeben.")
        return
    }

    try {
        val response: HttpResponse = client.get("$BASE_URL/records/$id")
        println("Status: ${response.status}")
        println("Response: ${response.bodyAsText()}")
    } catch (e: Exception) {
        println("Fehler beim Abrufen: ${e.message}")
    }
}

suspend fun updateRecord(client: HttpClient) {
    println("=== Record aktualisieren ===")

    print("Record ID: ")
    val id = readLine()?.toIntOrNull() ?: run {
        println("Ungültige ID eingegeben.")
        return
    }

    print("Owner (muss mit dem ursprünglichen Owner übereinstimmen): ")
    val owner = readLine() ?: return

    print("Neuer Titel: ")
    val title = readLine() ?: return

    print("Neuer Artist: ")
    val artist = readLine() ?: return

    print("Neues Jahr: ")
    val year = readLine()?.toIntOrNull() ?: run {
        println("Ungültiges Jahr eingegeben.")
        return
    }

    val updatedAlbum = Album(
        owner = owner,
        title = title,
        artist = artist,
        year = year
    )

    try {
        val response: HttpResponse = client.put("$BASE_URL/records/$id") {
            contentType(ContentType.Application.Json)
            setBody(updatedAlbum)
        }

        println("Status: ${response.status}")
        println("Response: ${response.bodyAsText()}")

        when (response.status) {
            HttpStatusCode.OK -> println("Record erfolgreich aktualisiert!")
            HttpStatusCode.NotFound -> println("Record nicht gefunden!")
            HttpStatusCode.Forbidden -> println("Nur der Owner darf den Record aktualisieren!")
            else -> println("Unerwarteter Status Code")
        }
    } catch (e: Exception) {
        println("Fehler beim Aktualisieren: ${e.message}")
    }
}

suspend fun deleteRecord(client: HttpClient) {
    println("=== Record löschen ===")

    print("Record ID: ")
    val id = readLine()?.toIntOrNull() ?: run {
        println("Ungültige ID eingegeben.")
        return
    }

    print("Owner (zur Berechtigung): ")
    val owner = readLine() ?: return

    try {
        val response: HttpResponse = client.delete("$BASE_URL/records/$id") {
            url {
                parameters.append("owner", owner)
            }
        }

        println("Status: ${response.status}")
        println("Response: ${response.bodyAsText()}")

        when (response.status) {
            HttpStatusCode.NoContent -> println("Record erfolgreich gelöscht!")
            HttpStatusCode.NotFound -> println("Record nicht gefunden!")
            HttpStatusCode.Forbidden -> println("Nur der Owner darf den Record löschen!")
            else -> println("Unerwarteter Status Code")
        }
    } catch (e: Exception) {
        println("Fehler beim Löschen: ${e.message}")
    }
}

suspend fun fillDatabaseWithTestData(client: HttpClient) {
    println("=== Datenbank mit Testdaten füllen ===")

    val testAlbums = listOf(
        Album("Max", "The Dark Side of the Moon", "Pink Floyd", 1973),
        Album("Anna", "Abbey Road", "The Beatles", 1969),
        Album("Peter", "Rumours", "Fleetwood Mac", 1977),
        Album("Lisa", "Hotel California", "Eagles", 1976),
        Album("Tom", "Led Zeppelin IV", "Led Zeppelin", 1971),
        Album("Sarah", "Thriller", "Michael Jackson", 1982),
        Album("Chris", "Back in Black", "AC/DC", 1980),
        Album("Emma", "The Wall", "Pink Floyd", 1979),
        Album("Mike", "Nevermind", "Nirvana", 1991),
        Album("Julia", "OK Computer", "Radiohead", 1997),
        Album("Alex", "Appetite for Destruction", "Guns N' Roses", 1987),
        Album("Nina", "Purple Rain", "Prince", 1984),
        Album("Ben", "Born to Run", "Bruce Springsteen", 1975),
        Album("Sophie", "Aja", "Steely Dan", 1977),
        Album("David", "Pet Sounds", "The Beach Boys", 1966)
    )

    uploadAlbums(client, testAlbums)
}

suspend fun fillDatabaseFromFile(client: HttpClient) {
    println("=== Testdaten aus CSV-Datei laden ===")

    val resourcesPath = "src/main/resources"
    val resourcesDir = File(resourcesPath)

    if (!resourcesDir.exists()) {
        println("Resources-Verzeichnis '$resourcesPath' nicht gefunden!")
        println("Bitte erstellen Sie das Verzeichnis und legen Sie dort eine CSV-Datei ab.")
        return
    }

    // Alle CSV-Dateien im resources-Verzeichnis finden
    val csvFiles = resourcesDir.listFiles { _, name ->
        name.endsWith(".csv", ignoreCase = true)
    }?.toList() ?: emptyList()

    if (csvFiles.isEmpty()) {
        println("Keine CSV-Dateien im Verzeichnis '$resourcesPath' gefunden!")
        println("Bitte legen Sie eine CSV-Datei mit folgendem Format ab:")
        println("owner,title,artist,year")
        println("\"Max\",\"The Dark Side of the Moon\",\"Pink Floyd\",1973")
        return
    }

    println("Verfügbare CSV-Dateien:")
    csvFiles.forEachIndexed { index, file ->
        println("${index + 1}. ${file.name}")
    }
    println()

    print("Wählen Sie eine Datei (1-${csvFiles.size}): ")
    val choice = readLine()?.toIntOrNull()

    if (choice == null || choice < 1 || choice > csvFiles.size) {
        println("Ungültige Auswahl.")
        return
    }

    val selectedFile = csvFiles[choice - 1]

    try {
        val albums = loadFromCsv(selectedFile)

        if (albums.isEmpty()) {
            println("Keine gültigen Datensätze in der Datei gefunden.")
            return
        }

        println("${albums.size} Datensätze aus '${selectedFile.name}' geladen.")
        uploadAlbums(client, albums)

    } catch (e: Exception) {
        println("Fehler beim Laden der Datei: ${e.message}")
    }
}

fun loadFromJson(file: File): List<Album> {
    val mapper = jacksonObjectMapper()
    return try {
        val jsonText = file.readText()
        mapper.readValue<List<Album>>(jsonText)
    } catch (e: Exception) {
        println("Fehler beim Parsen der JSON-Datei: ${e.message}")
        emptyList()
    }
}

fun loadFromCsv(file: File): List<Album> {
    return try {
        val lines = file.readLines()
        if (lines.isEmpty()) return emptyList()

        val albums = mutableListOf<Album>()

        // Skip header if present
        val dataLines = if (lines.first().contains("owner", ignoreCase = true) ||
            lines.first().contains("title", ignoreCase = true)) {
            lines.drop(1)
        } else {
            lines
        }

        for ((lineIndex, line) in dataLines.withIndex()) {
            val parts = line.split(",").map { it.trim().removeSurrounding("\"") }

            if (parts.size >= 4) {
                try {
                    val album = Album(
                        owner = parts[0],
                        title = parts[1],
                        artist = parts[2],
                        year = parts[3].toInt()
                    )
                    albums.add(album)
                } catch (e: Exception) {
                    println("Warnung: Zeile ${lineIndex + 2} konnte nicht geparst werden: $line")
                }
            } else {
                println("Warnung: Zeile ${lineIndex + 2} hat nicht genügend Spalten: $line")
            }
        }

        albums
    } catch (e: Exception) {
        println("Fehler beim Parsen der CSV-Datei: ${e.message}")
        emptyList()
    }
}

suspend fun uploadAlbums(client: HttpClient, albums: List<Album>) {
    var successCount = 0
    var errorCount = 0

    println("Füge ${albums.size} Datensätze hinzu...")
    println()

    for ((index, album) in albums.withIndex()) {
        try {
            val response: HttpResponse = client.post("$BASE_URL/records") {
                contentType(ContentType.Application.Json)
                setBody(album)
            }

            if (response.status.isSuccess()) {
                successCount++
                println("✓ ${index + 1}/${albums.size} - ${album.title} by ${album.artist} (Owner: ${album.owner})")
            } else {
                errorCount++
                println("✗ ${index + 1}/${albums.size} - Fehler bei ${album.title}: Status ${response.status}")
            }

        } catch (e: Exception) {
            errorCount++
            println("✗ ${index + 1}/${albums.size} - Exception bei ${album.title}: ${e.message}")
        }

        // Kleine Pause zwischen den Requests
        kotlinx.coroutines.delay(100)
    }

    println()
    println("=== Zusammenfassung ===")
    println("Erfolgreich hinzugefügt: $successCount")
    println("Fehler: $errorCount")
    println("Gesamt: ${albums.size}")
}