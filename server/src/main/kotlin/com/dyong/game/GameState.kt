package com.dyong.game

import com.dyong.domain.*
import com.dyong.network.NetMessageUpdateLevelState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component

val json = Json { encodeDefaults = true }

@Component
class GameState {
    val players = mutableListOf<Player>()
    val monsters = mutableListOf<Monster>()
    val levels = mutableListOf<Level>()

    fun loadFromDB() {
        levels.add(
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
        monsters.add(
            Monster(
                id = 2L,
                experience = 10,
                life = 10,
                position = Position2D(22, 10),
                droppables = mutableListOf(DroppableItem(itemId = 1L))
            )
        )
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



