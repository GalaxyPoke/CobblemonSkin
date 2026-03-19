/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.ObjectValue
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Collection of all AI properties definable at the species level of a Pokémon.
 *
 * @author Hiroku
 * @since July 15th, 2022
 */
open class PokemonBehaviour(
    val resting: RestBehaviour = RestBehaviour(),
    var moving: MoveBehaviour = MoveBehaviour(),
    val idle: IdleBehaviour = IdleBehaviour(),
    val fireImmune: Boolean = false,
    val dampensVibrations: Boolean = false,
    val entityInteract: EntityBehaviour = EntityBehaviour(),
    val lightningHit: ThunderstruckBehaviour = ThunderstruckBehaviour(),
    val blockInteract: BlockBehavior = BlockBehavior(),
    val combat: CombatBehaviour = CombatBehaviour(),
    val herd: HerdBehaviour = HerdBehaviour(),
    val characteristicRainbow: Boolean = false,
    val itemInteract: ItemBehavior = ItemBehavior(),
) {
    @Transient
    val struct = ObjectValue<PokemonBehaviour>(this).also {
        it.addFunction("resting") { resting.struct }
        it.addFunction("moving") { moving.struct }
        it.addFunction("idle") { idle.struct }
        it.addFunction("entity_interact") { entityInteract.struct }
        it.addFunction("block_interact") { blockInteract.struct }
        it.addFunction("lightning_hit") { lightningHit.struct }
        it.addFunction("characteristic_rainbow") { DoubleValue(characteristicRainbow) }
        it.addFunction("combat") { combat.struct }
        it.addFunction("herd") { herd.struct }
        it.addFunction("item_interact") { itemInteract.struct }
    }

    fun encode(buffer: RegistryFriendlyByteBuf) {
        combat.encode(buffer)
    }

    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf): PokemonBehaviour {
            val combat = CombatBehaviour.decode(buffer)

            val decodedPokemonBehaviour = PokemonBehaviour(combat = combat)

            return decodedPokemonBehaviour
        }
    }
}