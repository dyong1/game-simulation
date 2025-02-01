package com.dyong.domain

import com.dyong.game.GameState
import com.dyong.game.StateChangeBroadcaster
import com.dyong.netrouting.UseCaseContext
import org.springframework.stereotype.Component

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