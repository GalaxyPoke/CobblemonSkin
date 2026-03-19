/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.google.common.collect.ImmutableMap
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JavaOps
import java.util.ArrayDeque
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.schedule.Activity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes


fun Entity.makeEmptyBrainDynamic() = Dynamic(
    NbtOps.INSTANCE,
    NbtOps.INSTANCE.createMap(ImmutableMap.of(NbtOps.INSTANCE.createString("memories"), NbtOps.INSTANCE.emptyMap() as Tag)) as Tag
)

fun Entity.effectiveName() = this.displayName ?: this.name

fun LivingEntity.hasMemory(memoryIdentifier: ResourceLocation): Boolean {
    val registry = BuiltInRegistries.MEMORY_MODULE_TYPE
    val memoryType = registry.get(memoryIdentifier)
    return brain.checkMemory(memoryType, MemoryStatus.VALUE_PRESENT)
}

fun LivingEntity.hasMemory(tagKey: TagKey<MemoryModuleType<*>>): Boolean {
    val registry = BuiltInRegistries.MEMORY_MODULE_TYPE
    val memoryTypesInTag = registry.getTag(tagKey).orElse(null) ?: return false
    return memoryTypesInTag.any { brain.checkMemory(it.value(), MemoryStatus.VALUE_PRESENT) }
}

fun LivingEntity.hasMemoryFromString(memory: String): Boolean {
    val key = memory.asIdentifierOrTag(Registries.MEMORY_MODULE_TYPE)
    return key.map(
        { hasMemory(memoryIdentifier = it) },
        { hasMemory(tagKey = it) }
    )
}

fun LivingEntity.jitterDropItem(itemEntity: ItemEntity) {
    itemEntity.deltaMovement = itemEntity.deltaMovement.add(
        ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f).toDouble(),
        (this.random.nextFloat() * 0.05f).toDouble(),
        ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f).toDouble()
    )
}

fun LivingEntity.isDoingActivity(activityIdentifier: ResourceLocation): Boolean {
    val registry = BuiltInRegistries.ACTIVITY
    val activity = registry.get(activityIdentifier) ?: return false
    return brain.isActive(activity)
}

fun LivingEntity.isDoingActivity(tagKey: TagKey<Activity>): Boolean {
    val registry = BuiltInRegistries.ACTIVITY
    val activitiesInTag = registry.getTag(tagKey).orElse(null) ?: return false
    return activitiesInTag.any { brain.isActive(it.value()) }
}

fun LivingEntity.isDoingActivityFromString(activity: String): Boolean {
    val key = activity.asIdentifierOrTag(Registries.ACTIVITY)
    return key.map(
        { isDoingActivity(activityIdentifier = it) },
        { isDoingActivity(tagKey = it) }
    )
}

private fun getPosScore(level: Level, entity: Entity, box: AABB, pos: BlockPos): Int {
    // position the hitbox in the xz center of the block
    val movedBox = box.move(Vec3(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5).subtract(box.bottomCenter))
    return level.getBlockCollisions(entity, movedBox).count()
}
private fun findBestBlockPosBFS(
    entity: Entity,
    pos: Vec3,
    level: Level,
    maxRadius: Int = 4
): BlockPos {
    val directions = listOf(
        BlockPos(0, 1, 0), BlockPos(0, -1, 0), // Up and down
        BlockPos(1, 0, 0), BlockPos(-1, 0, 0), // East and west
        BlockPos(0, 0, 1), BlockPos(0, 0, -1)  // North and south
    )
    val queue = ArrayDeque<BlockPos>()
    val visited = mutableSetOf<BlockPos>()
    val centerPos = BlockPos(if (pos.x > 0) pos.x.toInt() else floor(pos.x).toInt(), if (pos.y > 0) pos.y.toInt() else floor(pos.y).toInt(), if (pos.z > 0) pos.z.toInt() else floor(pos.z).toInt())
    queue.add(centerPos)
    var bestScore = Int.MAX_VALUE
    var bestPos = centerPos
    var deflatedBox = entity.boundingBox
    val maxHeight = 3
    val maxWidth = 3
    // We deflate the box to make collision checks cheaper for large pokemon.
    if(entity.bbWidth > maxWidth) {
        deflatedBox = deflatedBox.deflate((entity.bbWidth - maxWidth) / 2.0, 0.0, (entity.bbWidth - maxWidth) / 2.0)
    }
    if(entity.bbHeight > maxHeight) {
        deflatedBox = deflatedBox.deflate(0.0, (entity.bbWidth - maxHeight) / 2.0, 0.0)
    }

    while (queue.isNotEmpty()) {
        val currentPos = queue.removeFirst()
        if (currentPos in visited) continue
        visited.add(currentPos)

        val blockPosHasCollision = !level.getBlockState(currentPos).getCollisionShape(level, currentPos, CollisionContext.empty()).isEmpty

        // Ignore blockPositions that have collision
        val score = if (blockPosHasCollision) Int.MAX_VALUE else getPosScore(level, entity, deflatedBox, currentPos)
        if (score == 0) {
            // Position found with zero collisions, return immediately.
            return currentPos
        } else if (bestScore > score) {
            // Only take better scores and ignore ties, ensures that positions closer to the original position win ties.
            bestPos = currentPos
            bestScore = score
        }

        // Add neighbors (up to maxRadius)
        for (dir in directions) {
            val neighbor = currentPos.offset(dir)
            // stay within the max radius and do not path find into a wall
            if (neighbor.distManhattan(centerPos) <= maxRadius && (blockPosHasCollision || level.getBlockState(neighbor).getCollisionShape(level, neighbor, CollisionContext.empty()).isEmpty)) {
                queue.add(neighbor)
            }
        }
    }
    return bestPos
}

