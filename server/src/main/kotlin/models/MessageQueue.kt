package models

interface MessageQueue {
    fun push(message: Message)
    fun consume(count: Int): List<Message>
}