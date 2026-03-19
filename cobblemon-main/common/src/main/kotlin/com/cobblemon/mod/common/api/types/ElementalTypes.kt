/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.types

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

/**
 * Registry for all known ElementalTypes
 */
object ElementalTypes {

    private val allTypes = mutableListOf<ElementalType>()

    @JvmField
    val NORMAL = register(
        name = "Normal",
        displayName = Component.translatable("cobblemon.type.normal"),
        hue = 0xE8E8DA,
        textureXMultiplier = 0
    )

    @JvmField
    val FIRE = register(
        name = "Fire",
        displayName = Component.translatable("cobblemon.type.fire"),
        hue = 0xFF6E21,
        textureXMultiplier = 1
    )

    @JvmField
    val WATER = register(
        name = "Water",
        displayName = Component.translatable("cobblemon.type.water"),
        hue = 0x3FA5FF,
        textureXMultiplier = 2
    )

    @JvmField
    val GRASS = register(
        name = "Grass",
        displayName = Component.translatable("cobblemon.type.grass"),
        hue = 0x62D14F,
        textureXMultiplier = 3
    )

    @JvmField
    val ELECTRIC = register(
        name = "Electric",
        displayName = Component.translatable("cobblemon.type.electric"),
        hue = 0xFFD314,
        textureXMultiplier = 4
    )

    @JvmField
    val ICE = register(
        name = "Ice",
        displayName = Component.translatable("cobblemon.type.ice"),
        hue = 0x54F2F2,
        textureXMultiplier = 5
    )

    @JvmField
    val FIGHTING = register(
        name = "Fighting",
        displayName = Component.translatable("cobblemon.type.fighting"),
        hue = 0xEF565D,
        textureXMultiplier = 6
    )

    @JvmField
    val POISON = register(
        name = "Poison",
        displayName = Component.translatable("cobblemon.type.poison"),
        hue = 0xD651FF,
        textureXMultiplier = 7
    )

    @JvmField
    val GROUND = register(
        name = "Ground",
        displayName = Component.translatable("cobblemon.type.ground"),
        hue = 0xF4A453,
        textureXMultiplier = 8
    )

    @JvmField
    val FLYING = register(
        name = "Flying",
        displayName = Component.translatable("cobblemon.type.flying"),
        hue = 0xB8B2FF,
        textureXMultiplier = 9
    )

    @JvmField
    val PSYCHIC = register(
        name = "Psychic",
        displayName = Component.translatable("cobblemon.type.psychic"),
        hue = 0xFF5E9E,
        textureXMultiplier = 10
    )

    @JvmField
    val BUG = register(
        name = "Bug",
        displayName = Component.translatable("cobblemon.type.bug"),
        hue = 0xD3D319,
        textureXMultiplier = 11
    )

    @JvmField
    val ROCK = register(
        name = "Rock",
        displayName = Component.translatable("cobblemon.type.rock"),
        hue = 0xB7A16E,
        textureXMultiplier = 12
    )

    @JvmField
    val GHOST = register(
        name = "Ghost",
        displayName = Component.translatable("cobblemon.type.ghost"),
        hue = 0x9C80F7,
        textureXMultiplier = 13
    )

    @JvmField
    val DRAGON = register(
        name = "Dragon",
        displayName = Component.translatable("cobblemon.type.dragon"),
        hue = 0x7580FF,
        textureXMultiplier = 14
    )

    @JvmField
    val DARK = register(
        name = "Dark",
        displayName = Component.translatable("cobblemon.type.dark"),
        hue = 0x587DA0,
        textureXMultiplier = 15
    )

    @JvmField
    val STEEL = register(
        name = "Steel",
        displayName = Component.translatable("cobblemon.type.steel"),
        hue = 0xABD1F4,
        textureXMultiplier = 16
    )

    @JvmField
    val FAIRY = register(
        name = "Fairy",
        displayName = Component.translatable("cobblemon.type.fairy"),
        hue = 0xFF7FE5,
        textureXMultiplier = 17
    )

    @JvmStatic
    fun register(name: String, displayName: MutableComponent, hue: Int, textureXMultiplier: Int): ElementalType {
        return ElementalType(
            name = name,
            displayName = displayName,
            hue = hue,
            textureXMultiplier = textureXMultiplier
        ).also {
            allTypes.add(it)
        }
    }

