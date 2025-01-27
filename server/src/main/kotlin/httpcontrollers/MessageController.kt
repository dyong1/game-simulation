package httpcontrollers

import models.MessageChannel
import models.MessageChannelType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import services.messaging.SendMessage

@RestController
class MessageController(
    private val sendMessageServ: SendMessage
) {
    @PostMapping("/messages")
    fun sendMessage(
        body: SendMessageRequestBody
    ) {
        this.sendMessageServ.command(
            body.type,
            body.content,
            body.senderId,
            body.receiverId
        )
    }
}

data class SendMessageRequestBody(
    val type: MessageChannelType,
    val content: String,
    val senderId: Long,
    val receiverId: Long?
)