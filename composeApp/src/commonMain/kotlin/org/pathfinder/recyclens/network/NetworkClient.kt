package org.pathfinder.recyclens.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.InternalAPI
import kotlinx.serialization.json.Json
import org.pathfinder.recyclens.models.RecycleImageResponse

// Create a Ktor client with JSON support.
val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }
}

// A suspend function to fetch data from a web endpoint.
@OptIn(InternalAPI::class)
suspend fun fetchRecycleResponse(image: ByteArray): RecycleImageResponse {
    val url = "https://recyclens.herokuapp.com/recycle"
    return httpClient.post(url) {
        setBody(MultiPartFormDataContent(
            formData {
                // Append the image as a binary part.
                append(
                    key = "image",  // name of the form field
                    value = image,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg") // adjust MIME type if needed
                        append(HttpHeaders.ContentDisposition, "filename=\"image.jpg\"")
                    }
                )
                // You can append additional fields if needed:
                append("description", "This is an image upload")
            }
        ))
    }.body()
}
