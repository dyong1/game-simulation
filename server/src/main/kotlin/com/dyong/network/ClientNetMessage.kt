package com.dyong.network

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

enum class ClientNetMessageType {
    LOGIN_PLAYER,
    PLAYER_MOVE,
    PLAYER_ATTACK,
}

@Serializable
data class NetMessageUserHandshake(
    val userId: Long
) {
    companion object {
        fun type(): ClientNetMessageType {
            return ClientNetMessageType.LOGIN_PLAYER
        }
    }
}

@Serializable
data class NetMessagePlayerMove(
    val deltaX: Long,
    val deltaY: Long
) {
    companion object {
        fun type(): ClientNetMessageType {
            return ClientNetMessageType.PLAYER_MOVE
        }
    }
}

@Serializable
data class NetMessagePlayerAttack(
    val targetId: Long,
    val damage: Long
) {
    companion object {
        fun type(): ClientNetMessageType {
            return ClientNetMessageType.PLAYER_ATTACK
        }
    }
}

@Component
class ClientNetMessageParser {
    @OptIn(InternalSerializationApi::class)
    fun <T : Any> parse(message: String, clazz1: KClass<T>): T {
        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString(
            clazz1.serializer(),
            message.substring(message.indexOf(" ") + 1)
        )
    }
}

