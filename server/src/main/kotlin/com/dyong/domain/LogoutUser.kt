package com.dyong.domain

import com.dyong.game.GameState
import com.dyong.game.StateChangeBroadcaster
import org.springframework.stereotype.Component

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