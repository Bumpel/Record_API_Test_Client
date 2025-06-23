import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import com.fasterxml.jackson.databind.*
import io.ktor.http.*

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
    println("Verbindung zu http://127.0.0.1:8100")
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
            6 -> {
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
    println("6. Beenden")
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
        val response: HttpResponse = client.post("http://127.0.0.1:8100/records") {
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
        val response: HttpResponse = client.get("http://127.0.0.1:8100/records")
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
        val response: HttpResponse = client.get("http://127.0.0.1:8100/records/$id")
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
        val response: HttpResponse = client.put("http://127.0.0.1:8100/records/$id") {
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
        val response: HttpResponse = client.delete("http://127.0.0.1:8100/records/$id") {
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
            else -> println("⚠️ Unerwarteter Status Code")
        }
    } catch (e: Exception) {
        println("Fehler beim Löschen: ${e.message}")
    }
}