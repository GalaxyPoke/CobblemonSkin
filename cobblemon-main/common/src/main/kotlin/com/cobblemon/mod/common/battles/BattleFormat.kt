/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.getStringOrNull
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeSizedInt
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Rules around how a battle is going to work.
 *
 * @author Hiroku
 * @since March 9th, 2022
 */
data class BattleFormat(
    val mod: String = "cobblemon",
    val battleType: BattleType = BattleTypes.SINGLES,
    var ruleSet: Set<String> = setOf(),
    val gen: Int = 9,
    var adjustLevel: Int = -1, // Stop gap rule before a more general system for rules enforced by Cobblemon is implemented.
) {
    companion object {
        fun setBattleRules(
            battleFormat: BattleFormat,
            rules: Set<String>? = null
        ): BattleFormat {
            val filteredRules = rules?.filter { it.isNotBlank() }?.toSet()
            val ruleValues = filteredRules?.mapNotNull { ruleName ->
                BattleRules::class.members
                    .filterIsInstance<kotlin.reflect.KProperty1<BattleRules, String>>()
                    .find { it.name == ruleName }
                    ?.getter
                    ?.call()
            }.orEmpty()

            return battleFormat.copy(
                ruleSet = filteredRules ?: (battleFormat.ruleSet + ruleValues)
            )
        }

        fun fromFormatIdentifier(id: String): BattleFormat = when (id) {
            "single_battle", "single", "singles" -> BattleFormat.GEN_9_SINGLES
            "double_battle", "double", "doubles" -> BattleFormat.GEN_9_DOUBLES
            "triple_battle", "triple", "triples" -> BattleFormat.GEN_9_TRIPLES
            else ->  BattleFormat.GEN_9_SINGLES
        }

        val GEN_9_SINGLES = BattleFormat(
            battleType = BattleTypes.SINGLES,
            ruleSet = mutableSetOf(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)
        )

        val GEN_9_DOUBLES = BattleFormat(
            battleType = BattleTypes.DOUBLES,
            ruleSet = mutableSetOf(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)
        )

        val GEN_9_TRIPLES = BattleFormat(
                battleType = BattleTypes.TRIPLES,
                ruleSet = mutableSetOf(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)
        )

        val GEN_9_MULTI = BattleFormat(
            battleType = BattleTypes.MULTI,
            ruleSet = mutableSetOf(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)
        )

        val GEN_9_ROYAL = BattleFormat(
            battleType = BattleTypes.ROYAL,
            ruleSet = mutableSetOf(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE)
        )

        fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): BattleFormat {
            val mod = buffer.readString()
            val battleType = BattleType.loadFromBuffer(buffer)
            val ruleSet = mutableSetOf<String>()
            repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) { ruleSet.add(buffer.readString()) }
            val adjustLevel = buffer.readSizedInt(IntSize.INT)
            return BattleFormat(
                mod = mod,
                battleType = battleType,
                ruleSet = ruleSet,
                adjustLevel = adjustLevel
            )
        }
    }

    fun saveToBuffer(buffer: RegistryFriendlyByteBuf): RegistryFriendlyByteBuf {
        buffer.writeString(mod)
        battleType.saveToBuffer(buffer)
        buffer.writeSizedInt(IntSize.U_BYTE, ruleSet.size)
        ruleSet.forEach(buffer::writeString)
        buffer.writeSizedInt(IntSize.INT, adjustLevel)
        return buffer
    }

    fun toFormatJSON(): String {
        /*
          Bag clause does not exist in showdown as a rule because showdown simply has no concept of a 'bag'.
          If we try sending it, the battle will error. So this simply omits that from ever getting sent.
          If we ever find a case where we are doing this for more than just bag clause, we should make a proper abstraction.
         */
        val rulesToSendToShowdown = ruleSet.filter { it != BattleRules.BAG_CLAUSE }
        return """
            {
                "mod": "$mod",
                "gameType": "${battleType.name}",
                "gen": $gen,
                "ruleset": [${rulesToSendToShowdown.joinToString { "\"$it\"" }}],
                "effectType": "Format"
            }
        """.trimIndent().replace("\n", "")
    }
}