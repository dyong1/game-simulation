package com.dyong.game

import Level
import Monster
import Player
import com.dyong.network.NetMessageUpdateLevelState
import json
import kotlinx.serialization.encodeToString
import org.springframework.stereotype.Component

@Component
class GameState {
    val players = mutableListOf<Player>()
    val monsters = mutableListOf<Monster>()
    val levels = mutableListOf<Level>()
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

