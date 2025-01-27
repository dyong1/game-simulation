package services.messaging

import models.Message
import models.MessageChannel
import models.MessageContent
import models.MessageQueue
import throwables.TypedThrowable

class SendMessage(
    private val messageQueueRepo: MessageQueueRepository
) {
    fun command(sender: MessageChannel, receiver: MessageChannel, content: MessageContent) {
        if (sender.type != receiver.type) {
            throw MessageChannelTypesMismatch("sender and receiver both need the same message channel")
        }

        val senderQueue = this.findMessageQueue(sender) ?: throw QueueNotFound("sender queue not found")
        val receiverQueue = this.findMessageQueue(receiver) ?: throw QueueNotFound("receiver queue not found")

        senderQueue.push(
            Message(
                content = content,
                from = null,
                to = receiver,
            )
        )
        receiverQueue.push(
            Message(
                content = content,
                from = sender,
                to = null
            )
        )
    }

    private fun findMessageQueue(channel: MessageChannel): MessageQueue? {
        return this.messageQueueRepo.findById(channel.id)
    }
}

class QueueNotFound(override val message: String) : TypedThrowable("QUEUE_NOT_FOUND", message)

class MessageChannelTypesMismatch(override val message: String) :
    TypedThrowable("MESSAGE_CHANNEL_TYPES_MISMATCH", message)



