/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.ai

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.genericRuntime
import com.cobblemon.mod.common.util.mainThreadRuntime
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.withQueryValue
import com.google.common.collect.ImmutableMap
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus

class LookInDirectionTask(
    val shouldLock: Expression = "true".asExpression(),
    val yaw: Expression = "0".asExpression(),
    val pitch: Expression = "0".asExpression()
) : Behavior<LivingEntity>(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT), 0, 0) {
    override fun canStillUse(level: ServerLevel, entity: LivingEntity, gameTime: Long): Boolean {
        return !entity.brain.getMemory(MemoryModuleType.LOOK_TARGET).isPresent && mainThreadRuntime.withQueryValue("entity", entity.asMostSpecificMoLangValue()).resolveBoolean(shouldLock)
    }

    override fun checkExtraStartConditions(level: ServerLevel, owner: LivingEntity): Boolean {
        return owner is PathfinderMob && !owner.brain.getMemory(MemoryModuleType.LOOK_TARGET).isPresent && mainThreadRuntime.withQueryValue("entity", owner.asMostSpecificMoLangValue()).resolveBoolean(shouldLock)
    }

    override fun stop(level: ServerLevel, entity: LivingEntity, gameTime: Long) {
    }

    override fun tick(level: ServerLevel, entity: LivingEntity, gameTime: Long) {
        entity as PathfinderMob
        mainThreadRuntime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
        val yaw = (mainThreadRuntime.resolveFloat(this.yaw) + 90) * Math.PI / 180.0
        val pitch = mainThreadRuntime.resolveFloat(this.pitch) * Math.PI / 180.0 * -1

        val xDisp = Math.cos(yaw)
        val zDisp = Math.sin(yaw)
        val yDisp = Math.sin(pitch)

        entity.lookControl.setLookAt(entity.x + xDisp, entity.y + entity.eyeHeight + yDisp, entity.z + zDisp)
    }
}