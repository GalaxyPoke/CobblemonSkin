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
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.util.party
import net.minecraft.server.level.ServerPlayer


/**
 * Behavioural properties relating to how it treats other entities and how base Minecraft entities treat it.
 */
class EntityBehaviour {
    val avoidedByCreeper = false
    val avoidedByPhantom = false
    val avoidedByFox = false
    val avoidedBySkeleton = false

    @Transient
    val struct = ObjectValue(this).also {
        it.addFunction("avoided_by_creeper") { DoubleValue(avoidedByCreeper) }
        it.addFunction("avoided_by_phantom") { DoubleValue(avoidedByPhantom) }
        it.addFunction("avoided_by_fox") { DoubleValue(avoidedByFox) }
        it.addFunction("avoided_by_skeleton") { DoubleValue(avoidedBySkeleton) }
    }

    companion object {
        fun hasCreeperFearedShoulderMount(player: ServerPlayer) : Boolean {
            return player.party().any { pokemon -> pokemon.state is ShoulderedState && pokemon.form.behaviour.entityInteract.avoidedByCreeper }
        }

        fun hasFoxFearedShoulderMount(player: ServerPlayer) : Boolean {
            return player.party().any { pokemon -> pokemon.state is ShoulderedState && pokemon.form.behaviour.entityInteract.avoidedByFox }
        }

        fun hasSkeletonFearedShoulderMount(player: ServerPlayer) : Boolean {
            return player.party().any { pokemon -> pokemon.state is ShoulderedState && pokemon.form.behaviour.entityInteract.avoidedBySkeleton }
        }

        fun hasPhantomFearedShoulderMount(player: ServerPlayer) : Boolean {
            return player.party().any { pokemon -> pokemon.state is ShoulderedState && pokemon.form.behaviour.entityInteract.avoidedByPhantom }
        }
    }
}