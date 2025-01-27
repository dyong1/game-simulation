package services.messaging

import io.lettuce.core.RedisClient
import org.springframework.stereotype.Component

interface MessageQueueRepository {
    fun findById(id: Long): MessageQueue?
}

@Component
class RedisMessageQueueRepository(
    private val redis: RedisClient
): MessageQueueRepository{
    override fun findById(id: Long): MessageQueue? {
        return KafkaRedisMessageQueue("message_queue_$id")
    }

}