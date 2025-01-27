package services.messaging

import models.MessageQueue

interface MessageQueueRepository {
    fun findById(id: Long): MessageQueue?
}