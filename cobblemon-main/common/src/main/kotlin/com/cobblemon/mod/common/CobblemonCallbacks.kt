/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.MoParams
import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.scripting.CobblemonScripts
import com.cobblemon.mod.common.net.messages.client.data.CallbackRegistrySyncPacket
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.endsWith
import java.util.concurrent.ExecutionException
import java.util.function.Predicate
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager

/**
 * Holds all the callbacks that are loaded from the server's data packs. A callback is foldered under the event identifier,
 * which can be Cobblemon's namespace but can also be minecraft or any other namespace someone has added hooks for.
 * The value of the map is a list of MoLang scripts that will execute when that event occurs.
 *
 * @author Hiroku
 * @since February 24th, 2024
 */
object CobblemonCallbacks : DataRegistry {
    override val id = cobblemonResource("callbacks")
    override val observable = SimpleObservable<CobblemonCallbacks>()
    override val type = PackType.SERVER_DATA
    override fun sync(player: ServerPlayer) {
        player.sendPacket(CallbackRegistrySyncPacket(clientCallbacks.entries))
    }

    val runtime by lazy { MoLangRuntime().setup() } // Lazy for if someone adds to generalFunctions in MoLangFunctions

    val clientCallbacks = hashMapOf<ResourceLocation, MutableList<ExpressionLike>>()
    val callbacks = hashMapOf<ResourceLocation, MutableList<ExpressionLike>>()


    override fun reload(manager: ResourceManager) {
        clientCallbacks.clear()
        callbacks.clear()

        val unsortedCallbacks = mutableMapOf<ResourceLocation, MutableList<Pair<String, ExpressionLike>>>()
        val folderBeforeNameRegex = ".*\\/([^\\/]+)\\/[^\\/]+\$".toRegex()
        val predicate: Predicate<ResourceLocation> = Predicate { path -> path.endsWith(CobblemonScripts.MOLANG_EXTENSION) }

        manager.listResources("callbacks", predicate)
            .plus(manager.listResources("flows", predicate)) // old name
            .forEach { (identifier, resource) ->
                resource.openAsReader().use { stream ->
                    stream.buffered().use { reader ->
                        try {
                            val expression = reader.readText().asExpressionLike()

                            val event = folderBeforeNameRegex.find(identifier.path)?.groupValues?.get(1)
                                ?: throw IllegalArgumentException("Invalid callback path: $identifier. Should have a folder structure that includes the name of the event to callback.")

                            val callbackKey = ResourceLocation.fromNamespaceAndPath(identifier.namespace, event)
                            unsortedCallbacks.putIfAbsent(callbackKey, mutableListOf())
                            unsortedCallbacks[callbackKey]!!.add(identifier.path to expression)
                        } catch (exception: Exception) {
                            throw ExecutionException("Error loading MoLang script for callback: $identifier", exception)
                        }
                    }
                }
        }

        unsortedCallbacks.forEach { (key, value) ->
            value.sortBy { it.first }
            if (key.path.startsWith("flows/client/") || key.path.startsWith("callbacks/client/")) {
                clientCallbacks.getOrPut(key) { mutableListOf() }.addAll(value.map { it.second })
            } else {
                callbacks.getOrPut(key) { mutableListOf() }.addAll(value.map { it.second })
            }
        }

        Cobblemon.LOGGER.info("Loaded ${callbacks.size} callbacks and ${clientCallbacks.size} client callbacks")
        observable.emit(this)
    }

    fun run(
        eventResourceLocation: ResourceLocation,
        context: Map<String, MoValue>,
        functions: Map<String, (MoParams) -> Any> = emptyMap(),
        cancelable: Cancelable? = null
    ) {
        if (eventResourceLocation !in callbacks) return

        if (cancelable == null) {
            runtime.environment.query.functions.remove("cancel")
        } else {
            runtime.environment.query.addFunction("cancel") { cancelable }
        }

        // Most places in the mod use query structs so it's probably nicer to present the things as query values.
        context.forEach { (key, value) -> runtime.environment.query.addFunction(key) { return@addFunction value } }

        functions.forEach { (name, function) -> runtime.environment.query.addFunction(name, function) }

        callbacks[eventResourceLocation]?.forEach {
            if (cancelable != null && cancelable.isCanceled) {
                return
            }
            it.resolve(runtime, context)
        }
    }
}