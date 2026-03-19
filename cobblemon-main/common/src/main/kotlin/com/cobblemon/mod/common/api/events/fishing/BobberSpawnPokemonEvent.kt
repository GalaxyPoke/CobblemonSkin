/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.fishing

import com.bedrockk.molang.runtime.value.MoValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.moLangFunctionMap
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.itemRegistry
import com.cobblemon.mod.common.util.server
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.ItemStack

/**
 * Event that is fired when a Pokemon is spawned by a bobber.
 */
interface BobberSpawnPokemonEvent {

    /**
     * Event that is fired before a Pokemon is spawned by a bobber.
     * @param bobber The PokeRodFishingBobberEntity that is spawning the Pokemon.
     * @param spawnAction The spawn that is planned.
     * @param rod The ItemStack of the rod that the bobber is attached to.
     */
    data class Pre(
        val bobber: PokeRodFishingBobberEntity,
        val spawnAction: SpawnAction<*>,
        val rod: ItemStack
    ) : Cancelable(), BobberSpawnPokemonEvent {
        val context = mutableMapOf<String, MoValue>(
            "rod" to rod.asMoLangValue(server()!!.registryAccess())
        )
        val functions = moLangFunctionMap(
            cancelFunc
        )
    }

    /**
     * Event that is fired when a Pokemon is modified before it is spawned by a bobber.
     * @param spawnAction The [SpawnAction] that is motivating the spawn.
     * @param rod The ItemStack of the rod that the bobber is attached to.
     * @param pokemon The Pokemon that is modified.
     */
    data class Modify(
        val spawnAction: SpawnAction<*>,
        val rod: ItemStack,
        val pokemon: PokemonEntity
    ) : BobberSpawnPokemonEvent

    /**
     * Event that is fired after a Pokemon is spawned by a bobber.
     * @param bobber The PokeRodFishingBobberEntity that is spawning the Pokemon.
     * @param chosenBucket The bucket that is chosen.
     * @param bait The ItemStack of the bait that is set on the rod.
     * @param pokemon The Pokemon that is spawned.
     */
    data class Post(
        val bobber: PokeRodFishingBobberEntity,
        val spawnAction: SpawnAction<*>,
        val bait: ItemStack,
        val pokemon: PokemonEntity
    ) : BobberSpawnPokemonEvent {
        val context = mutableMapOf<String, MoValue>(
            "chosen_bucket" to ObjectValue(spawnAction.bucket, { it.name }),
            "bait" to pokemon.level().itemRegistry.wrapAsHolder(bait.item).asMoLangValue(Registries.ITEM),
            "pokemon_entity" to pokemon.asMoLangValue()
        )
    }
}