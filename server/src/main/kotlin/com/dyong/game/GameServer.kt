package com.dyong.game

import DroppableItem
import DroppedItem
import Level
import Monster
import Player
import Position2D
import com.dyong.domain.LoginUser
import com.dyong.domain.LogoutUser
import com.dyong.netrouting.ClientMessageHandler
import com.dyong.network.ClientNetMessageParser
import com.dyong.network.NetConnection
import com.dyong.network.NetMessagePlayerLogin
import com.dyong.network.NetMessageUserLogin
import json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.springframework.stereotype.Component
import java.net.ServerSocket
import java.net.Socket

@Component
class GameServer(
    private val clientMessageHandler: ClientMessageHandler,
    private val clientPool: ClientPool,
    private val gameState: GameState,
    private val stateChangeBroadcaster: StateChangeBroadcaster,
    private val loginUser: LoginUser,
    private val logoutUser: LogoutUser,
) {
    private val incomingMessages = Channel<Pair<UserClient, String>>()

    fun start(serverPort: Int) {
        loadGameState()

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

    private fun loadGameState() {
        gameState.levels.add(
            Level(
                id = 11L,
                droppedItems = mutableListOf(
                    DroppedItem(
                        itemId = 1L,
                        position = Position2D(13L, 33L),
                    )
                )
            )
        )
        gameState.monsters.add(
            Monster(
                id = 2L,
                experience = 10,
                life = 10,
                position = Position2D(22, 10),
                droppables = mutableListOf(DroppableItem(itemId = 1L))
            )
        )
    }

    private suspend fun handleNewClient(clientSocket: Socket) {
        val conn = NetConnection(clientSocket)

        val userId = loginUser.execute(conn) ?: return

        val client = UserClient(userId, conn)
        clientPool.add(userId, client)
        val playerLoggedIn = Player(
            client,
            userId = userId,
            position = Position2D(64L, 123L),
            currentLevelId = 11L,
            experience = 13L
        )

        gameState.players.add(playerLoggedIn)
        stateChangeBroadcaster.notifyLevel(11L)
        client.send(json.encodeToString(NetMessagePlayerLogin.fromPlayer(playerLoggedIn)))

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

