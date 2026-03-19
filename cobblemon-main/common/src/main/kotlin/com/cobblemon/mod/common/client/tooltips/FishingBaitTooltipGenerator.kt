/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.api.fishing.SpawnBait
import com.cobblemon.mod.common.api.fishing.SpawnBaitEffects
import com.cobblemon.mod.common.api.fishing.SpawnBaitUtils
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.text.blue
import com.cobblemon.mod.common.api.text.gold
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.obfuscate
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.item.interactive.PokerodItem
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.util.lang
import java.text.DecimalFormat
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object FishingBaitTooltipGenerator : TooltipGenerator() {
    private val fishingBaitItemClass by lazy { lang("item_class.fishing_bait").blue() }

    override fun generateCategoryTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        if (!SpawnBaitEffects.isFishingBait(stack)) {
            return null
        }
        return mutableListOf(fishingBaitItemClass)
    }

    override fun generateAdditionalTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        val resultLines = mutableListOf<Component>()

        if (SpawnBaitEffects.isFishingBait(stack)) resultLines.add(this.fishingBaitItemClass)

        return resultLines
    }

}