/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.tags

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey

/**
 * A collection of the Cobblemon [TagKey]s related to the [Registries.BLOCK].
 *
 * @author Licious
 * @since October 29th, 2022
 */
@Suppress("HasPlatformType", "unused")
object CobblemonBlockTags {

    @JvmField val ALL_HANGING_SIGNS = createTag("all_hanging_signs")
    @JvmField val ALL_SAPLINGS = createTag("all_saplings")
    @JvmField val ALL_SIGNS = createTag("all_signs")
    @JvmField val ANCIENT_CITY_BLOCKS = createTag("ancient_city_blocks")
    @JvmField val APRICORN_LEAVES = createTag("apricorn_leaves")
    @JvmField val APRICORN_LOGS = createTag("apricorn_logs")
    @JvmField val APRICORN_SAPLINGS = createTag("apricorn_saplings")
    @JvmField val APRICORNS = createTag("apricorns")
    @JvmField val BERRIES = createTag("berries")
    @JvmField val BERRY_REPLACEABLE = createTag("berry_replaceable")
    @JvmField val BERRY_SOIL = createTag("berry_soil")
    @JvmField val BERRY_WILD_SOIL = createTag("berry_wild_soil")
    @JvmField val BLACK_TUMBLESTONE_BRICKS = createTag("black_tumblestone_bricks")
    @JvmField val BLACK_TUMBLESTONES = createTag("black_tumblestones")
    @JvmField val BLUE_FLOWERS = createTag("blue_flowers")
    @JvmField val CEILING_HANGING_SIGNS = createTag("ceiling_hanging_signs")
    @JvmField val CROPS = createTag("crops")
    @JvmField val DAWN_STONE_ORES = createTag("dawn_stone_ores")
    @JvmField val DESERT_PYRAMID_BLOCKS = createTag("desert_pyramid_blocks")
    @JvmField val DRIPSTONE_GROWABLE = createTag("dripstone_growable")
    @JvmField val DRIPSTONE_REPLACEABLES = createTag("dripstone_replaceables")
    @JvmField val DUSK_STONE_ORES = createTag("dusk_stone_ores")
    @JvmField val END_CITY_BLOCKS = createTag("end_city_blocks")
    @JvmField val EVOLUTION_STONE_BLOCKS = createTag("evolution_stone_blocks")
    @JvmField val FIRE_STONE_ORES = createTag("fire_stone_ores")
    @JvmField val FLOWERS = createTag("flowers")
    @JvmField val FOSSIL_MACHINE_PARTS = createTag("fossil_machine_parts")
    @JvmField val GALARICA_NUT_MAY_PLACE_ON = createTag("galarica_nut_may_place_on")
    @JvmField val GEMSTONES = createTag("gemstones")
    @JvmField val GILDED_CHESTS = createTag("gilded_chests")
    @JvmField val GLAZED_TERRACOTTA_BLOCKS = createTag("glazed_terracotta_blocks")
    @JvmField val ICE_STONE_ORES = createTag("ice_stone_ores")
    @JvmField val JUNGLE_PYRAMID_BLOCKS = createTag("jungle_pyramid_blocks")
    @JvmField val LEAF_STONE_ORES = createTag("leaf_stone_ores")
    @JvmField val MACHINES = createTag("machines")
    @JvmField val MANSION_BLOCKS = createTag("mansion_blocks")
    @JvmField val MEDICINAL_LEEK_PLANTABLE = createTag("medicinal_leek_plantable")
    @JvmField val HEARTY_GRAINS_LAND_PLANTABLE = createTag("hearty_grains_land_plantable")
    @JvmField val HEARTY_GRAINS_WATER_PLANTABLE = createTag("hearty_grains_water_plantable")
    @JvmField val MINTS = createTag("mints")
    @JvmField val MOON_STONE_ORES = createTag("moon_stone_ores")
    @JvmField val NATURAL = createTag("natural")
    @JvmField val NETHER_STRUCTURE_BLOCKS = createTag("nether_structure_blocks")
    @JvmField val PINK_FLOWERS = createTag("pink_flowers")
    @JvmField val RED_FLOWERS = createTag("red_flowers")
    @JvmField val RED_TUMBLESTONE_BRICKS = createTag("red_tumblestone_bricks")
    @JvmField val RED_TUMBLESTONES = createTag("red_tumblestones")
    @JvmField val REDSTONE_BLOCKS = createTag("redstone_blocks")
    @JvmField val ROOTS = createTag("roots")
    @JvmField val ROOTS_SPREADABLE = createTag("roots_spreadable")
    @JvmField val SACCHARINE_LEAVES = createTag("saccharine_leaves")
    @JvmField val SACCHARINE_LOGS = createTag("saccharine_logs")
    @JvmField val SACCHARINE_SAPLING = createTag("saccharine_sapling")
    @JvmField val RUINED_PORTAL_BLOCKS = createTag("ruined_portal_blocks")
    @JvmField val SEES_SKY = createTag("sees_sky")
    @JvmField val SHINY_STONE_ORES = createTag("shiny_stone_ores")
    @JvmField val SIGNS = createTag("signs")
    @JvmField val SKY_TUMBLESTONE_BRICKS = createTag("sky_tumblestone_bricks")
    @JvmField val SKY_TUMBLESTONES = createTag("sky_tumblestones")
    @JvmField val SMALL_FLOWERS = createTag("small_flowers")
    @JvmField val SNOW_BLOCK = createTag("snow_block")
    @JvmField val STANDING_SIGNS = createTag("standing_signs")
    @JvmField val SUN_STONE_ORES = createTag("sun_stone_ores")
    @JvmField val THUNDER_STONE_ORES = createTag("thunder_stone_ores")
    @JvmField val TRAIL_RUINS_BLOCKS = createTag("trail_ruins_blocks")
    @JvmField val TRASH = createTag("trash")
    @JvmField val TREES = createTag("trees")
    @JvmField val TUMBLESTONE_BRICKS = createTag("tumblestone_bricks")
    @JvmField val TUMBLESTONES = createTag("tumblestones")
    @JvmField val TUMBLESTONE_HEAT_SOURCE = createTag("tumblestone_heat_source")
    @JvmField val WALL_HANGING_SIGNS = createTag("wall_hanging_signs")
    @JvmField val WALL_SIGNS = createTag("wall_signs")
    @JvmField val WATER_STONE_ORES = createTag("water_stone_ores")
    @JvmField val WHITE_FLOWERS = createTag("white_flowers")
    @JvmField val YELLOW_FLOWERS = createTag("yellow_flowers")

    private fun createTag(name: String) = TagKey.create(Registries.BLOCK, cobblemonResource(name))

}
