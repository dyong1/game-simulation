package com.dyong.configs

import com.dyong.network.ConnectionPool
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ConnectionPoolConfig {
    @Bean
    fun connectionPool(
        @Value("\${rtcserver.port}")
        serverPort: Int
    ): ConnectionPool {
        val pool = ConnectionPool()
        pool.listen(serverPort)
        return pool
    }
}