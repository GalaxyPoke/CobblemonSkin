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
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.entity.OmniPathingEntity
import com.cobblemon.mod.common.entity.ai.CobblemonRandomSurfacePos
import com.cobblemon.mod.common.entity.ai.CobblemonWalkTarget
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.mainThreadRuntime
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.toVec3d
import com.cobblemon.mod.common.util.withQueryValue
import com.mojang.datafixers.util.Either
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.BehaviorUtils
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.util.HoverRandomPos
import net.minecraft.world.entity.ai.util.LandRandomPos
import net.minecraft.world.level.pathfinder.PathType

class WanderTaskConfig : SingleTaskConfig {
    companion object {
        const val WANDER = "wander" // Category
        const val MAX_LOOK_DOWN_DISTANCE = 64
    }

    val condition = booleanVariable(WANDER, "wanders", true).asExpressible()
    val wanderChance = numberVariable(WANDER, "wander_chance", 1 / (20 * 6F)).asExpressible()
    val horizontalRange = numberVariable(WANDER, "horizontal_wander_range", 10).asExpressible()
    val verticalRange = numberVariable(WANDER, "vertical_wander_range", 5).asExpressible()
    val speedMultiplier =
        numberVariable(SharedEntityVariables.MOVEMENT_CATEGORY, SharedEntityVariables.WALK_SPEED, 0.35).asExpressible()
    val avoidTargetingAir: ExpressionOrEntityVariable =
        Either.left("true".asExpression()) // Whether to avoid air blocks when wandering
    val minimumHeight: ExpressionOrEntityVariable = Either.left("0".asExpression()) // Height off the ground
    val maximumHeight: ExpressionOrEntityVariable = Either.left("-1".asExpression()) // Height off the ground

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) =
        listOf(
            condition,
            wanderChance,
            horizontalRange,
            verticalRange,
            speedMultiplier,
            avoidTargetingAir,
            minimumHeight,
            maximumHeight
        ).asVariables()

    private fun applyHeightConstraints(
        pos: BlockPos,
        minimumHeight: Int,
        maximumHeight: Int,
        world: ServerLevel
    ): BlockPos {
        if (minimumHeight <= 0 && maximumHeight == -1) {
            return pos // It ain't a hoverer
        }

        val block = world.getBlockState(pos)
        val altitude = if (!block.isAir) {
            0
        } else {
            (1..MAX_LOOK_DOWN_DISTANCE).firstOrNull {
                val newPos = pos.below(it)
                if (!world.getBlockState(newPos).isAir) {
                    return@firstOrNull true
                } else if (newPos.y <= world.minBuildHeight) {
                    // Don't adjust downward into the void (Mostly if we're in the end)
                    // This doesn't stop fliers from wandering off end islands,
                    // but it does stop them from diving directly into the void.
                    return pos
                }
                return@firstOrNull false
            } ?: MAX_LOOK_DOWN_DISTANCE
        }

        if (altitude > maximumHeight && maximumHeight >= 0) {
            val excess = altitude - maximumHeight
            val excessFromMinimum = altitude - minimumHeight
            val correction = world.random.nextIntBetweenInclusive(excess, excessFromMinimum)
            return pos.atY(pos.y - correction)
        } else if (altitude < minimumHeight && minimumHeight >= 0) {
            val deficit = minimumHeight - altitude
            val deficitFromMaximum = maximumHeight - altitude
            val correction = world.random.nextIntBetweenInclusive(deficit, deficitFromMaximum)
            return pos.atY(pos.y + correction)
        } else {
            return pos
        }
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
                it.absent(CobblemonMemories.PATH_COOLDOWN),
                it.registered(CobblemonMemories.WANDER_CONTROL)
            ).apply(it) { walkTarget, lookTarget, pathCooldown, wanderControl ->
                Trigger { world, entity, time ->
                    if (entity !is PathfinderMob || (entity.isUnderWater && !(entity.canBreatheUnderwater() && !((entity as OmniPathingEntity).canSwimInWater())))) {
                        return@Trigger false
                    }

                    val wanderControl = it.tryGet(wanderControl).orElse(null) ?: CobblemonWanderControl()
                    mainThreadRuntime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
                    val avoidsTargetingAir = avoidTargetingAir.resolveBoolean(mainThreadRuntime)
                    val wanderChance = mainThreadRuntime.resolveFloat(wanderChanceExpression)
                    if (wanderChance <= 0 || world.random.nextFloat() > wanderChance) {
                        return@Trigger false
                    }
                    pathCooldown.setWithExpiry(true, wanderControl.pathCooldownTicks.toLong())
                    val minimumHeight = minimumHeight.resolveInt(mainThreadRuntime)
                    val maximumHeight = maximumHeight.resolveInt(mainThreadRuntime)

                    var attempts = 0
                    var pos: BlockPos? = null
                    while (attempts++ < wanderControl.maxAttempts && pos == null) {
//                    val targetVec = getLandTarget(entity) ?: return@Trigger true
                        val targetVec = if (maximumHeight != -1) {
                            HoverRandomPos.getPos(
                                entity,
                                horizontalRange.resolveInt(mainThreadRuntime),
                                verticalRange.resolveInt(mainThreadRuntime),
                                entity.random.nextFloat() - 0.5,
                                entity.random.nextFloat() - 0.5,
                                Math.PI.toFloat(),
                                maximumHeight,
                                minimumHeight
                            )
                        } else if (entity.isUnderWater && entity.canBreatheUnderwater() && !((entity as OmniPathingEntity).canSwimInWater())) {
                            BehaviorUtils.getRandomSwimmablePos(
                                entity,
                                horizontalRange.resolveInt(mainThreadRuntime),
                                verticalRange.resolveInt(mainThreadRuntime)
                            )
                        } else if (entity is OmniPathingEntity && (entity.canWalkOnWater() || entity.canWalkOnLava())) {
                            CobblemonRandomSurfacePos.getPos(
                                entity,
                                horizontalRange.resolveInt(mainThreadRuntime),
                                verticalRange.resolveInt(mainThreadRuntime)
                            )
                        } else {
                            LandRandomPos.getPos(
                                entity,
                                horizontalRange.resolveInt(mainThreadRuntime),
                                verticalRange.resolveInt(mainThreadRuntime)
                            )
                        } ?: continue

                        if (targetVec == null) continue

                        // Ignore pathing targets outside of the pasture zone if the pokemon is pastured.
                        val tethering = (entity as? PokemonEntity)?.tethering
                        if (tethering?.canRoamTo(BlockPos.containing(targetVec)) == false) {
                            continue
                        }

                        pos = applyHeightConstraints(
                            pos = BlockPos.containing(targetVec),
                            minimumHeight = minimumHeight,
                            maximumHeight = maximumHeight,
                            world = world
                        ).takeIf(wanderControl::isSuitable)
                    }

                    if (pos == null) {
                        return@Trigger false
                    }

                    walkTarget.set(
                        CobblemonWalkTarget(
                            pos = pos,
                            speedModifier = speedMultiplier.resolveFloat(mainThreadRuntime),
                            completionRange = 0,
                            nodeTypeFilter = { nodeType ->
                                entity.canBreatheUnderwater() || nodeType !in listOf(
                                    PathType.WATER,
                                    PathType.WATER_BORDER
                                )
                            },
                            destinationNodeTypeFilter = { nodeType ->
                                !avoidsTargetingAir || nodeType !in listOf(
                                    PathType.OPEN
                                )
                            }
                        )
                    )
                    lookTarget.set(BlockPosTracker(pos.toVec3d().add(0.0, entity.eyeHeight.toDouble(), 0.0)))
                    return@Trigger true
                }
            }
        }
    }

    /*
     * This method is from the old wander goal. It is very verbose but reliably finds decent land wander targets. Its only advantage over
     * the LandRandomPos method (with our CobblemonWalkTarget) is that it doesn't lead entities in the direction of water.
     */
