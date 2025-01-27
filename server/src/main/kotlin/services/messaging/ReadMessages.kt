package services.messaging

import models.Message
import models.MessageChannel


class ReadMessages(
    private val messageQueueRepository: MessageQueueRepository

) {
    fun query(reader: MessageChannel, count: Int): List<Message> {
        val queue = this.messageQueueRepository.findById(reader.id) ?: throw QueueNotFound("reader queue not found")
        queue.consume {  }
        return emptyList()
    }
}