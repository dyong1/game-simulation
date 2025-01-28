package com.dyong.network

import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import java.net.ServerSocket

@Component
class ConnectionPool {
    val connections = mutableListOf<Connection>()
    fun listen(
        port: Int
    ) {
        val serverSocket = ServerSocket(port)
        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {
            while (true) {
                val clientSocket = serverSocket.accept()
                connections.add(Connection(clientSocket))
            }
        }
    }
    fun connectedConnections(): List<Connection> {
        return connections.filter { it.isConnected() }
    }
}