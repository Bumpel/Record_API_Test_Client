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

suspend fun main() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }

    // 1. POST: Album an Server senden
    val newAlbum = Album(

        owner = "alice",
        title = "Dark Side of the Moon",
        artist = "Pink Floyd",
        year = 1973
    )

    val postResponse: HttpResponse = client.post("http://192.168.179.3:8100/album") {
        contentType(ContentType.Application.Json)
        setBody(newAlbum)
    }

    println("POST Status: ${postResponse.status}")
    println("POST Response: ${postResponse.bodyAsText()}")

    // 2. GET: Album oder Liste von Alben abrufen
    val getResponse: HttpResponse = client.get("http://192.168.179.3:8100/album")
    println("GET Status: ${getResponse.status}")
    println("GET Response: ${getResponse.bodyAsText()}")

    client.close()
}