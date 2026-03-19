/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.world.feature.ore

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.tags.CobblemonBiomeTags
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.placement.PlacedFeature

object CobblemonOrePlacedFeatures {

    private val features = arrayListOf<FeatureHolder>()

    // Dawn Stone
    @JvmField
    val DAWN_STONE_UPPER = of("dawn_stone_upper", CobblemonBiomeTags.HAS_DAWN_STONE_ORE)
    @JvmField
    val DAWN_STONE_LOWER = of("dawn_stone_lower", CobblemonBiomeTags.HAS_DAWN_STONE_ORE)
    @JvmField
    val DAWN_STONE_UPPER_RARE = of("dawn_stone_upper_rare", CobblemonBiomeTags.HAS_DAWN_STONE_ORE_RARE)
    @JvmField
    val DAWN_STONE_LOWER_RARE = of("dawn_stone_lower_rare", CobblemonBiomeTags.HAS_DAWN_STONE_ORE_RARE)

    // Dusk Stone
    @JvmField
    val DUSK_STONE_UPPER = of("dusk_stone_upper", CobblemonBiomeTags.HAS_DUSK_STONE_ORE)
    @JvmField
    val DUSK_STONE_LOWER = of("dusk_stone_lower", CobblemonBiomeTags.HAS_DUSK_STONE_ORE)
    @JvmField
    val DUSK_STONE_UPPER_RARE = of("dusk_stone_upper_rare", CobblemonBiomeTags.HAS_DUSK_STONE_ORE_RARE)
    @JvmField
    val DUSK_STONE_LOWER_RARE = of("dusk_stone_lower_rare", CobblemonBiomeTags.HAS_DUSK_STONE_ORE_RARE)

    // Fire Stone
    @JvmField
    val FIRE_STONE_UPPER = of("fire_stone_upper", CobblemonBiomeTags.HAS_FIRE_STONE_ORE)
    @JvmField
    val FIRE_STONE_LOWER = of("fire_stone_lower", CobblemonBiomeTags.HAS_FIRE_STONE_ORE)
    @JvmField
    val FIRE_STONE_UPPER_RARE = of("fire_stone_upper_rare", CobblemonBiomeTags.HAS_FIRE_STONE_ORE_RARE)
    @JvmField
    val FIRE_STONE_LOWER_RARE = of("fire_stone_lower_rare", CobblemonBiomeTags.HAS_FIRE_STONE_ORE_RARE)
    @JvmField
    val FIRE_STONE_NETHER = of("fire_stone_nether", CobblemonBiomeTags.HAS_FIRE_STONE_ORE_NETHER)

    // Ice Stone
    @JvmField
    val ICE_STONE_UPPER = of("ice_stone_upper", CobblemonBiomeTags.HAS_ICE_STONE_ORE)
    @JvmField
    val ICE_STONE_LOWER = of("ice_stone_lower", CobblemonBiomeTags.HAS_ICE_STONE_ORE)
    @JvmField
    val ICE_STONE_UPPER_RARE = of("ice_stone_upper_rare", CobblemonBiomeTags.HAS_ICE_STONE_ORE_RARE)
    @JvmField
    val ICE_STONE_LOWER_RARE = of("ice_stone_lower_rare", CobblemonBiomeTags.HAS_ICE_STONE_ORE_RARE)

    // Leaf Stone
    @JvmField
    val LEAF_STONE_UPPER = of("leaf_stone_upper", CobblemonBiomeTags.HAS_LEAF_STONE_ORE)
    @JvmField
    val LEAF_STONE_LOWER = of("leaf_stone_lower", CobblemonBiomeTags.HAS_LEAF_STONE_ORE)
    @JvmField
    val LEAF_STONE_UPPER_RARE = of("leaf_stone_upper_rare", CobblemonBiomeTags.HAS_LEAF_STONE_ORE_RARE)
    @JvmField
    val LEAF_STONE_LOWER_RARE = of("leaf_stone_lower_rare", CobblemonBiomeTags.HAS_LEAF_STONE_ORE_RARE)