//    fun getLandTarget(entity: PathfinderMob): Vec3? {
//        val roamDistanceCondition: (BlockPos) -> Boolean = if (entity is PokemonEntity) ({ entity.tethering?.canRoamTo(it) != false }) else ({ true })
//        val iterable: Iterable<BlockPos> = BlockPos.randomBetweenClosed(entity.random, 64, entity.blockX - 10, entity.blockY, entity.blockZ - 10, entity.blockX + 10, entity.blockY, entity.blockZ + 10)
//        val condition: (BlockState, BlockPos) -> Boolean = { _, pos -> entity.canFit(pos) && roamDistanceCondition(pos) }
//        val iterator = iterable.iterator()
//        position@
//        while (iterator.hasNext()) {
//            val pos = iterator.next().mutable()
//            var blockState = entity.level().getBlockState(pos)
//
//            val maxSteps = 16
//            var steps = 0
//            var good = false
//            if (!blockState.isSolid && !blockState.liquid()) {
//                pos.move(0, -1, 0)
//                var previousWasAir = true
//                while (steps++ < maxSteps && pos.y > entity.level().minBuildHeight) {
//                    if (pos.y <= entity.level().minBuildHeight) {
//                        continue@position
//                    }
//                    blockState = entity.level().getBlockState(pos)
//                    if (blockState.isSolid && !blockState.`is`(BlockTags.LEAVES) && previousWasAir) {
//                        pos.move(0, 1, 0)
//                        blockState = entity.level().getBlockState(pos)
//                        good = true
//                        break
//                    } else {
//                        previousWasAir = blockState.isAir
//                    }
//                    pos.move(0, -1, 0)
//                }
//            } else {
//                var previousWasSolid = blockState.isSolid && !blockState.`is`(BlockTags.LEAVES)
//                pos.move(0, 1, 0)
//                while (steps++ < maxSteps) {
//                    if (pos.y >= entity.level().maxBuildHeight) {
//                        continue@position
//                    }
//                    blockState = entity.level().getBlockState(pos)
//                    if (blockState.isAir && previousWasSolid) {
//                        good = true
//                        break
//                    }
//                    previousWasSolid = blockState.isSolid && !blockState.`is`(BlockTags.LEAVES)
//                    pos.move(0, 1, 0)
//                }
//            }
//
//            if (good && condition(blockState, pos)) {
//                return pos.toVec3d()
//            }
//        }
//
//        return null
//    }
}