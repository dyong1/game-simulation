package com.dyong.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import java.net.ServerSocket

@Component
class ConnectionPool(
    private val users: UserConnections,
) {
    fun listen(
        port: Int
    ) {
        val serverSocket = ServerSocket(port)
        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {
            while (true) {
                val clientSocket = serverSocket.accept()
                val conn = Connection(clientSocket)
                val sessionToken = issueSessionToken()
                conn.sendMessage(
                    Json.encodeToString(
                        ServerNetMessage(
                            true,
                            NetMessageConnectedData(
                                ServerNetMessageType.CONNECTED,
                                sessionToken
                            )
                        )
                    )
                )
                users.add(conn, sessionToken)
            }
        }
    }
    private fun issueSessionToken(): String {
        return "test_session_token"
    }
}

@Component
class UserConnections {
    val connectionBySessionToken = hashMapOf<String, Connection>()
    fun add(connection: Connection, sessionToken: String) {
        connectionBySessionToken[sessionToken] = connection
    }
}

