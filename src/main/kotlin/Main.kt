import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import com.fasterxml.jackson.databind.*
import io.ktor.http.*

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
            7 -> {
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
    println("7. Beenden")
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

    var successCount = 0
    var errorCount = 0

    println("Füge ${testAlbums.size} Testdatensätze hinzu...")
    println()

    for ((index, album) in testAlbums.withIndex()) {
        try {
            val response: HttpResponse = client.post("$BASE_URL/records") {
                contentType(ContentType.Application.Json)
                setBody(album)
            }

            if (response.status.isSuccess()) {
                successCount++
                println("✓ ${index + 1}/${testAlbums.size} - ${album.title} by ${album.artist} (Owner: ${album.owner})")
            } else {
                errorCount++
                println("✗ ${index + 1}/${testAlbums.size} - Fehler bei ${album.title}: Status ${response.status}")
            }

        } catch (e: Exception) {
            errorCount++
            println("✗ ${index + 1}/${testAlbums.size} - Exception bei ${album.title}: ${e.message}")
        }

        // Kleine Pause zwischen den Requests
        kotlinx.coroutines.delay(100)
    }

    println()
    println("=== Zusammenfassung ===")
    println("Erfolgreich hinzugefügt: $successCount")
    println("Fehler: $errorCount")
    println("Gesamt: ${testAlbums.size}")
}