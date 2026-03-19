/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.modifiers

import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import kotlin.reflect.KClass

/**
 * Saves and loads [CatchRateModifier]s with JSON.
 * For the default implementation see [CobbledCatchRateModifierAdapter].
 *
 * @author Licious
 * @since August 8th, 2022
 */
interface CatchRateModifierAdapter : JsonDeserializer<CatchRateModifier>, JsonSerializer<CatchRateModifier> {

    /**
     * Registers the given type of [CatchRateModifier] to it's associated ID for deserialization.
     *
     * @param T The type of [CatchRateModifier].
     * @param id The id of the type.
     * @param type The [KClass] of the type.
     */
    fun <T : CatchRateModifier> registerType(id: String, type: KClass<T>)

}
