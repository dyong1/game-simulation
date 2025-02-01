package com.dyong.domain

import com.dyong.game.GameState
import com.dyong.game.StateChangeBroadcaster
import com.dyong.network.UserClient
import org.springframework.stereotype.Component

@Component
class LoginUser(
    private val gameState: GameState,
    private val stateChangeBroadcaster: StateChangeBroadcaster,
) {
    fun execute(userId: Long, client: UserClient) {
        val playerLoggedIn = Player(
            client,
            userId = userId,
            position = Position2D(64L, 123L),
            currentLevelId = 11L,
            experience = 13L
        )

        gameState.players.add(playerLoggedIn)
        stateChangeBroadcaster.notifyLevel(11L)
    }
}