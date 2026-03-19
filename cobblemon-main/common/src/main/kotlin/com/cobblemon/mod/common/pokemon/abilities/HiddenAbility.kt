/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.abilities

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.abilities.AbilityTemplate
import com.cobblemon.mod.common.api.abilities.PotentialAbility
import com.cobblemon.mod.common.api.abilities.PotentialAbilityType
import com.google.gson.JsonElement

object HiddenAbilityType : PotentialAbilityType<HiddenAbility> {
    override fun parseFromJSON(element: JsonElement): HiddenAbility? {
        val str = if (element.isJsonPrimitive) element.asString else null
        return if (str?.startsWith("h:") == true) {
            val abilityString = str.substringAfter("h:")
            val ability = Abilities.get(abilityString)
            if (ability != null) {
                HiddenAbility(ability)
            } else {
                Cobblemon.LOGGER.error("Hidden ability referred to unknown ability: $abilityString")
                null
            }
        } else {
            null
        }
    }
}

/**
 * Crappy Pokémon feature
 *
 * @author Hiroku
 * @since July 28th, 2022
 */
class HiddenAbility(override val template: AbilityTemplate) : PotentialAbility {
    override val priority: Priority = Priority.LOW
    override val type = HiddenAbilityType
    override fun isSatisfiedBy(aspects: Set<String>) = false // TODO actually implement hidden abilities ig? Chance in config or aspect check?
}