/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.components

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.FastColor
import net.minecraft.world.item.DyeColor

/**
 * This component contains a list of [DyeColor] picked up from cooking.
 *
 * @author Hiroku
 * @since March 20th, 2025
 */
class FoodColourComponent(
    val colours: List<DyeColor>
) {
    companion object {
        val CODEC: Codec<FoodColourComponent> = DyeColor.CODEC.listOf().xmap(::FoodColourComponent, FoodColourComponent::colours)
        val PACKET_CODEC: StreamCodec<ByteBuf, FoodColourComponent> = ByteBufCodecs.fromCodec(CODEC)

        private val COLORS = mapOf<DyeColor, Int>(
            DyeColor.WHITE to FastColor.ARGB32.color(255, 255, 255, 255),
            DyeColor.ORANGE to FastColor.ARGB32.color(255, 255, 195, 175),
            DyeColor.MAGENTA to FastColor.ARGB32.color(255, 175, 140, 255),
            DyeColor.LIGHT_BLUE to FastColor.ARGB32.color(255, 120, 195, 235),
            DyeColor.YELLOW to FastColor.ARGB32.color(255, 255, 225, 175),
            DyeColor.PINK to FastColor.ARGB32.color(255, 225, 160, 255),
            DyeColor.LIME to FastColor.ARGB32.color(255, 205, 255, 175),
            DyeColor.GRAY to FastColor.ARGB32.color(255, 71, 79, 82),
            DyeColor.LIGHT_GRAY to FastColor.ARGB32.color(255, 157, 157, 151),
            DyeColor.CYAN to FastColor.ARGB32.color(255, 135, 235, 215),
            DyeColor.PURPLE to FastColor.ARGB32.color(255, 145, 145, 255),
            DyeColor.BLUE to FastColor.ARGB32.color(255, 120, 165, 255),
            DyeColor.BROWN to FastColor.ARGB32.color(255, 131, 84, 50),
            DyeColor.GREEN to FastColor.ARGB32.color(255, 175, 255, 180),
            DyeColor.RED to FastColor.ARGB32.color(255, 255, 175, 215),
            DyeColor.BLACK to FastColor.ARGB32.color(255, 0, 0, 0)
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is FoodColourComponent && colours == other.colours // List == List in kotlin checks for element order and index.
    }

    fun getColoursAsARGB(): List<Int> {
        return colours.map { dye -> COLORS[dye] ?: COLORS[DyeColor.WHITE]!! }
    }

    override fun hashCode() = colours.hashCode()
}