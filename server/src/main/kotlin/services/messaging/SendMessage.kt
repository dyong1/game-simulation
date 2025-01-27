package services.messaging

import models.*
import throwables.TypedThrowable

class SendMessage(
    private val messageQueueRepo: MessageQueueRepository
) {
    fun command(
        type: MessageChannelType,
        content: String,
        senderId: Long,
        receiverId: Long?
    ) {
        val (sender, receiver) = when (type) {
            MessageChannelType.PUBLIC_NEAR -> {
                Pair(
                    MessageChannel(
                        type,
                        senderId,
                    ), MessageChannel(
                        type,
                        0L,
                    )
                )
            }

            MessageChannelType.WHISPER -> {
                if (receiverId == null) {
                    throw TypedThrowable("MISSING_RECEIVER_ID", "missing receiver id")
                }
                Pair(
                    MessageChannel(
                        type,
                        senderId,
                    ), MessageChannel(
                        type,
                        receiverId,
                    )
                )
            }
        }

        val senderQueue = this.findMessageQueue(sender) ?: throw QueueNotFound("sender queue not found")
        val receiverQueue = this.findMessageQueue(receiver) ?: throw QueueNotFound("receiver queue not found")

        senderQueue.push(
            Message(
                content = MessageContent(content),
                from = null,
                to = receiver,
            )
        )
        receiverQueue.push(
            Message(
                content = MessageContent(content),
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



