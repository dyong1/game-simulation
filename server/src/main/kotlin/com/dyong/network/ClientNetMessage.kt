package com.dyong.network

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Serializable
data class NetMessageUserHandshake(
    val userId: Long
)

@Serializable
data class NetMessagePlayerMove(
    val deltaX: Long,
    val deltaY: Long
) {
    companion object {
        fun type(): String {
            return "PLAYER_MOVE"
        }
    }
}

@Serializable
data class NetMessagePlayerAttack(
    val targetId: Long,
    val damage: Long
) {
    companion object {
        fun type(): String {
            return "PLAYER_ATTACK"
        }
    }
}

@Serializable
data class NetMessagePlayerUseSkill(
    val skillId: Long,
    val targetId: Long?
) {
    companion object {
        fun type(): String {
            return "PLAYER_USE_SKILL"
        }
    }
}

@Serializable
data class ClientNetMessage<Body>(
    val sessionToken: String,
    val type: String,
    val body: Body
)

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

