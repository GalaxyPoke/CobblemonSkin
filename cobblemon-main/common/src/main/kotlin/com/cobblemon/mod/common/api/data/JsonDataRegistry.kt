/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.data

import com.cobblemon.mod.common.util.endsWith
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.util.concurrent.ExecutionException
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager

/**
 * A [DataRegistry] that consumes JSON files.
 * Every deserialized instance is attached to an [ResourceLocation].
 * For example a file under data/mymod/[resourcePath]/entry.json would be backed by the identifier modid:entry.
 *
 * @param T The type of the data consumed by this registry.
 *
 * @author Licious
 * @since August 5th, 2022
 */
interface JsonDataRegistry<T> : DataRegistry {

    /**
     * The [Gson] used to deserialize the data this registry will consume.
     */
    val gson: Gson

    /**
     * The [TypeToken] of type [T].
     */
    val typeToken: TypeToken<T>

    /**
     * The folder location for the data this registry will consume.
     */
    val resourcePath: String

    override fun reload(manager: ResourceManager) {
        val data = hashMapOf<ResourceLocation, T>()
        manager.listResources(resourcePath) { path -> path.endsWith(JSON_EXTENSION) }.forEach { (identifier, resource) ->
            if (identifier.namespace == "pixelmon") {
                return@forEach
            }

            resource.open().use { stream ->
                stream.bufferedReader().use { reader ->
                    val resolvedIdentifier = ResourceLocation.fromNamespaceAndPath(identifier.namespace, File(identifier.path).nameWithoutExtension)
                    data[resolvedIdentifier] = parse(reader, resolvedIdentifier)
                }
            }
        }

        reload(data)
    }

    fun parse(reader: BufferedReader, identifier: ResourceLocation): T {
        return try {
            gson.fromJson(reader, typeToken.type)
        } catch (exception: Exception) {
            throw ExecutionException("Error loading JSON for data: $identifier", exception)
        }
    }

    /**
     * Reloads this registry from the deserialized data.
     *
     * @param data A map of the data associating an instance to the respective identifier from the [ResourceManager].
     */
    fun reload(data: Map<ResourceLocation, T>)

    companion object {
        const val JSON_EXTENSION = ".json"
    }
}