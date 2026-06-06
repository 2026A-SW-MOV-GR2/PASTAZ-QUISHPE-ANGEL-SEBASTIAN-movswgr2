package com.example.kotlin_fundamentals.network

import com.example.kotlin_fundamentals.model.Post
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.json

class PostApiClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getPost(id: Int): Post {
        return client.get("https://jsonplaceholder.typicode.com/posts/$id").body()
    }

    suspend fun updatePost(id: Int, post: Post): Post {
        return client.put("https://jsonplaceholder.typicode.com/posts/$id") {
            setBody(post)
        }.body()
    }
}

