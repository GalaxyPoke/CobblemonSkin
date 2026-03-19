/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.configuration

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.readText
import com.cobblemon.mod.common.util.writeString
import com.cobblemon.mod.common.util.writeText
import com.mojang.datafixers.util.Either
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component

/**
 * A predefined variable that will be declared and filled in on any assigned entities. This gives the client
 * a cleaner way to represent a variable that should exist on the entity.
 *
 * @author Hiroku
 * @since August 14th, 2023
 */
class MoLangConfigVariable(
    val variableName: String = "variable",
    val category: Component = "Variables".asTranslated(),
    val displayName: Component = "Variable".asTranslated(),
    val description: Component = "A variable that can be used in the entity's configuration.".asTranslated(),
    val type: MoLangVariableType = MoLangVariableType.NUMBER,
    val defaultValue: String = "0",
) {
    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf): MoLangConfigVariable {
            return MoLangConfigVariable(
                buffer.readString(),
                buffer.readText(),
                buffer.readText(),
                buffer.readText(),
                buffer.readEnum(MoLangVariableType::class.java),
                buffer.readString()
            )
        }
    }

    fun asExpressible(): ExpressionOrEntityVariable = Either.right(this)

    enum class MoLangVariableType {
        NUMBER,
        TEXT,
        BOOLEAN;

        fun toMoValue(value: String): MoValue {
            return when (this) {
                TEXT -> StringValue(value)
                BOOLEAN -> DoubleValue(value.let { it == "1" || it.toBooleanStrictOrNull() == true  })
                else -> DoubleValue(value.toDouble())
            }
        }
    }

    fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(variableName)
        buffer.writeText(category)
        buffer.writeText(displayName)
        buffer.writeText(description)
        buffer.writeEnum(type)
        buffer.writeString(defaultValue)
    }
}