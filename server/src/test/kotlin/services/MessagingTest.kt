package services

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import models.*
import org.junit.platform.suite.api.Suite
import services.messaging.MessageQueueRepository
import services.messaging.SendMessage

@Suite
class MessagingTest : AnnotationSpec() {
    @Test
    fun `sent message is stored both of sender and receiver message queues`() {
        class MockMessageQueue : MessageQueue {
            val messages = ArrayList<Message>()
            var cursor = 0
            override fun push(message: Message) {
                messages.add(message)
            }
            override fun consume(count: Int): List<Message> {
                val mm = messages.slice(cursor..cursor + count)
                cursor += count
                return mm
            }
        }
        class MockMessageQueueRepo : MessageQueueRepository {
            val senderQueue = MockMessageQueue()
            val receiverQueue = MockMessageQueue()
            override fun findById(id: Long): MessageQueue? {
                return when (id) {
                    1L -> senderQueue
                    2L -> receiverQueue
                    else -> throw RuntimeException()
                }
            }
        }
        val repo = MockMessageQueueRepo()
        val sendMessage = SendMessage(
            messageQueueRepo = repo
        )
        val sender = MessageChannel(
            type = MessageChannelType.WHISPER,
            id = 1L,
        )
        val receiver = MessageChannel(
            type = MessageChannelType.WHISPER,
            id = 2L,
        )
        val content = MessageContent(
            plainText = "plainText"
        )
        sendMessage.command(
            sender,
            receiver,
            content,
        )
        repo.senderQueue.messages.last.content.plainText shouldBe "plainText"
        repo.receiverQueue.messages.last.content.plainText shouldBe "plainText"
        repo.receiverQueue.messages.last.from?.id shouldBe 1L
    }
}