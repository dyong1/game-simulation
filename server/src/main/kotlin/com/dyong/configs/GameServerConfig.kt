package com.dyong.configs

import com.dyong.game.ClientMessageHandler
import com.dyong.game.ClientPool
import com.dyong.game.GameServer
import com.dyong.game.GameState
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
    ): GameServer {
        val server = GameServer(
            clientMessageHandler,
            clientPool,
            gameState,
        )
        server.start(port)
        return server
    }
}