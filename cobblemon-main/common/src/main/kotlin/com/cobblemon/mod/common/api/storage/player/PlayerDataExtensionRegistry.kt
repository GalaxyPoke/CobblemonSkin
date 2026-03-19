/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player

object PlayerDataExtensionRegistry {

    private val allExtensions = mutableMapOf<String, Class<out PlayerDataExtension>>()

    fun register(name: String, extension: Class<out PlayerDataExtension>, overwrite: Boolean = false): Boolean {
        if (allExtensions.contains(name) && !overwrite)
            return false
        allExtensions[name] = extension
        return true
    }

    fun get(name: String) = allExtensions[name]
    fun getOrException(name: String) = get(name)
        ?: throw IllegalStateException("PlayerDataExtension with name $name was not found.")
    fun count() = allExtensions.size
    fun remove(name: String) = allExtensions.remove(name)
    fun contains(name: String) = allExtensions.containsKey(name)

}