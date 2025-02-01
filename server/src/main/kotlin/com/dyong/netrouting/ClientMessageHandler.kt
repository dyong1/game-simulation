package com.dyong.netrouting

import com.dyong.domain.*
import com.dyong.network.UserClient
import com.dyong.network.ClientNetMessageParser
import com.dyong.network.NetMessagePlayerAttack
import com.dyong.network.NetMessagePlayerMove
import org.springframework.stereotype.Component

@Component
class ClientMessageHandler(
    private val parser: ClientNetMessageParser,
    private val playerMove: PlayerMove,
    private val playerAttack: PlayerAttack,
) {
    fun handle(message: Pair<UserClient, String>) {
        val client = message.first
        val ctx = UseCaseContext(
            client,
        )
        val raw = message.second

        when {
            raw.startsWith(NetMessagePlayerMove.type().toString()) -> playerMove.execute(
                ctx, parser.parse(
                    raw, NetMessagePlayerMove::class
                ).let {
                    PlayerMove.Params(
                        deltaX = it.deltaX,
                        deltaY = it.deltaY
                    )
                }
            )

            raw.startsWith(NetMessagePlayerAttack.type().toString()) -> playerAttack.execute(
                ctx, parser.parse(
                    raw, NetMessagePlayerAttack::class
                ).let {
                    PlayerAttack.Params(
                        targetId = it.targetId,
                        damage = it.damage,
                    )
                }
            )
        }
    }
}

data class UseCaseContext(
    val client: UserClient,
)
