/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.fishing

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.conditional.RegistryLikeIdentifierCondition
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.data.SpawnBaitRegistrySyncPacket
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.adapters.ItemLikeConditionAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

object SpawnBaitEffects : JsonDataRegistry<SpawnBait> {
    override val id = cobblemonResource("spawn_bait_effects")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<SpawnBaitEffects>()
    override val typeToken: TypeToken<SpawnBait> = TypeToken.get(SpawnBait::class.java)
    override val resourcePath = "spawn_bait_effects"
    override val gson: Gson = GsonBuilder()
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Item::class.java).type, ItemLikeConditionAdapter)
        .setPrettyPrinting()
        .create()

    private val effectsMap = mutableMapOf<ResourceLocation, SpawnBait>()

    override fun sync(player: ServerPlayer) {
        SpawnBaitRegistrySyncPacket(this.effectsMap.toMap()).sendToPlayer(player)
    }

    override fun reload(data: Map<ResourceLocation, SpawnBait>) {
        effectsMap.clear()
        data.forEach { id, bait ->
            effectsMap[id] = bait
        }
    }

    @JvmStatic
    fun getEffectsFromRodItemStack(stack: ItemStack): List<SpawnBait.Effect> {
        return getEffectsFromItemStack(stack.components.get(CobblemonItemComponents.BAIT)?.stack ?: ItemStack.EMPTY)
    }

    @JvmStatic
    fun getEffectsFromItemStack(stack: ItemStack): List<SpawnBait.Effect> {
        val componentEffects = stack.get(CobblemonItemComponents.BAIT_EFFECTS)?.effects ?: emptyList()
        return componentEffects.mapNotNull(::getFromIdentifier).flatMap { it.effects } +
                getEffectsFromItem(stack.itemHolder)
    }

    @JvmStatic
    fun getBaitIdentifiersFromItem(holder: Holder<Item>): List<ResourceLocation> {
        return effectsMap.entries.filter { it.value.item.fits(holder) }.map { it.key }
    }

    @JvmStatic
    fun getBaitFromItemStack(holder: Holder<Item>): List<SpawnBait> {
        return effectsMap.values.filter { it.item.fits(holder) }
    }

    private fun getEffectsFromItem(holder: Holder<Item>): List<SpawnBait.Effect> {
        return getBaitFromItemStack(holder).flatMap { it.effects }
    }

    @JvmStatic
    fun getFromIdentifier(identifier: ResourceLocation): SpawnBait? {
        // Check normal spawn bait registry
        effectsMap[identifier]?.let { return it }

        // check seasoning spawn bait registry
        if (identifier.namespace == "seasonings") {
            val seasoning = com.cobblemon.mod.common.api.cooking.Seasonings.seasonings.find {
                it.ingredient is RegistryLikeIdentifierCondition &&
                        (it.ingredient as RegistryLikeIdentifierCondition<Item>).identifier.path == identifier.path
            }

            if (seasoning != null && !seasoning.baitEffects.isNullOrEmpty()) {
                return SpawnBait(
                    item = seasoning.ingredient,
                    effects = seasoning.baitEffects
                )
            }
        }

        return null
    }

    // if it has bait effects and is not listed as a seasoning then it is a bait
    @JvmStatic
    fun isFishingBait(stack: ItemStack): Boolean {
        val holder = stack.itemHolder
        return effectsMap.values.any { it.item.fits(holder) }
    }
}