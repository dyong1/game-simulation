package com.dyong.network

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

class MessageParser {
    @OptIn(InternalSerializationApi::class)
    fun <H : Any, B : Any> parse(message: String, clazz1: KClass<H>, clazz2: KClass<B>): Pair<H, B> {
        val headerLength = message.takeWhile { it.isDigit() }.toInt()

        val headerStartsAt = headerLength.toString().length
        val header = message.substring(headerStartsAt, headerStartsAt + headerLength)
        val body = message.substring(headerStartsAt + headerLength)

        val json = Json { ignoreUnknownKeys = true }
        val headerJson = json.decodeFromString(clazz1.serializer(), header)
        val bodyJson = json.decodeFromString(clazz2.serializer(), body)

        return Pair(headerJson, bodyJson)
    }
}