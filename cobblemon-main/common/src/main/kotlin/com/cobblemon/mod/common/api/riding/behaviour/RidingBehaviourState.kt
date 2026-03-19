/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour

import com.cobblemon.mod.common.util.ifClient
import com.cobblemon.mod.common.util.ifServer
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.phys.Vec3

/**
 * Represents the state of a Pokemon when being ridden.
 * This is intended to contain mutable state that is passed between the client and server
 * or temporary state that is used during the riding process.
 *
 * This will be destroyed when the main rider dismounts the Pokemon.
 *
 * @author landonjw
 */
open class RidingBehaviourState {
    open val rideVelocity: SidedRidingState<Vec3> = ridingState(Vec3.ZERO, Side.CLIENT)
    open val stamina: SidedRidingState<Float> = ridingState(1.0F, Side.CLIENT)

    open fun reset() {
        rideVelocity.set(Vec3.ZERO, true)
        stamina.set(1.0F, true)
    }

    open fun copy(): RidingBehaviourState {
        val copy = RidingBehaviourState()
        copy.rideVelocity.set(rideVelocity.get(), true)
        copy.stamina.set(stamina.get(), true)
        return copy
    }

    open fun shouldSync(previous: RidingBehaviourState): Boolean {
        if (previous.rideVelocity.get() != rideVelocity.get()) return true
        if (previous.stamina.get() != stamina.get()) return true
        return false
    }

    open fun encode(buffer: FriendlyByteBuf) {
        buffer.writeVec3(rideVelocity.get())
        buffer.writeFloat(stamina.get())
    }

    open fun decode(buffer: FriendlyByteBuf) {
        rideVelocity.set(buffer.readVec3(), true)
        stamina.set(buffer.readFloat(), true)
    }
}

fun <T> ridingState(value: T, side: Side) = SidedRidingState(value, side)

class SidedRidingState<T>(private var value: T, val side: Side) {
    fun get() = value

    fun set(value: T, forced: Boolean = false) {
        if (forced) {
            this.value = value
        }
        else {
            when (side) {
                Side.BOTH -> { this.value = value }
                Side.CLIENT -> { ifClient { this.value = value } }
                Side.SERVER -> { ifServer { this.value = value } }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is SidedRidingState<*>) {
            return value == other.value
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

enum class Side {
    SERVER, CLIENT, BOTH
}

enum class DriverSide {
    LOCAL, REMOTE, BOTH
}
