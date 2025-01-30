package com.dyong.game

import com.dyong.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

private val json = Json { encodeDefaults = true }

@Component
class GameServer(
    private val clientMessageHandler: ClientMessageHandler,
    private val clientPool: ClientPool,
    private val gameState: GameState,
    private val stateChangeBroadcaster: StateChangeBroadcaster,
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
        val conn = Connection(clientSocket)

        val userId = loginUser(conn) ?: return

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

    private fun loginUser(conn: Connection): Long? {
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

data class DroppedItem(
    val itemId: Long,
    val position: Position2D,
)

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


@Component
class ClientMessageHandler(
    private val parser: ClientNetMessageParser,
    private val playerMove: PlayerMove,
    private val playerAttack: PlayerAttack,
) {
    fun handle(message: Pair<UserClient, String>) {
        val client = message.first
        val ctx = UseCaseContext(
            client,
        )
        val raw = message.second

        when {
            raw.startsWith(NetMessagePlayerMove.type().toString()) -> playerMove.execute(
                ctx, parser.parse(
                    raw, NetMessagePlayerMove::class
                ).let {
                    PlayerMove.Params(
                        deltaX = it.deltaX,
                        deltaY = it.deltaY
                    )
                }
            )

            raw.startsWith(NetMessagePlayerAttack.type().toString()) -> playerAttack.execute(
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
class PlayerMove(
    private val gameState: GameState,
    private val stateChangeBroadcaster: StateChangeBroadcaster,
) {
    data class Params(
        val deltaX: Long,
        val deltaY: Long
    )

    fun execute(ctx: UseCaseContext, params: Params) {
        val player = gameState.players.find { it.userId == ctx.client.userId } ?: return
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
        val players = gameState.players.filter { it.currentLevelId == levelId }
        val serializableState = NetMessageUpdateLevelState.fromGameState(gameState)
        for (p in players) {
            p.client.send(json.encodeToString(serializableState))
        }
    }
}

@Component
class GameState {
    val players = mutableListOf<Player>()
    val monsters = mutableListOf<Monster>()
    val levels = mutableListOf<Level>()
}

class Monster(
    val id: Long,
    var life: Long,
    val experience: Long,
    val droppables: List<DroppableItem>,
    val position: Position2D
) {
    fun dropItem(): DroppedItem? {
        return droppables.shuffled().getOrNull(0)?.let {
            DroppedItem(
                itemId = it.itemId,
                position = position,
            )
        }
    }
}

@Serializable
data class DroppableItem(
    val itemId: Long
)

data class Level(
    val id: Long,
    val droppedItems: MutableList<DroppedItem>
) {
}

class Player(
    val client: UserClient,
    val userId: Long,
    var position: Position2D,
    var currentLevelId: Long,
    var experience: Long
)

@Serializable
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
        val player = gameState.players.find { it.userId == ctx.client.userId } ?: return
        val level = gameState.levels.find { it.id == player.currentLevelId } ?: return
        val monster = gameState.monsters.find { it.id == params.targetId } ?: return
        monster.life -= params.damage
        if (monster.life <= 0) {
            player.experience += monster.experience
            val item = monster.dropItem()
            if (item != null) {
                item.position.x = monster.position.x
                item.position.y = monster.position.y
                level.droppedItems.add(item)
            }
            gameState.monsters.remove(monster)
        }
        stateChangeBroadcaster.notifyLevel(player.currentLevelId)
    }
}

@Component
class LogoutUser(
    private val gameState: GameState,
    private val stateChangeBroadcaster: StateChangeBroadcaster,
) {
    fun execute(userId: Long) {
        val player = gameState.players.find { it.userId == userId } ?: return
        gameState.players.remove(player)
        stateChangeBroadcaster.notifyLevel(player.currentLevelId)
    }
}

data class UseCaseContext(
    val client: UserClient,
)
