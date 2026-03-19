/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.ai.ObtainableItem
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.random.Random
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.core.BlockPos
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.pathfinder.Node
import net.minecraft.world.level.pathfinder.Path
import net.minecraft.world.phys.shapes.VoxelShape
import org.joml.Vector4f
import kotlin.jvm.optionals.getOrNull

fun cobblemonResource(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(Cobblemon.MODID, path)
fun cobblemonModel(path: String, variant: String) =
    ModelResourceLocation(cobblemonResource(path), variant)

fun String.asTranslated() = Component.translatable(this)
fun String.asResource() = ResourceLocation.parse(this)
fun String.asTranslated(vararg data: Any) = Component.translatable(this, *data)
fun String.isInt() = this.toIntOrNull() != null
fun String.isDouble() = this.toDoubleOrNull() != null
fun String.isFloat() = this.toFloatOrNull() != null
fun String.isHigherVersion(other: String): Boolean {
    val thisSplits = split(".")
    val thatSplits = other.split(".")

    val thisCount = thisSplits.size
    val thatCount = thatSplits.size

    val min = min(thisCount, thatCount)
    for (i in 0 until min) {
        val thisDigit = thisSplits[i].toIntOrNull()
        val thatDigit = thatSplits[i].toIntOrNull()
        if (thisDigit == null || thatDigit == null) {
            return false
        }

        if (thisDigit > thatDigit) {
            return true
        } else if (thisDigit < thatDigit) {
            return false
        }
    }

    return thisCount > thatCount
}

fun String.substitute(placeholder: String, value: Any?) = replace("{{$placeholder}}", value?.toString() ?: "")

val Pair<Boolean, Boolean>.either: Boolean get() = first || second

fun Random.nextBetween(min: Float, max: Float): Float {
    return nextFloat() * (max - min) + min;
}

fun Random.nextBetween(min: Double, max: Double): Double {
    return nextDouble() * (max - min) + min;
}

fun Random.nextBetween(min: Int, max: Int): Int {
    return nextInt(max - min + 1) + min
}

infix fun <A, B> A.toDF(b: B): com.mojang.datafixers.util.Pair<A, B> = com.mojang.datafixers.util.Pair(this, b)

fun isUuid(string: String) : Boolean {
    return Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\$").matches(string)
}

fun VoxelShape.blockPositionsAsListRounded(): List<BlockPos> {
    val result = mutableListOf<BlockPos>()
    forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
        for (x in floor(minX).toInt() until ceil(maxX).toInt()) {
            for (y in floor(minY).toInt() until ceil(maxY).toInt()) {
                for (z in floor(minZ).toInt() until ceil(maxZ).toInt()) {
                    result.add(BlockPos(x, y, z))
                }
            }
        }
    }

    return result
}

fun VoxelShape.blockPositionsAsList(): List<BlockPos> {
    val result = mutableListOf<BlockPos>()
    forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
        for (x in minX.toInt() until maxX.toInt()) {
            for (y in minY.toInt() until maxY.toInt()) {
                for (z in minZ.toInt() until maxZ.toInt()) {
                    result.add(BlockPos(x, y, z))
                }
            }
        }
    }

    return result
}

operator fun <T> Consumer<T>.plus(action: (T) -> Unit): Consumer<T> {
    return andThen(action)
}

fun chainFutures(others: Iterator<() -> CompletableFuture<*>>, finalFuture: CompletableFuture<Unit>) {
    if (!others.hasNext()) {
        finalFuture.complete(Unit)
        return
    }

    others.next().invoke().thenApply {
        chainFutures(others, finalFuture)
    }
}

val PosableState.isBattling: Boolean
    get() = (getEntity() as? PokemonEntity)?.isBattling == true || (getEntity() as? NPCEntity)?.isInBattle() == true
val PosableState.isUnderWater: Boolean
    get() = getEntity()?.isUnderWater == true
val PosableState.isInWater: Boolean
    get() = getEntity()?.isInWater == true
val PosableState.isInWaterOrRain: Boolean
    get() = getEntity()?.isInWaterOrRain == true

fun Screen.isInventoryKeyPressed(client: Minecraft?, keyCode: Int, scanCode: Int): Boolean {
    return client?.options?.keyInventory?.matches(keyCode, scanCode) == true
}

fun InteractionHand.toEquipmentSlot(): EquipmentSlot {
    return when (this) {
        InteractionHand.MAIN_HAND -> EquipmentSlot.MAINHAND
        InteractionHand.OFF_HAND -> EquipmentSlot.OFFHAND
    }
}

fun EquipmentSlot.toHand(): InteractionHand {
    return when (this) {
        EquipmentSlot.MAINHAND -> InteractionHand.MAIN_HAND
        EquipmentSlot.OFFHAND -> InteractionHand.OFF_HAND
        else -> throw IllegalArgumentException("Invalid equipment slot: $this")
    }
}

val String.asUUID: UUID?
    get() = try {
        UUID.fromString(this)
    } catch (e: Exception) {
        null
    }

fun toHex(red: Float, green: Float, blue: Float, alpha: Float): Int {
    return ((alpha * 255).toInt() shl 24) or ((red * 255).toInt() shl 16) or ((green * 255).toInt() shl 8) or (blue * 255).toInt()
}

fun Int.toRGBA(): Vector4f {
    val red = (this shr 16 and 255) / 255.0f
    val green = (this shr 8 and 255) / 255.0f
    val blue = (this and 255) / 255.0f
    val alpha = (this shr 24 and 255) / 255.0f
    return Vector4f(red, green, blue, alpha)
}

inline fun <reified T> Any.ifIsType(block: T.() -> Unit) {
    if (this is T) {
        block(this)
    }
}

// Maybe it'd be better to just Access Widen the nodes list.
fun Path.deleteNode(index: Int) {
    val nodesAfterwards = mutableListOf<Node>()
    for (i in index + 1 until this.nodeCount) {
        nodesAfterwards.add(this.getNode(i))
    }
    val nodesBefore = mutableListOf<Node>()
    for (i in 0 until index) {
        nodesBefore.add(this.getNode(i))
    }
    this.truncateNodes(nodesBefore.size + nodesAfterwards.size)
    var i = 0
    val correctNodes = nodesBefore + nodesAfterwards
    for (node in correctNodes) {
        this.replaceNode(i++, node)
    }
}

fun Collection<ObtainableItem>.findMatchingEntry(registryAccess: RegistryAccess, stack: ItemStack): ObtainableItem? {
    return if (stack.isEmpty) {
        null
    } else {
        this.find { it.item?.isItemObtainable(registryAccess, stack) != false }
    }
}

/**
 * Resolves entity types from an entity type id or entity type tag.
 *
 * @param registryAccess The registry access to use for resolving entity types.
 * @param entityType A literal entity ID or an entity tag.
 */
fun resolveEntityTypes(registryAccess: RegistryAccess, entityType: String): Set<EntityType<*>> {
    val registry = registryAccess.registry(Registries.ENTITY_TYPE).getOrNull() ?: return emptySet()
    val types = mutableSetOf<EntityType<*>>()

    if (entityType.startsWith("#")) {
        val id = ResourceLocation.tryParse(entityType.substring(1)) ?: return emptySet()
        val tag = TagKey.create(Registries.ENTITY_TYPE, id)
        registry.getTag(tag).getOrNull()?.stream()?.forEach { types.add(it.value()) }
    } else {
        val id = ResourceLocation.tryParse(entityType) ?: return emptySet()
        registry.get(id)?.let { types.add(it) }
    }

    return types
}