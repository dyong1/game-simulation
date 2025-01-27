package models

data class MessageContent(
    val plainText: String,
)

data class Message(val content: MessageContent, val from: MessageChannel?, val to: MessageChannel?)