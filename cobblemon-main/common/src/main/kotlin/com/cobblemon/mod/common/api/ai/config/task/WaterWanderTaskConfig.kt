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
import com.cobblemon.mod.common.api.ai.CobblemonWanderControl
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.api.ai.config.task.WanderTaskConfig.Companion.WANDER
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.entity.ai.CobblemonWalkTarget
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.mainThreadRuntime
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.withQueryValue
import com.mojang.datafixers.util.Either
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.BehaviorUtils
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.Vec3
import kotlin.math.ceil

class WaterWanderTaskConfig : SingleTaskConfig {
    val condition = booleanVariable(WANDER, "water_wanders", true).asExpressible()
    val wanderChance = numberVariable(WANDER, "water_wander_chance", 1/(20 * 3F)).asExpressible()
    val speedMultiplier = numberVariable(SharedEntityVariables.MOVEMENT_CATEGORY, SharedEntityVariables.WALK_SPEED, 0.35).asExpressible()
    val horizontalRange: ExpressionOrEntityVariable = Either.left("10.0".asExpression())
    val verticalRange: ExpressionOrEntityVariable = Either.left("7.0".asExpression())

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): List<MoLangConfigVariable> {
        return listOf(condition, wanderChance, speedMultiplier).asVariables()
    }

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (!condition.resolveBoolean(behaviourConfigurationContext.runtime)) return null

        val wanderChanceExpression = wanderChance.asSimplifiedExpression(entity)

        behaviourConfigurationContext.addMemories(
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET,
            CobblemonMemories.PATH_COOLDOWN,
            CobblemonMemories.WANDER_CONTROL
        )

        return BehaviorBuilder.create {
            it.group(
                it.absent(MemoryModuleType.WALK_TARGET),
                it.registered(MemoryModuleType.LOOK_TARGET),
                it.registered(CobblemonMemories.PATH_COOLDOWN),
                it.registered(CobblemonMemories.WANDER_CONTROL)
            ).apply(it) { walkTarget, lookTarget, pathCooldown, wanderControlData ->
                Trigger { world, entity, time ->
                    if (entity !is PathfinderMob || !entity.isInWater) {
                        return@Trigger false
                    }

                    val wanderControl = it.tryGet(wanderControlData).orElse(null) ?: CobblemonWanderControl()
                    if (!wanderControl.allowWater) {
                        return@Trigger false
                    }

                    mainThreadRuntime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
                    val wanderChance = mainThreadRuntime.resolveFloat(wanderChanceExpression)
                    if (wanderChance <= 0 || world.random.nextFloat() > wanderChance) {
                        return@Trigger false
                    }

                    pathCooldown.setWithExpiry(true, wanderControl.pathCooldownTicks.toLong())

                    var pos: BlockPos? = null
                    var target: Vec3? = null
                    var attempts = 0

                    while (attempts < wanderControl.maxAttempts && pos == null) {
                        attempts++
                        target = BehaviorUtils.getRandomSwimmablePos(entity, horizontalRange.resolveInt(mainThreadRuntime), verticalRange.resolveInt(mainThreadRuntime))
                            ?: continue
                        pos = BlockPos.containing(target).takeIf(wanderControl::isSuitable)
                    }

                    if (pos == null || target == null) {
                        return@Trigger false
                    }

                    // Move target pos down so that we don't move out of water too much
                    val requiredDepth = ceil(0.5 + entity.eyeHeight)
                    // Get the current depth of the block
                    var depth = 1
                    var testPos = pos.above()
                    var blockState = entity.level().getBlockState(testPos)
                    while (blockState.fluidState.type == Fluids.WATER) {
                        depth++
                        testPos = testPos!!.above()
                        blockState = entity.level().getBlockState(testPos)
                    }

                    if (depth < requiredDepth) {
                        // Iterate down until we reach the desired min depth or we reach a non water block
                        pos = pos.below()
                        blockState = entity.level().getBlockState(pos)
                        while (blockState.fluidState.type == Fluids.WATER && depth < requiredDepth) {
                            depth ++
                            pos = pos!!.below()
                            blockState = entity.level().getBlockState(pos)
                        }
                    }

                    if (depth < requiredDepth) {
                        return@Trigger false
                    }

                    walkTarget.set(
                        CobblemonWalkTarget(
                            pos = pos,
                            nodeTypeFilter = { it == PathType.WATER || it == PathType.WATER_BORDER },
                            speedModifier = speedMultiplier.resolveFloat(mainThreadRuntime),
                            completionRange = 0
                        )
                    )
                    lookTarget.set(BlockPosTracker(target))
                    return@Trigger true
                }
            }
        }
    }
}