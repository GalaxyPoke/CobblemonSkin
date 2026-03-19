/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

/**
 * This class is responsible for setting up the animation state of a Pokemon entity before it begins to render.
 *
 * Sometimes we depend on the animation state of the Pokemon to be set up before we begin rendering it, so this class
 * allows us to do that. It also caches which Pokemon have been pre-animated to avoid doing it multiple times.
 *
 * @author landonjw
 */
object MountedPokemonAnimationRenderController {
    private val forcedDeltas = mutableMapOf<Int, Float>()

    fun setup(pokemon: PokemonEntity, partialTickTime: Float) {
        if (forcedDeltas.contains(pokemon.id)) return

        val delegate = pokemon.delegate as? PokemonClientDelegate ?: return
        val model = delegate.currentModel ?: return
        if (!model.isReadyForAnimation()) return

        val entityPos = Vec3(
            Mth.lerp(partialTickTime.toDouble(), pokemon.xOld, pokemon.x),
            Mth.lerp(partialTickTime.toDouble(), pokemon.yOld, pokemon.y),
            Mth.lerp(partialTickTime.toDouble(), pokemon.zOld, pokemon.z)
        )

        val f = Mth.rotLerp(partialTickTime, pokemon.yBodyRotO, pokemon.yBodyRot)
        val g = Mth.rotLerp(partialTickTime, pokemon.yHeadRotO, pokemon.yHeadRot)
        var h = g - f
        val j = Mth.lerp(partialTickTime, pokemon.xRotO, pokemon.xRot)
        h = Mth.wrapDegrees(h)

        var l = 0.0f
        var m = 0.0f
        if (!pokemon.isPassenger && pokemon.isAlive) {
            l = pokemon.walkAnimation.speed(partialTickTime)
            m = pokemon.walkAnimation.position(partialTickTime)
            if (pokemon.isBaby) {
                m *= 3.0f
            }

            if (l > 1.0f) {
                l = 1.0f
            }
        }

        delegate.updatePartialTicks(partialTickTime)
        delegate.updateLocatorPosition(entityPos)
        delegate.currentModel?.applyAnimations(pokemon, delegate, l, m, partialTickTime, j, h)

        forcedDeltas[pokemon.id] = partialTickTime
    }

    fun getPartialTick(pokemon: PokemonEntity) : Float? {
        return forcedDeltas[pokemon.id]
    }

    /**
     * Resets the prerendered list. This should be called in the render loop before we begin setting up any pokemon with `setup`.
     */
    fun reset() {
        forcedDeltas.clear()
    }
}
