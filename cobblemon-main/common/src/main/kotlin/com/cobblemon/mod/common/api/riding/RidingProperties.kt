/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.util.*
import com.cobblemon.mod.common.util.adapters.RidingBehaviourSettingsAdapter
import net.minecraft.network.RegistryFriendlyByteBuf

class RidingProperties(
        val seats: List<Seat> = listOf(),
        val conditions: List<Expression> = listOf(),
        val behaviours: Map<RidingStyle, RidingBehaviourSettings>? = null
) {

    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf): RidingProperties {
            val seats: List<Seat> = buffer.readList { _ -> Seat.decode(buffer) }
            val conditions = buffer.readList { buffer.readString().asExpression() }
            val behaviours = buffer.readNullable { _ ->
                buffer.readMap(
                    { buffer.readEnumConstant(RidingStyle::class.java) },
                    {
                        val key = buffer.readResourceLocation()
                        val behaviour = RidingBehaviourSettingsAdapter.types[key]?.getConstructor()?.newInstance()
                            ?: error("Unknown controller key: $key")
                        behaviour.decode(buffer)
                        behaviour
                    }
                )
            }

            return RidingProperties(seats = seats, conditions = conditions, behaviours = behaviours)
        }
    }

    fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(seats) { _, seat -> seat.encode(buffer) }
        buffer.writeCollection(conditions) { _, condition -> buffer.writeString(condition.getString()) }
        buffer.writeNullable(behaviours) { _, v ->
            buffer.writeMap(
                v,
                { _, style -> buffer.writeEnumConstant(style) },
                { _, behaviour ->
                    buffer.writeResourceLocation(behaviour.key)
                    behaviour.encode(buffer)
                }
            )
        }
    }
}