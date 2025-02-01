package com.dyong.network

import com.dyong.domain.*
import com.dyong.game.GameState
import kotlinx.serialization.Serializable

@Serializable
enum class ServerNetMessageType {
    PLAYER_LOGIN,
    UPDATE_LEVEL_STATE
}

@Serializable
data class NetMessagePlayerLogin(
    @Serializable
    val type: ServerNetMessageType = ServerNetMessageType.PLAYER_LOGIN,
    @Serializable
    val player: UpdatePlayerState
) {
    companion object {
        fun fromPlayer(player: Player): NetMessagePlayerLogin {
            return NetMessagePlayerLogin(
                player = UpdatePlayerState(
                    userId = player.userId,
                    position = player.position,
                    currentLevelId = player.currentLevelId,
                    experience = player.experience,
                )
            )
        }
    }
}

@Serializable
data class NetMessageUpdateLevelState(
    @Serializable
    val type: ServerNetMessageType = ServerNetMessageType.UPDATE_LEVEL_STATE,
    @Serializable
    val monsters: List<UpdateMonsterState>,
    @Serializable
    val players: List<UpdatePlayerState>,
) {
    companion object {
        fun fromGameState(gameState: GameState): NetMessageUpdateLevelState {
            return NetMessageUpdateLevelState(
                players = gameState.players.map {
                    UpdatePlayerState(
                        userId = it.userId,
                        position = it.position,
                        currentLevelId = it.currentLevelId,
                        experience = it.experience,
                    )
                },
                monsters = gameState.monsters.map {
                    UpdateMonsterState(
                        id = it.id,
                        life = it.life,
                        position = it.position,
                    )
                }
            )
        }
    }
}

@Serializable
data class UpdatePlayerState(
    val userId: Long,
    var position: Position2D,
    var currentLevelId: Long,
    var experience: Long
)

@Serializable
data class UpdateMonsterState(
    val id: Long,
    var life: Long,
    val position: Position2D
)