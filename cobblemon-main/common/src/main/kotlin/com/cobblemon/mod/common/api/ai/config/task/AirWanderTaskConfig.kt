/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.api.ai.config.task.WanderTaskConfig.Companion.WANDER
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.mainThreadRuntime
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.withQueryValue
import com.mojang.datafixers.util.Either
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.entity.ai.util.HoverRandomPos

class AirWanderTaskConfig : SingleTaskConfig {
    val condition = booleanVariable(WANDER, "air_wanders", true).asExpressible()
    val wanderChance = numberVariable(WANDER, "air_wander_chance", 1/(20 * 1F)).asExpressible()
    val horizontalRange = numberVariable(WANDER, "horizontal_wander_range", 20).asExpressible()
    val verticalRange = numberVariable(WANDER, "vertical_wander_range", 5).asExpressible()
    val speedMultiplier = numberVariable(SharedEntityVariables.MOVEMENT_CATEGORY, SharedEntityVariables.WALK_SPEED, 0.35).asExpressible()
    val minUpwardsMovement: ExpressionOrEntityVariable = Either.left("3.0".asExpression())
    val minDownwardsMovement: ExpressionOrEntityVariable = Either.left("3.0".asExpression())

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): List<MoLangConfigVariable> {
        return listOf(condition, wanderChance, horizontalRange, verticalRange, speedMultiplier, minUpwardsMovement, minDownwardsMovement).asVariables()
    }

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (!condition.resolveBoolean(behaviourConfigurationContext.runtime)) return null
        behaviourConfigurationContext.addMemories(MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, CobblemonMemories.PATH_COOLDOWN)
        val wanderChanceExpression = wanderChance.asSimplifiedExpression(entity)

        return BehaviorBuilder.create {
            it.group(
                it.absent(MemoryModuleType.WALK_TARGET),
                it.registered(MemoryModuleType.LOOK_TARGET),
                it.absent(CobblemonMemories.PATH_COOLDOWN)
            ).apply(it) { walkTarget, lookTarget, pathCooldown ->
                Trigger { world, entity, time ->
                    if (entity !is PathfinderMob || entity.isInWater) {
                        return@Trigger false
                    }

                    val runtime = mainThreadRuntime
                    runtime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
                    val wanderChance = runtime.resolveFloat(wanderChanceExpression)
                    if (wanderChance <= 0 || world.random.nextFloat() > wanderChance) return@Trigger false

                    val rotVec = entity.getViewVector(0F)
                    pathCooldown.setWithExpiry(true, 60L)
                    val target = HoverRandomPos.getPos(entity, horizontalRange.resolveInt(runtime), verticalRange.resolveInt(runtime), rotVec.x, rotVec.y, 1.5707964f, minUpwardsMovement.resolveInt(runtime), minDownwardsMovement.resolveInt(runtime))
                        ?: return@Trigger false
                    walkTarget.set(WalkTarget(target, speedMultiplier.resolveFloat(runtime), 3))
                    lookTarget.set(BlockPosTracker(target))
                    return@Trigger true
                }
            }
        }
    }
}