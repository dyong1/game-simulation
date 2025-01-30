package com.dyong.configs

import com.dyong.game.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class GameServerConfig {

    @Bean
    fun gameServer(
        @Value("\${rtcserver.port}")
        port: Int,
        clientMessageHandler: ClientMessageHandler,
        clientPool: ClientPool,
        gameState: GameState,
        stateChangeBroadcaster: StateChangeBroadcaster,
        logoutUser: LogoutUser
    ): GameServer {
        val server = GameServer(
            clientMessageHandler,
            clientPool,
            gameState,
            stateChangeBroadcaster,
            logoutUser,
        )
        server.start(port)
        return server
    }
}