package com.dyong.game

import com.dyong.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class GameServer(
    private val clientMessageHandler: ClientMessageHandler,
    private val clientPool: ClientPool,
    private val gameState: GameState,
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
        //WIP:
    }

    private suspend fun handleNewClient(clientSocket: Socket) {
        val conn = Connection(clientSocket)

        val userId = handshake(conn) ?: return

        val sessionToken = UUID.randomUUID().toString()
        val client = UserClient(userId, conn)
        clientPool.add(userId, client)
        gameState.addPlayer(Player(
            userId = userId,
            position = Position2D(64L,123L),
            currentLevelId = 11L,
            experience = 13L
        ))
        client.send(
            Json.encodeToString(
                ServerNetMessage(
                    true, NetMessageConnectedData(
                        ServerNetMessageType.CONNECTED, sessionToken
                    )
                )
            )
        )

        try {
            client.receiveMessages { m -> incomingMessages.send(client to m) }
        } finally {
            val player = gameState.findPlayerByUserId(userId)
            gameState.removePlayer(player)
            clientPool.remove(userId)
        }
    }

    private fun handshake(conn: Connection): Long? {
        val raw = conn.readMessage() ?: return null
        val m = ClientNetMessageParser().parse(raw, NetMessageUserHandshake::class)
        return m.userId
    }

    private suspend fun handleIncomingMessages() {
        for (m in incomingMessages) {
            clientMessageHandler.handle(m)
        }
    }
}

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
    private val connection: Connection,
) {
    fun send(message: String) {
        connection.sendMessage(message)
    }

    suspend fun receiveMessages(onMessage: suspend (String) -> Unit) {
        try {
            while (true) {
                val m = connection.readMessage() ?: break
                onMessage(m)
            }
        } finally {
            connection.close()
        }
    }
}


@Component
class ClientMessageHandler(
    private val parser: ClientNetMessageParser,
    private val playerMove: PlayerMove,
    private val playerAttack: PlayerAttack,
    private val sessionValidater: SessionValidater,
) {
    fun handle(message: Pair<UserClient, String>) {
        val client = message.first
        val ctx = UseCaseContext(
            client,
        )
        val raw = message.second

        val session = parser.parse(raw, SessionWrapper::class)
        val result = sessionValidater.validate(session.sessionToken)
        if (!result.ok) {
            return
        }

        when {
            raw.startsWith(NetMessagePlayerMove.type()) -> playerMove.execute(
                ctx, parser.parse(
                    raw, NetMessagePlayerMove::class
                ).let {
                    PlayerMove.Params(
                        deltaX = it.deltaX,
                        deltaY = it.deltaY
                    )
                }
            )

            raw.startsWith(NetMessagePlayerAttack.type()) -> playerAttack.execute(
                ctx, parser.parse(
                    raw, NetMessagePlayerAttack::class
                ).let {
                    PlayerAttack.Params(
                        targetId = it.targetId,
                        damage = it.damage,
                    )
                }
            )
        }
    }
}

@Component
class SessionValidater {
    data class ValidateResult(val ok: Boolean)

    fun validate(sessionToken: String): ValidateResult {
        return ValidateResult(true)
    }

}

@Component
class PlayerMove(
    val gameState: GameState,
    val stateChangeBroadcaster: StateChangeBroadcaster,
) {
    data class Params(
        val deltaX: Long,
        val deltaY: Long
    )

    fun execute(ctx: UseCaseContext, params: Params) {
        val player = gameState.findPlayerByUserId(ctx.client.userId) ?: return
        player.position.x += params.deltaX
        player.position.y += params.deltaY
        stateChangeBroadcaster.notifyLevel(player.currentLevelId)
    }
}

@Component
class StateChangeBroadcaster(
    private val gameState: GameState
) {
    fun notifyLevel(levelId: Long) {
        //WIP:
    }
}

@Component
class GameState {
    private val players = mutableListOf<Player>()
    private val monsters = mutableListOf<Monster>()

    fun findPlayerByUserId(userId: Long): Player? {
        return players.find { p -> p.userId == userId }
    }

    fun findLevelById(currentLevelId: Long): Level? {
        return null
    }

    fun findMonster(currentLevelId: Long): Monster? {
        return null
    }

    fun removeMonster(monster: Monster) {
        monsters.remove(monster)
    }

    fun removePlayer(player: Player?) {
        players.remove(player)
    }

    fun addPlayer(player: Player) {
        players.add(player)
    }
}

class Monster(
    var life: Long,
    val experience: Long,
    val droppables: List<DroppableObject>,
    val position: Position2D
) {
    fun dropItem(): DroppableObject? {
        return droppables.shuffled().getOrNull(0)
    }
}

data class DroppableObject(val id: Long, val position: Position2D)

data class Level(
    val id: Long,
    val droppedItems: MutableList<DroppableObject>
) {
}

class Player(
    val userId: Long,
    var position: Position2D,
    var currentLevelId: Long,
    var experience: Long
)

data class Position2D(
    var x: Long,
    var y: Long
)

@Component
class PlayerAttack(
    val gameState: GameState,
    val stateChangeBroadcaster: StateChangeBroadcaster,
) {
    data class Params(
        val targetId: Long,
        val damage: Long
    )

    fun execute(ctx: UseCaseContext, params: Params) {
        val player = gameState.findPlayerByUserId(ctx.client.userId) ?: return
        val level = gameState.findLevelById(player.currentLevelId) ?: return
        val monster = gameState.findMonster(params.targetId) ?: return
        monster.life -= params.damage
        if (monster.life < 0) {
            player.experience += monster.experience
            val item = monster.dropItem()
            if (item != null) {
                item.position.x = monster.position.x
                item.position.y = monster.position.y
                level.droppedItems.add(item)
            }
            gameState.removeMonster(monster)
        }
        stateChangeBroadcaster.notifyLevel(player.currentLevelId)
    }
}

data class UseCaseContext(
    val client: UserClient,
)
