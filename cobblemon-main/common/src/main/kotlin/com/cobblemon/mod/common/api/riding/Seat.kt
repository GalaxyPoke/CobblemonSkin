/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding

import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.readNullable
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeCollection
import com.cobblemon.mod.common.util.writeNullable
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.phys.Vec3

/**
 * Seat Properties are responsible for the base information that would then be used to construct a Seat on an entity.
 */
data class Seat(
    val locator: String?,
    val offset: Vec3?,
    val poseOffsets: MutableList<SeatPoseOffset>?,
    val poseAnimations: MutableList<SeatPoseAnimations>?,
    val handsBusy: Boolean = false,
) : Encodable {
    fun getOffset(poseType: PoseType) : Vec3 {
        return poseOffsets?.firstOrNull { poseType in it.poseTypes }?.offset ?: Vec3.ZERO
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeNullable(locator) { _, v -> buffer.writeString(v) }
        buffer.writeNullable(offset) { _, v -> buffer.writeVec3(v) }
        buffer.writeNullable(poseOffsets) { _, v ->
            buffer.writeCollection(v) { _, offset -> offset.encode(buffer) }
        }
        buffer.writeNullable(poseAnimations) { _, v ->
            buffer.writeCollection(v) { _, animations -> animations.encode(buffer) }
        }
        buffer.writeBoolean(handsBusy)
    }

    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf) : Seat {
            return Seat(
                buffer.readNullable { buffer.readString() },
                buffer.readNullable { buffer.readVec3() },
                buffer.readNullable { buffer.readList { SeatPoseOffset.decode(buffer) } },
                buffer.readNullable { buffer.readList { SeatPoseAnimations.decode(buffer) } },
                buffer.readBoolean()
            )
        }
    }
}

class SeatPoseOffset {
    val poseTypes = mutableSetOf<PoseType>()
    var offset = Vec3.ZERO

    fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(poseTypes) { _, poseType -> buffer.writeString(poseType.name) }
        buffer.writeDouble(offset.x)
        buffer.writeDouble(offset.y)
        buffer.writeDouble(offset.z)
    }

    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf) : SeatPoseOffset {
            val offset = SeatPoseOffset()
            offset.poseTypes.addAll(buffer.readList { buffer.readString().let { PoseType.valueOf(it) } })
            offset.offset = Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())
            return offset
        }
    }
}

class SeatPoseAnimations {
    val poseTypes = mutableSetOf<PoseType>()
    var animations = mutableListOf<RidingAnimation>()

    fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(poseTypes) { _, poseType -> buffer.writeString(poseType.name) }
        buffer.writeCollection(animations) { _, animation -> animation.encode(buffer) }
    }

    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf) : SeatPoseAnimations {
            val animations = SeatPoseAnimations()
            animations.poseTypes.addAll(buffer.readList { buffer.readString().let { PoseType.valueOf(it) } })
            animations.animations = buffer.readList { RidingAnimation.decode(buffer) }
            return animations
        }
    }
}