    @JvmStatic
    fun register(elementalType: ElementalType): ElementalType {
        allTypes.add(elementalType)
        return elementalType
    }

    @JvmStatic
    fun get(name: String): ElementalType? {
        return allTypes.firstOrNull { type -> type.name.equals(name, ignoreCase = true) }
    }

    @JvmStatic
    fun getOrException(name: String): ElementalType {
        return allTypes.first { type -> type.name.equals(name, ignoreCase = true) }
    }

    @JvmStatic
    fun count() = allTypes.size

    @JvmStatic
    fun all() = this.allTypes.toList()

    @JvmStatic
    fun getRandomType(): ElementalType = this.allTypes.random()

    /**
     * Backwards compatibility getters
     */
    @JvmName("getNORMAL")
    @Deprecated("Use ElementalTypes.NORMAL, provided for backwards compatibility until Cobblemon 1.8.")
    fun getNORMAL() = NORMAL

    @JvmName("getFIRE")
    @Deprecated("Use ElementalTypes.FIRE, provided for backwards compatibility until Cobblemon 1.8.")
    fun getFIRE() = FIRE

    @JvmName("getWATER")
    @Deprecated("Use ElementalTypes.WATER, provided for backwards compatibility until Cobblemon 1.8.")
    fun getWATER() = WATER

    @JvmName("getGRASS")
    @Deprecated("Use ElementalTypes.GRASS, provided for backwards compatibility until Cobblemon 1.8.")
    fun getGRASS() = GRASS

    @JvmName("getELECTRIC")
    @Deprecated("Use ElementalTypes.ELECTRIC, provided for backwards compatibility until Cobblemon 1.8.")
    fun getELECTRIC() = ELECTRIC

    @JvmName("getICE")
    @Deprecated("Use ElementalTypes.ICE, provided for backwards compatibility until Cobblemon 1.8.")
    fun getICE() = ICE

    @JvmName("getFIGHTING")
    @Deprecated("Use ElementalTypes.FIGHTING, provided for backwards compatibility until Cobblemon 1.8.")
    fun getFIGHTING() = FIGHTING

    @JvmName("getPOISON")
    @Deprecated("Use ElementalTypes.POISON, provided for backwards compatibility until Cobblemon 1.8.")
    fun getPOISON() = POISON

    @JvmName("getGROUND")
    @Deprecated("Use ElementalTypes.GROUND, provided for backwards compatibility until Cobblemon 1.8.")
    fun getGROUND() = GROUND

    @JvmName("getFLYING")
    @Deprecated("Use ElementalTypes.FLYING, provided for backwards compatibility until Cobblemon 1.8.")
    fun getFLYING() = FLYING

    @JvmName("getPSYCHIC")
    @Deprecated("Use ElementalTypes.PSYCHIC, provided for backwards compatibility until Cobblemon 1.8.")
    fun getPSYCHIC() = PSYCHIC

    @JvmName("getBUG")
    @Deprecated("Use ElementalTypes.BUG, provided for backwards compatibility until Cobblemon 1.8.")
    fun getBUG() = BUG

    @JvmName("getROCK")
    @Deprecated("Use ElementalTypes.ROCK, provided for backwards compatibility until Cobblemon 1.8.")
    fun getROCK() = ROCK

    @JvmName("getGHOST")
    @Deprecated("Use ElementalTypes.GHOST, provided for backwards compatibility until Cobblemon 1.8.")
    fun getGHOST() = GHOST

    @JvmName("getDRAGON")
    @Deprecated("Use ElementalTypes.DRAGON, provided for backwards compatibility until Cobblemon 1.8.")
    fun getDRAGON() = DRAGON

    @JvmName("getDARK")
    @Deprecated("Use ElementalTypes.DARK, provided for backwards compatibility until Cobblemon 1.8.")
    fun getDARK() = DARK

    @JvmName("getSTEEL")
    @Deprecated("Use ElementalTypes.STEEL, provided for backwards compatibility until Cobblemon 1.8.")
    fun getSTEEL() = STEEL

    @JvmName("getFAIRY")
    @Deprecated("Use ElementalTypes.FAIRY, provided for backwards compatibility until Cobblemon 1.8.")
    fun getFAIRY() = FAIRY
}