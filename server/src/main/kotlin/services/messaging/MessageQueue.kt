package services.messaging

import KafkaConsumerConfig
import models.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.MessageListener
import org.springframework.stereotype.Component

interface MessageQueue {
    fun push(message: Message)
    fun consume(callback: (Message) -> Unit)
    fun resetConsumer()
}


@Component
class KafkaRedisMessageQueue : MessageQueue {
    private val TOPIC = "messages"

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Message>
    @Autowired
    private lateinit var dynamicConfig: KafkaConsumerConfig

    private var callback: ((Message) -> Unit)? = null

    constructor(
        queueId: String,
    ) {
        val factory = dynamicConfig.kafkaListenerContainerFactory(queueId)
        val listener = factory.createContainer(TOPIC)
        listener.containerProperties.messageListener = MessageListener<String, Message> {
            this.callback?.invoke(it.value())
        }
    }

    override fun push(message: Message) {
        this.kafkaTemplate.send(TOPIC, message)
    }

    override fun consume(callback: (Message) -> Unit) {
        this.callback = callback
    }

    override fun resetConsumer() {
        this.callback = null
    }
}