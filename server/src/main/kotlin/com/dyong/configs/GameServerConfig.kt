package com.dyong.configs

import com.dyong.game.GameServer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class GameServerConfig {

    @Bean
    fun gameServer(
        @Value("\${rtcserver.port}")
        port: Int,
    ): GameServer {
        val server = GameServer()
        server.start(port)
        return server
    }
}