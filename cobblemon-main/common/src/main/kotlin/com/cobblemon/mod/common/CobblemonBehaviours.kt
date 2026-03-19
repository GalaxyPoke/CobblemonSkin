/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.ai.CobblemonBehaviour
import com.cobblemon.mod.common.api.ai.config.BehaviourConfig
import com.cobblemon.mod.common.api.ai.config.task.TaskConfig
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.data.JsonDataRegistry.Companion.JSON_EXTENSION
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.data.BehaviourSyncPacket
import com.cobblemon.mod.common.util.adapters.ActivityAdapter
import com.cobblemon.mod.common.util.adapters.BehaviourConfigAdapter
import com.cobblemon.mod.common.util.adapters.ExpressionAdapter
import com.cobblemon.mod.common.util.adapters.ExpressionLikeAdapter
import com.cobblemon.mod.common.util.adapters.ExpressionOrEntityVariableAdapter
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.adapters.MemoryModuleTypeAdapter
import com.cobblemon.mod.common.util.adapters.SensorTypeAdapter
import com.cobblemon.mod.common.util.adapters.TaskConfigAdapter
import com.cobblemon.mod.common.util.adapters.TranslatedTextAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.endsWith
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mojang.datafixers.util.Either
import java.io.File
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType
import net.minecraft.world.entity.schedule.Activity

object CobblemonBehaviours : JsonDataRegistry<CobblemonBehaviour> {
    override val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Activity::class.java, ActivityAdapter)
        .registerTypeAdapter(Expression::class.java, ExpressionAdapter)
        .registerTypeAdapter(ExpressionLike::class.java, ExpressionLikeAdapter)
        .registerTypeAdapter(MemoryModuleType::class.java, MemoryModuleTypeAdapter)
        .registerTypeAdapter(SensorType::class.java, SensorTypeAdapter)
        .registerTypeAdapter(BehaviourConfig::class.java, BehaviourConfigAdapter)
        .registerTypeAdapter(TaskConfig::class.java, TaskConfigAdapter)
        .registerTypeAdapter(
            TypeToken.getParameterized(Either::class.java, Expression::class.java, MoLangConfigVariable::class.java).type,
            ExpressionOrEntityVariableAdapter
        )
        .registerTypeAdapter(Component::class.java, TranslatedTextAdapter)
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .create()

    override val typeToken = TypeToken.get(CobblemonBehaviour::class.java)
    override val resourcePath = "behaviours"
    override val id: ResourceLocation = cobblemonResource("behaviours")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<CobblemonBehaviours>()

    /**
     * Any behaviours that are put into /pokemon/auto will be automatically
     * applied to all Pokémon species/forms that don't define a baseAI list of
     * behaviour configurations. This is an addon-friendly way of defining the general
     * rules of Pokémon AI without it being unavoidable for specific
     * species.
     */
    val autoPokemonBehaviours = mutableListOf<CobblemonBehaviour>()

    /** See [autoPokemonBehaviours] but think npc instead of pokemon*/
    val autoNPCBehaviours = mutableListOf<CobblemonBehaviour>()

    val behaviours = mutableMapOf<ResourceLocation, CobblemonBehaviour>()

    override fun sync(player: ServerPlayer) {
        player.sendPacket(BehaviourSyncPacket(behaviours.filter { it.value.visible }))
    }

    override fun reload(manager: ResourceManager) {
        autoPokemonBehaviours.clear()
        autoNPCBehaviours.clear()
        val data = mutableMapOf<ResourceLocation, CobblemonBehaviour>()
        manager.listResources(resourcePath) { path -> path.endsWith(JSON_EXTENSION) }.forEach { (identifier, resource) ->
            resource.open().use { stream ->
                stream.bufferedReader().use { reader ->
                    val resolvedIdentifier = ResourceLocation.fromNamespaceAndPath(identifier.namespace, File(identifier.path).nameWithoutExtension)
                    val behaviour = parse(reader, resolvedIdentifier)
                    if ("pokemon/auto/" in identifier.path) {
                        autoPokemonBehaviours.add(behaviour)
                    } else if ("npc/auto/" in identifier.path) {
                        autoNPCBehaviours.add(behaviour)
                    }
                    data[resolvedIdentifier] = behaviour
                }
            }
        }

        reload(data)
        Cobblemon.LOGGER.info("Loaded ${behaviours.size} entity behaviours")
        observable.emit(this)
    }

    override fun reload(data: Map<ResourceLocation, CobblemonBehaviour>) {
        behaviours.clear()
        behaviours.putAll(data)
    }
}