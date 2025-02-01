package com.dyong.domain

import com.dyong.network.ClientNetMessageParser
import com.dyong.network.NetConnection
import com.dyong.network.NetMessageUserLogin
import org.springframework.stereotype.Component

@Component
class LoginUser {
    fun execute(conn: NetConnection): Long? {
        val raw = conn.readMessage() ?: return null
        val m = ClientNetMessageParser().parse(raw, NetMessageUserLogin::class)
        return m.userId
    }
}