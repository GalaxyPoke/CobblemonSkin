/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.item

import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.battles.runner.ShowdownService
import com.cobblemon.mod.common.pokemon.helditem.CobblemonHeldItemManager
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import java.io.File

object HeldItems : DataRegistry {

    override val id = cobblemonResource("held_items")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<HeldItems>()
    override fun sync(player: ServerPlayer) {}

    // val heldItems = PrioritizedList<BagItemLike>()
    internal val heldItemsScripts = mutableMapOf<String, String>() // showdown item ID to JavaScript
    val showdownItems = hashSetOf<String>()

    override fun reload(manager: ResourceManager) {
        heldItemsScripts.clear()
        ShowdownService.service.resetRegistryData("heldItem")
        manager.listResources("held_items") { it.path.endsWith(".js") }.forEach { (identifier, resource) ->
            resource.open().use { stream ->
                stream.bufferedReader().use { reader ->
                    val resolvedIdentifier = ResourceLocation.fromNamespaceAndPath(identifier.namespace, File(identifier.path).nameWithoutExtension)
                    val js = reader.readText()
                    heldItemsScripts[resolvedIdentifier.path] = js
                }
            }
        }
        ShowdownService.service.sendRegistryData(heldItemsScripts, "heldItem")

        val itemsJson = ShowdownService.service.getRegistryData("heldItem")
        for (i in 0 until itemsJson.size()) {
            val jsItem = itemsJson[i].asJsonObject
            showdownItems += jsItem.get("id").asString
        }

        CobblemonHeldItemManager.load()

        this.observable.emit(this)
    }
}