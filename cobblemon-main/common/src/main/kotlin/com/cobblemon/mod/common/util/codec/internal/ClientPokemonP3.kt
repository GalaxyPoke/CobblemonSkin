/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.codec.internal

import com.cobblemon.mod.common.api.mark.Marks
import com.cobblemon.mod.common.api.riding.stats.RidingStat
import com.cobblemon.mod.common.client.settings.ServerSettings
import com.cobblemon.mod.common.pokemon.OriginalTrainerType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import java.util.*

internal data class ClientPokemonP3(
    val originalTrainerType: OriginalTrainerType,
    val originalTrainer: Optional<String>,
    val originalTrainerName: Optional<String>,
    val aspects: Set<String>,
    val heldItemVisible: Optional<Boolean>,
    val cosmeticItem: Optional<ItemStack>,
    val activeMark: Optional<ResourceLocation>,
    val marks: Set<ResourceLocation>,
    val potentialMarks: Set<ResourceLocation>,
    val markings: List<Int>,
    val rideBoosts: Map<String, Float>,
    val currentFullness: Int,
    val interactionCooldowns: Map<ResourceLocation, Int>,
) : Partial<Pokemon> {

    override fun into(other: Pokemon): Pokemon {
        other.originalTrainerType = this.originalTrainerType
        this.originalTrainer.ifPresent { other.originalTrainer = it }
        this.originalTrainerName.ifPresent { other.originalTrainerName = it }
        other.forcedAspects = this.aspects
        this.heldItemVisible.ifPresent { other.heldItemVisible = it }
        other.cosmeticItem = this.cosmeticItem.orElse(ItemStack.EMPTY)
        this.activeMark.ifPresent { other.activeMark = Marks.getByIdentifier(it) }
        other.marks.clear()
        other.marks += this.marks.map { Marks.getByIdentifier(it) }.filterNotNull().toMutableSet()
        other.potentialMarks.clear()
        other.potentialMarks += this.potentialMarks.map { Marks.getByIdentifier(it) }.filterNotNull().toMutableSet()
        other.markings = this.markings
        this.rideBoosts.let { other.setRideBoosts(it.mapKeys { RidingStat.valueOf(it.key) }) }
        other.currentFullness = this.currentFullness
        other.interactionCooldowns = this.interactionCooldowns.toMutableMap()
        return other
    }

    companion object {
        /**
         * do not use cobblemon.config in here, as this is used by the client whose config is different to server, always use [ServerSettings]
         */
        internal val CODEC: MapCodec<ClientPokemonP3> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                OriginalTrainerType.CODEC.optionalFieldOf(DataKeys.POKEMON_ORIGINAL_TRAINER_TYPE, OriginalTrainerType.NONE).forGetter(ClientPokemonP3::originalTrainerType),
                Codec.STRING.optionalFieldOf(DataKeys.POKEMON_ORIGINAL_TRAINER).forGetter(ClientPokemonP3::originalTrainer),
                Codec.STRING.optionalFieldOf(DataKeys.POKEMON_ORIGINAL_TRAINER_NAME).forGetter(ClientPokemonP3::originalTrainerName),
                Codec.list(Codec.STRING).optionalFieldOf(DataKeys.POKEMON_FORCED_ASPECTS, emptyList()).xmap({ it.toSet() }, { it.toMutableList() }).forGetter(ClientPokemonP3::aspects),
                Codec.BOOL.optionalFieldOf(DataKeys.HELD_ITEM_VISIBLE).forGetter(ClientPokemonP3::heldItemVisible),
                ItemStack.CODEC.optionalFieldOf(DataKeys.POKEMON_COSMETIC_ITEM).forGetter(ClientPokemonP3::cosmeticItem),
                ResourceLocation.CODEC.optionalFieldOf(DataKeys.POKEMON_ACTIVE_MARK).forGetter(ClientPokemonP3::activeMark),
                Codec.list(ResourceLocation.CODEC).fieldOf(DataKeys.POKEMON_MARKS).xmap({ it.toSet() }, { it.toMutableList() }).forGetter(ClientPokemonP3::marks),
                Codec.list(ResourceLocation.CODEC).fieldOf(DataKeys.POKEMON_POTENTIAL_MARKS).xmap({ it.toSet() }, { it.toMutableList() }).forGetter(ClientPokemonP3::potentialMarks),
                Codec.list(Codec.INT).optionalFieldOf(DataKeys.POKEMON_MARKINGS, listOf(0, 0, 0, 0, 0, 0)).forGetter(ClientPokemonP3::markings),
                Codec.unboundedMap(Codec.STRING, Codec.FLOAT).fieldOf(DataKeys.POKEMON_RIDE_BOOSTS).forGetter(ClientPokemonP3::rideBoosts),
                Codec.intRange(0, 100).fieldOf(DataKeys.POKEMON_FULLNESS).forGetter(ClientPokemonP3::currentFullness),
                Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf(DataKeys.POKEMON_INTERACTION_COOLDOWN).forGetter(ClientPokemonP3::interactionCooldowns),
                ).apply(instance, ::ClientPokemonP3)
        }

        internal fun from(pokemon: Pokemon): ClientPokemonP3 = ClientPokemonP3(
            pokemon.originalTrainerType,
            Optional.ofNullable(pokemon.originalTrainer),
            Optional.ofNullable(pokemon.originalTrainerName),
            pokemon.aspects + pokemon.forcedAspects,
            Optional.ofNullable(pokemon.heldItemVisible),
            Optional.ofNullable(pokemon.cosmeticItem.takeIf { !it.isEmpty }),
            Optional.ofNullable(pokemon.activeMark?.identifier),
            pokemon.marks.toList().map { it.identifier }.toSet(),
            pokemon.potentialMarks.toList().map { it.identifier }.toSet(),
            pokemon.markings,
            pokemon.getRideBoosts().mapKeys { it.key.name },
            pokemon.currentFullness,
            pokemon.interactionCooldowns,
        )
    }
}
