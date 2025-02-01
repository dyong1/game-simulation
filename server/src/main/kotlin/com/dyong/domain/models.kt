package com.dyong.domain

import com.dyong.network.UserClient
import kotlinx.serialization.Serializable

@Serializable
data class Position2D(
    var x: Long,
    var y: Long
)

data class DroppedItem(
    val itemId: Long,
    val position: Position2D,
)


data class DroppableItem(
    val itemId: Long
)

data class Level(
    val id: Long,
    val droppedItems: MutableList<DroppedItem>
)

class Player(
    val client: UserClient,
    val userId: Long,
    var position: Position2D,
    var currentLevelId: Long,
    var experience: Long
)

class Monster(
    val id: Long,
    var life: Long,
    val experience: Long,
    val droppables: List<DroppableItem>,
    val position: Position2D
) {
    fun dropItem(): DroppedItem? {
        return droppables.shuffled().getOrNull(0)?.let {
            DroppedItem(
                itemId = it.itemId,
                position = position,
            )
        }
    }
}


