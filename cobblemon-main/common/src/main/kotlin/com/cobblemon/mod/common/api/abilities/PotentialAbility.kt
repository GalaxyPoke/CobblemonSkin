/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.abilities

import com.cobblemon.mod.common.api.Priority
import com.google.gson.JsonElement

interface PotentialAbilityType<T : PotentialAbility> {
    fun parseFromJSON(element: JsonElement): T?
}

/**
 * An ability on a species that may or may not be available for a specific instance of the species.
 *
 * Controls whether a Pokémon can learn an ability.
 *
 * @author Hiroku
 * @since July 27th, 2022
 */
interface PotentialAbility {
    val template: AbilityTemplate
    val priority: Priority
    val type: PotentialAbilityType<*>
    fun isSatisfiedBy(aspects: Set<String>): Boolean
    companion object {
        val types = mutableListOf<PotentialAbilityType<*>>()
    }
}

object CommonAbilityType : PotentialAbilityType<CommonAbility> {
    override fun parseFromJSON(element: JsonElement): CommonAbility? {
        val str = if (element.isJsonPrimitive) element.asString else null
        return str?.let {
            val ability = Abilities.get(it)
            if (ability != null) {
                return@let CommonAbility(ability)
            } else {
                return@let null
            }
        }
    }
}

open class CommonAbility(override val template: AbilityTemplate) : PotentialAbility {
    override val priority = Priority.LOWEST
    override val type = CommonAbilityType
    override fun isSatisfiedBy(aspects: Set<String>) = true
}

