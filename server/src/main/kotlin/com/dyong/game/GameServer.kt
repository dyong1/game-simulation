package com.dyong.game

import com.dyong.domain.LoginUser
import com.dyong.domain.LogoutUser
import com.dyong.netrouting.ClientMessageHandler
import com.dyong.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import java.net.ServerSocket
import java.net.Socket

@Component
class GameServer(
    private val clientMessageHandler: ClientMessageHandler,
    private val clientPool: ClientPool,
    private val gameState: GameState,
    private val loginUser: LoginUser,
    private val logoutUser: LogoutUser,
) {
    private val incomingMessages = Channel<Pair<UserClient, String>>()

    fun start(serverPort: Int) {
        gameState.loadFromDB()

        val serverSocket = ServerSocket(serverPort)

        CoroutineScope(Dispatchers.Default).launch {
            handleIncomingMessages()
        }

        while (true) {
            val clientSocket = serverSocket.accept()
            CoroutineScope(Dispatchers.IO).launch {
                handleNewClient(clientSocket)
            }
        }
    }

    private suspend fun handleNewClient(clientSocket: Socket) {
        val conn = NetConnection(clientSocket)

        val raw = conn.readMessage() ?: return
        val m = ClientNetMessageParser().parse(raw, NetMessageUserLogin::class)
        val userId = m.userId

        val client = UserClient(userId, conn)
        clientPool.add(userId, client)
        loginUser.execute(userId, client)

        try {
            client.receiveMessages(
                onMessage = { m -> incomingMessages.send(client to m) },
                onLogout = { logoutUser.execute(userId) }
            )
        } finally {
            val player = gameState.players.find { it.userId == userId }
            gameState.players.remove(player)
            clientPool.remove(userId)
        }
    }

    private suspend fun handleIncomingMessages() {
        for (m in incomingMessages) {
            clientMessageHandler.handle(m)
        }
    }
}

