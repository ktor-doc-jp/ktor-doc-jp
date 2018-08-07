package com.example

import java.util.*
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.swagger.experimental.*

interface ConduitAPIClient : ConduitAPI {
    fun setToken(token: String)
}

fun ConduitAPIClient(endpoint: String, client: HttpClient = HttpClient()): ConduitAPIClient = createClient(client, endpoint)
