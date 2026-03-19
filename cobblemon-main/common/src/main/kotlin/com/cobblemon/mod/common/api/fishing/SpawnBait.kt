/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.fishing

import com.cobblemon.mod.common.api.conditional.ITEM_REGISTRY_LIKE_CODEC
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.fishing.BaitEffectFunctionRegistryEvent
import com.cobblemon.mod.common.api.spawning.fishing.FishingSpawnCause
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

class SpawnBait(
    val item: RegistryLikeCondition<Item>,
    val effects: List<Effect>,
) {
    data class Effect(
        val type: ResourceLocation,
        val subcategory: ResourceLocation?,
        val chance: Double = 0.0,
        val value: Double = 0.0
    ) {
        constructor(type: ResourceLocation, subcategory: Optional<ResourceLocation>, chance: Double, value: Double) : this(type, subcategory.orElse(null), chance, value)

        companion object {
            val CODEC = RecordCodecBuilder.create<Effect> { instance ->
                instance.group(
                    ResourceLocation.CODEC.fieldOf("type").forGetter { it.type },
                    ResourceLocation.CODEC.optionalFieldOf("subcategory").forGetter { Optional.ofNullable(it.subcategory) },
                    Codec.DOUBLE.fieldOf("chance").forGetter { it.chance },
                    Codec.DOUBLE.fieldOf("value").forGetter { it.value }
                ).apply(instance, ::Effect)
            }
        }
    }

    companion object {
        val CODEC = RecordCodecBuilder.create<SpawnBait> { instance ->
            instance.group(
                ITEM_REGISTRY_LIKE_CODEC.fieldOf("item").forGetter { it.item },
                Effect.CODEC.listOf().fieldOf("effects").forGetter {it.effects}
            ).apply(instance, ::SpawnBait)
        }

        val STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC)
    }

    object Effects {
        private val EFFECT_FUNCTIONS: MutableMap<ResourceLocation, (PokemonEntity, Effect) -> Unit> = mutableMapOf()
        val NATURE = cobblemonResource("nature")
        val IV = cobblemonResource("iv")
        val EV = cobblemonResource("ev")
        val BITE_TIME = cobblemonResource("bite_time")
        val GENDER_CHANCE = cobblemonResource("gender_chance")
        val LEVEL_RAISE = cobblemonResource("level_raise")
        val TYPING = cobblemonResource("typing")
        val EGG_GROUP = cobblemonResource("egg_group")
        val SHINY_REROLL = cobblemonResource("shiny_reroll")
        val DROPS_REROLL = cobblemonResource("drops_reroll")
        val HIDDEN_ABILITY_CHANCE = cobblemonResource("ha_chance")
        val POKEMON_CHANCE = cobblemonResource("pokemon_chance")
        val FRIENDSHIP = cobblemonResource("friendship")
        val RARITY_BUCKET = cobblemonResource("rarity_bucket")

        fun registerEffect(type: ResourceLocation, effect: (PokemonEntity, Effect) -> Unit) {
            EFFECT_FUNCTIONS[type] = effect
        }

        fun getEffectFunction(type: ResourceLocation): ((PokemonEntity, Effect) -> Unit)? {
            return EFFECT_FUNCTIONS[type]
        }

        fun setupEffects() {
            registerEffect(NATURE) { entity, effect -> FishingSpawnCause.alterNatureAttempt(entity, effect) }
            registerEffect(IV) { entity, effect -> FishingSpawnCause.alterIVAttempt(entity, effect) }
            registerEffect(SHINY_REROLL) { entity, effect -> FishingSpawnCause.shinyReroll(entity, effect) }
            registerEffect(DROPS_REROLL) { entity, effect -> FishingSpawnCause.saveDropsReroll(entity, effect) }
            registerEffect(GENDER_CHANCE) { entity, effect -> FishingSpawnCause.alterGenderAttempt(entity, effect) }
            registerEffect(LEVEL_RAISE) { entity, effect -> FishingSpawnCause.alterLevelAttempt(entity, effect) }
            registerEffect(HIDDEN_ABILITY_CHANCE) { entity, _ -> FishingSpawnCause.alterHAAttempt(entity) }
            registerEffect(FRIENDSHIP) { entity, effect -> FishingSpawnCause.alterFriendshipAttempt(entity, effect) }
            CobblemonEvents.BAIT_EFFECT_REGISTRATION.post(BaitEffectFunctionRegistryEvent()) { event ->
                event.functions.forEach { (type, function) -> registerEffect(type, function) }
            }
        }
    }
}


