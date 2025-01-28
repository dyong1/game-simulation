package com.dyong.configs

import com.dyong.network.ConnectionPool
import com.dyong.network.UserConnections
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ConnectionPoolConfig (
    private val userConnections: UserConnections
){
    @Bean
    fun connectionPool(
        @Value("\${rtcserver.port}")
        serverPort: Int
    ): ConnectionPool {
        val pool = ConnectionPool(userConnections)
        pool.listen(serverPort)
        return pool
    }
}