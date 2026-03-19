/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.entity

import com.cobblemon.mod.common.api.riding.Rideable
import com.cobblemon.mod.common.client.MountedPokemonAnimationRenderController
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import org.joml.AxisAngle4f
import org.joml.Vector3f

object MountedLivingEntityRenderer {
    fun render(
        entity: LivingEntity,
        pokemon: PokemonEntity,
        stack: PoseStack,
        bob: Float,
        yBodyRot: Float,
        partialTicks: Float,
        i: Float
    ) {
        if (entity.vehicle !is Rideable) return
        val matrix = stack.last().pose()
        val delegate = pokemon.delegate as PokemonClientDelegate
        val locator = delegate.locatorStates[delegate.getSeatLocator(entity)]

        //Positions entity
        if (locator != null) {
            MountedPokemonAnimationRenderController.setup(pokemon, partialTicks)

            //Undo seat position
            val playerPos = Vec3(
                Mth.lerp(partialTicks.toDouble(), entity.xOld, entity.x),
                Mth.lerp(partialTicks.toDouble(), entity.yOld, entity.y),
                Mth.lerp(partialTicks.toDouble(), entity.zOld, entity.z),
            )

            val entityPos = Vec3(
                Mth.lerp(partialTicks.toDouble(), pokemon.xOld, pokemon.x),
                Mth.lerp(partialTicks.toDouble(), pokemon.yOld, pokemon.y),
                Mth.lerp(partialTicks.toDouble(), pokemon.zOld, pokemon.z),
            )

            matrix.translate(playerPos.subtract(entityPos).toVector3f().negate())
            matrix.translate(locator.matrix.getTranslation(Vector3f()))

            val offset = Vector3f(0f, entity.bbHeight / 2, 0f).mul(-1f)

            matrix.rotate(locator.matrix.getRotation(AxisAngle4f()))
            matrix.rotate(Axis.YP.rotationDegrees(180 + yBodyRot))
            matrix.translate(offset)

            matrix.translate(Vector3f(0f, 0.35f, 0f))
        }
    }
}