/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.events

import com.bedrockk.molang.runtime.MoParams
import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents.THROWN_POKEBALL_HIT
import com.cobblemon.mod.common.api.events.pokeball.ThrownPokeballHitEvent
import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.entity.EntityCallbacks
import com.cobblemon.mod.common.entity.EntityCallbacks.Companion.HIT_BY_POKEBALL
import com.cobblemon.mod.common.entity.MoLangScriptingEntity
import net.minecraft.resources.ResourceLocation

/**
 * Handles the registration of entity callbacks for events related to entities. You can add your own
 * as well, all this actually does is subscribe to the event and pass it through the [EntityCallbacks] of
 * the entity the event is about. In general, entity callbacks occur on the normal priority so that
 * plugins can jump in ahead but on parity with the general MoLang callbacks.
 *
 * @author Hiroku
 * @since July 26th, 2025
 */
object EntityCallbackHandler {
    fun setup() {
        bindCallback(THROWN_POKEBALL_HIT, HIT_BY_POKEBALL, ThrownPokeballHitEvent::pokemon, ThrownPokeballHitEvent::functions)
    }

    fun <T> bindCallback(
        observable: Observable<T>,
        type: ResourceLocation,
        entity: (T) -> MoLangScriptingEntity,
        functions: (T) -> Map<String, (MoParams) -> MoValue>
    ) {
        observable.subscribe(priority = Priority.NORMAL) { entity(it).callbacks.process(type, functions(it)) }
    }
}