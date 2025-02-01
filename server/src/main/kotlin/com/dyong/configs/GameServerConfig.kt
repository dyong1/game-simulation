package com.dyong.configs

import com.dyong.domain.LoginUser
import com.dyong.domain.LogoutUser
import com.dyong.game.*
import com.dyong.netrouting.ClientMessageHandler
import com.dyong.network.ClientPool
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
        loginUser: LoginUser,
        logoutUser: LogoutUser
    ): GameServer {
        val server = GameServer(
            clientMessageHandler,
            clientPool,
            gameState,
            stateChangeBroadcaster,
            loginUser,
            logoutUser,
        )
        server.start(port)
        return server
    }
}