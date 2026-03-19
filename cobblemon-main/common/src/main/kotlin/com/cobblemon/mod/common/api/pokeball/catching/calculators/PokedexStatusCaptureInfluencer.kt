/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.calculators

import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.pokedex
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity

/**
 * An influencer on capture results based on Pokedex data.
 */
interface PokedexStatusCaptureInfluencer {

    /**
     * Attempts to influence the [context] of a capture.
     *
     * @param thrower The [LivingEntity] that threw the Pokeball
     * @param target The [PokemonEntity] attempting to capture.
     * @param context The calculated [CaptureContext].
     * @return The final [CaptureContext].
     */
    fun influence(thrower: LivingEntity, target: PokemonEntity, context: CaptureContext): CaptureContext {
        if (!context.isSuccessfulCapture) {
            return context
        }
        val player = thrower as? ServerPlayer ?: return context
        val pokedex = player.pokedex()
        val speciesId = target.pokemon.species.resourceIdentifier
        val formName = target.pokemon.form.name
        val hasCaughtStatus = pokedex.getOrCreateSpeciesRecord(speciesId)
            .getOrCreateFormRecord(formName)
            .knowledge == PokedexEntryProgress.CAUGHT
        // In modern games when the catch is a success and the Pokémon has already been registered to the Pokédex
        // the catch becomes a critical capture and only requires one shake
        return if (hasCaughtStatus) {
            CaptureContext(
                numberOfShakes = 1,
                isSuccessfulCapture = true,
                isCriticalCapture = true
            )
        } else context
    }

}