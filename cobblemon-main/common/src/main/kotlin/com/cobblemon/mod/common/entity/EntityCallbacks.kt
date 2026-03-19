/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.MoParams
import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.api.ai.CobblemonBehaviour
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.scripting.CobblemonScripts
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.resolve
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity

/**
 * A class that holds callbacks for an entity, allowing scripts to be associated with specific events.
 * Mainly keeps code bloat away from the entity classes. There's likely to be some overlap between these
 * and the datapacked MoLang callbacks but the difference is that these are per-entity which allows
 * [CobblemonBehaviour]s to add callbacks very gracefully, rather than running the same code for all
 * entities then looking for some other property on the entity.
 *
 * @author Hiroku
 * @since July 26th, 2025
 */
class EntityCallbacks(val entity: Entity) : HashMap<ResourceLocation, MutableList<ResourceLocation>>() {
    companion object {
        val HIT_BY_POKEBALL = cobblemonResource("hit_by_pokeball")
        val SAW_ENTITY = cobblemonResource("saw_entity")
        val HURT = cobblemonResource("hurt")
        val DIED = cobblemonResource("died")
    }

    val runtime = MoLangRuntime().setup().also {
        it.environment.query.addFunction("entity") { entity.asMostSpecificMoLangValue() }
    }

    /**
     * Adds a script to the callbacks for a specific type. If `allowDuplicates` is false, it will not add
     * the script if it already exists in the list for that type.
     *
     * Returns true if the script was added, false if it was not added due to a duplicate being found and respected.
     */
    fun addCallback(type: ResourceLocation, callback: ResourceLocation, allowDuplicates: Boolean): Boolean {
        val callbacks = this.getOrPut(type) { mutableListOf() }
        if (!allowDuplicates && callback in callbacks) {
            return false
        }
        callbacks.add(callback)
        return true
    }

    fun removeCallback(type: ResourceLocation, callback: ResourceLocation): Boolean {
        val callbacks = this[type] ?: return false
        return callbacks.remove(callback)
    }

    fun process(type: ResourceLocation, functions: Map<String, (MoParams) -> MoValue> = hashMapOf()) {
        if (type !in this) {
            return
        }
        functions.forEach { (name, function) -> runtime.environment.query.addFunction(name, function) }
        val callbacks = this[type]?.mapNotNull { CobblemonScripts.scripts[it] } ?: return
        for (callback in callbacks) {
            runtime.resolve(callback)
        }
    }
}