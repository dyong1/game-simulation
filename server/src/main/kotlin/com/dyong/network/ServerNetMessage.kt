package com.dyong.network

import kotlinx.serialization.Serializable

enum class ServerNetMessageType {
    CONNECTED
}

@Serializable
data class NetMessageConnectedData(
    val type: ServerNetMessageType,
    val sessionToken: String
)

@Serializable
data class ServerNetMessage<Body>(
    val ok: Boolean,
    val data: Body?
)


