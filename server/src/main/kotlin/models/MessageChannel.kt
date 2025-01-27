package models

data class MessageChannel(
    val type: MessageChannelType,
    val id: Long
)

enum class MessageChannelType {
    PUBLIC_NEAR,
    WHISPER,
}
