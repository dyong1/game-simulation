package com.dyong.network

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ClientPool {
    private val clientByUserId = ConcurrentHashMap<Long, UserClient>()
    fun add(userId: Long, client: UserClient) {
        clientByUserId[userId] = client
    }

    fun remove(userId: Long) {
        clientByUserId.remove(userId)
    }
}

class UserClient(
    val userId: Long,
    private val connection: NetConnection,
) {
    fun send(message: String) {
        connection.sendMessage(message)
    }

    suspend fun receiveMessages(onMessage: suspend (String) -> Unit, onLogout: suspend () -> Unit) {
        try {
            while (true) {
                val m = connection.readMessage() ?: break
                onMessage(m)
            }
        } finally {
            onLogout()
            connection.close()
        }
    }
}
