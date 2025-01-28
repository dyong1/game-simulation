package com.dyong.network

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@Serializable
data class ClientNetMessage<Body>(
    val sessionToken: String,
    val body: Body
)
class ClientNetMessageParser {
    @OptIn(InternalSerializationApi::class)
    fun <T: Any> parse(message: String, clazz1: KClass<T>): T {
        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString(clazz1.serializer(), message)
    }
}