/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.cooking

import com.cobblemon.mod.common.api.riding.stats.RidingStat
import net.minecraft.util.FastColor
import net.minecraft.world.item.ItemStack

private val colourMap = mapOf(
    Flavour.SPICY to 0xFEB37D,
    Flavour.DRY to 0x8AE9FC,
    Flavour.SWEET to 0xFFBEED,
    Flavour.BITTER to 0x9EED8F,
    Flavour.SOUR to 0xFCF38A
)

private val bubbleColourMap = mapOf(
    Flavour.SPICY to 0xFFD9AD,
    Flavour.DRY to 0xBCF8FE,
    Flavour.SWEET to 0xFEE3F9,
    Flavour.BITTER to 0xC8F7BC,
    Flavour.SOUR to 0xFDFAB8
)

fun getColourMixFromSeasonings(seasonings: List<ItemStack>, forBubbles: Boolean = false): Int? {
    val flavourValues = mutableMapOf<Flavour, Int>()
    for (seasoning in seasonings) {
        val flavours = Seasonings.getFlavoursFromItemStack(seasoning) ?: continue
        for ((flavour, value) in flavours.entries) {
            flavourValues[flavour] = (flavourValues[flavour] ?: 0) + value
        }
    }

    if (flavourValues.isEmpty()) {
        return null
    }

    val maxFlavorValue = flavourValues.values.maxOrNull()
    val dominantFlavours = flavourValues.filter { it.value == maxFlavorValue }.map { it.key }

    return getColourMixFromFlavours(dominantFlavours, forBubbles)
}

fun getColourMixFromFlavours(dominantFlavours: List<Flavour>, forBubbles: Boolean = false): Int? {
    val colors =
        dominantFlavours
            .mapNotNull { if (forBubbles) bubbleColourMap[it] else colourMap[it] }
            .map { FastColor.ARGB32.opaque(it) }

    return getColourMixFromColors(colors)
}

fun getColourMixFromRideStatBoosts(dominantBoosts: Iterable<RidingStat>, forBubbles: Boolean = false): Int? {
    return getColourMixFromFlavours(dominantBoosts.map { it.flavour }, forBubbles)
}

fun getColourMixFromColors(colors: List<Int>): Int? {
    if (colors.isEmpty()) return null
    if (colors.size == 1) return colors[0]

    val firstWeight = 0.7f
    val otherWeightTotal = 1f - firstWeight
    val otherWeight = otherWeightTotal / (colors.size - 1)

    var alphaSum = 0f
    var redSum = 0f
    var greenSum = 0f
    var blueSum = 0f

    for (i in colors.indices) {
        val weight = if (i == 0) firstWeight else otherWeight
        val color = colors[i]

        alphaSum += FastColor.ARGB32.alpha(color) * weight
        redSum += FastColor.ARGB32.red(color) * weight
        greenSum += FastColor.ARGB32.green(color) * weight
        blueSum += FastColor.ARGB32.blue(color) * weight
    }

    return FastColor.ARGB32.color(
        alphaSum.toInt(),
        redSum.toInt(),
        greenSum.toInt(),
        blueSum.toInt()
    )
}