    // Moon Stone
    @JvmField
    val MOON_STONE_UPPER = of("moon_stone_upper", CobblemonBiomeTags.HAS_MOON_STONE_ORE)
    @JvmField
    val MOON_STONE_LOWER = of("moon_stone_lower", CobblemonBiomeTags.HAS_MOON_STONE_ORE)
    @JvmField
    val MOON_STONE_UPPER_RARE = of("moon_stone_upper_rare", CobblemonBiomeTags.HAS_MOON_STONE_ORE_RARE)
    @JvmField
    val MOON_STONE_LOWER_RARE = of("moon_stone_lower_rare", CobblemonBiomeTags.HAS_MOON_STONE_ORE_RARE)
    @JvmField
    val MOON_STONE_DRIPSTONE = of("moon_stone_dripstone", CobblemonBiomeTags.HAS_MOON_STONE_ORE_DRIPSTONE)

    // Shiny Stone
    @JvmField
    val SHINY_STONE_UPPER = of("shiny_stone_upper", CobblemonBiomeTags.HAS_SHINY_STONE_ORE)
    @JvmField
    val SHINY_STONE_LOWER = of("shiny_stone_lower", CobblemonBiomeTags.HAS_SHINY_STONE_ORE)
    @JvmField
    val SHINY_STONE_UPPER_RARE = of("shiny_stone_upper_rare", CobblemonBiomeTags.HAS_SHINY_STONE_ORE_RARE)
    @JvmField
    val SHINY_STONE_LOWER_RARE = of("shiny_stone_lower_rare", CobblemonBiomeTags.HAS_SHINY_STONE_ORE_RARE)

    // Sun Stone
    @JvmField
    val SUN_STONE_UPPER = of("sun_stone_upper", CobblemonBiomeTags.HAS_SUN_STONE_ORE)
    @JvmField
    val SUN_STONE_LOWER = of("sun_stone_lower", CobblemonBiomeTags.HAS_SUN_STONE_ORE)
    @JvmField
    val SUN_STONE_UPPER_RARE = of("sun_stone_upper_rare", CobblemonBiomeTags.HAS_SUN_STONE_ORE_RARE)
    @JvmField
    val SUN_STONE_LOWER_RARE = of("sun_stone_lower_rare", CobblemonBiomeTags.HAS_SUN_STONE_ORE_RARE)

    // Thunder Stone
    @JvmField
    val THUNDER_STONE_UPPER = of("thunder_stone_upper", CobblemonBiomeTags.HAS_THUNDER_STONE_ORE)
    @JvmField
    val THUNDER_STONE_LOWER = of("thunder_stone_lower", CobblemonBiomeTags.HAS_THUNDER_STONE_ORE)
    @JvmField
    val THUNDER_STONE_UPPER_RARE = of("thunder_stone_upper_rare", CobblemonBiomeTags.HAS_THUNDER_STONE_ORE_RARE)
    @JvmField
    val THUNDER_STONE_LOWER_RARE = of("thunder_stone_lower_rare", CobblemonBiomeTags.HAS_THUNDER_STONE_ORE_RARE)

    // Water Stone
    @JvmField
    val WATER_STONE_UPPER = of("water_stone_upper", CobblemonBiomeTags.HAS_WATER_STONE_ORE)
    @JvmField
    val WATER_STONE_LOWER = of("water_stone_lower", CobblemonBiomeTags.HAS_WATER_STONE_ORE)
    @JvmField
    val WATER_STONE_UPPER_RARE = of("water_stone_upper_rare", CobblemonBiomeTags.HAS_WATER_STONE_ORE_RARE)
    @JvmField
    val WATER_STONE_LOWER_RARE = of("water_stone_lower_rare", CobblemonBiomeTags.HAS_WATER_STONE_ORE_RARE)

    fun register() {
        this.features.forEach { holder ->
            Cobblemon.implementation.addFeatureToWorldGen(holder.feature, GenerationStep.Decoration.UNDERGROUND_ORES, holder.validBiomes)
        }
    }

    private fun of(id: String, validBiomes: TagKey<Biome>): ResourceKey<PlacedFeature> {
        val feature = ResourceKey.create(Registries.PLACED_FEATURE, cobblemonResource("ore/$id"))
        features += FeatureHolder(feature, validBiomes)
        return feature
    }

    private data class FeatureHolder(
        val feature: ResourceKey<PlacedFeature>,
        val validBiomes: TagKey<Biome>
    )

}