fun Entity.setPositionSafely(pos: Vec3): Boolean {
    // Unmute to view how long the BFS algorithm takes to run
//    val mute = false
    // TODO: Rework this function. Detect collisions in three categories: suffocation, damaging blocks, and general collision
    // The closest position with the least severe collision types will be selected to move the Pokemon to
    // The throw could be cancelled if there are no viable locations without severe problems

    // Optional: use getBlockCollisions iterator and VoxelShapes.combineAndSimplify to create a single cube to represent collision area
    // Use that cube to "push" the Pokemon out of the wall at an angle
    // Note: may not work well with L shape wall collisions
//    val collisions = world.getBlockCollisions(this, box).iterator()
//    if (collisions.hasNext()) {
//        var collisionShape = collisions.next()
//        while (collisions.hasNext()) {
//            collisionShape = VoxelShapes.union(collisionShape, collisions.next())
//            println(collisionShape)
//        }
//    } else {
//        setPosition(pos)
//        return true
//    }

    val box = boundingBox.move(pos.subtract(boundingBox.bottomCenter))

    if (level().noBlockCollision(this, box)) {
        // Given position is valid so no need to do extra work
        setPos(pos)
        return true
    }

    val bestBlockPosition: BlockPos
    val result: Vec3
//    val elapsedTime = measureTime {
        val searchRadius = min(ceil((this.bbWidth * 2)).toInt(), 4)
        bestBlockPosition = findBestBlockPosBFS(this, pos, level(), searchRadius)
        result = Vec3(bestBlockPosition.x + 0.5, bestBlockPosition.y.toDouble(), bestBlockPosition.z + 0.5)
        setPos(result)
//    }
//    if (!mute) {
//        // Displays the time taken to calculate the best position
//        server()?.playerList?.players?.forEach {
//            it.sendSystemMessage("Send out for ${(this as PokemonEntity).pokemon.species.name} completed in $elapsedTime".yellow())
//        }
//    }

    // I don't see the point of returning to the original position here, as the new position is guaranteed to have equal or fewer
    // block collisions are the original given position
    // We will return whether the new position causes suffocation, and the caller can decide what to do with that info,
    // but we will not revert the position here.
    // Battle has too many use cases in which using the new position is the preferred outcome, even if it is dangerous.
    val resultEyes = result.with(Direction.Axis.Y, result.y + this.eyeHeight)
    val resultEyeBox = AABB.ofSize(resultEyes, bbWidth.toDouble(), 1.0E-6, bbWidth.toDouble())
    var collides = false

    for (target in BlockPos.betweenClosedStream(resultEyeBox)) {
        val blockState = this.level().getBlockState(target)
        collides = !blockState.isAir &&
                blockState.isSuffocating(this.level(), target) &&
                Shapes.joinIsNotEmpty(
                    blockState.getCollisionShape(this.level(), target)
                        .move(target.x.toDouble(), target.y.toDouble(), target.z.toDouble()),
                    Shapes.create(resultEyeBox),
                    BooleanOp.AND
                )
        if (collides) break
    }
    this.setPos(result)
    return !collides
}

fun Entity.isDusk(): Boolean {
    val time = level().dayTime % 24000
    return time in 12000..13000
}

fun Entity.isStandingOn(blocks: Set<String>, depth: Int = 2): Boolean {
    for (currentDepth in 1..depth) {
        val bellowBlockPos = blockPosition().below(currentDepth)
        val blockState = level().getBlockState(bellowBlockPos)

        if (blockState.isAir || !blockState.isCollisionShapeFullBlock(level(), bellowBlockPos)) continue

        val elementOrTags = ExtraCodecs.TAG_OR_ELEMENT_ID.listOf().decode(JavaOps.INSTANCE, blocks.toList()).result().get().first
        elementOrTags.forEach {
            if (it.tag) {
                if (blockState.blockHolder.`is`(TagKey.create(Registries.BLOCK, it.id))) return true
            } else {
                if (blockState.blockHolder.`is`(it.id)) return true
            }
        }
    }

    return false
}

fun Entity.distanceTo(pos: BlockPos): Double {
    val difference = pos.toVec3d().subtract(this.position())
    return difference.length()
}

fun Entity.closestPosition(positions: Iterable<BlockPos>, filter: (BlockPos) -> Boolean = { true }): BlockPos? {
    var closest: BlockPos? = null
    var closestDistance = Double.MAX_VALUE

    val iterator = positions.iterator()
    while (iterator.hasNext()) {
        val position = iterator.next()
        if (filter(position)) {
            val distance = distanceTo(position)
            if (distance < closestDistance) {
                closest = BlockPos(position)
                closestDistance = distance
            }
        }
    }

    return closest
}

fun Entity.getIsSubmerged() = isInLava || isUnderWater

fun <T> SynchedEntityData.update(data: EntityDataAccessor<T>, mutator: (T) -> T) {
    val value = get(data)
    val newValue = mutator(value)
    if (value != newValue) {
        set(data, newValue)
    }
}