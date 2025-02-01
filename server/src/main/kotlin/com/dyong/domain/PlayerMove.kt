package com.dyong.domain

import com.dyong.game.GameState
import com.dyong.game.StateChangeBroadcaster
import com.dyong.netrouting.UseCaseContext
import org.springframework.stereotype.Component

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