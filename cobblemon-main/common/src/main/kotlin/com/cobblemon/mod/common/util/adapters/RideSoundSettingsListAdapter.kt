/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.riding.sound.RideSoundSettingsList
import com.cobblemon.mod.common.api.riding.sound.RideSoundSettings
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Class to control playing all ride sounds
 *
 * @author Jackowes
 * @since April 26th, 2025
 */
object RideSoundSettingsListAdapter: JsonDeserializer<RideSoundSettingsList?> {
    override fun deserialize(
        element: JsonElement,
        type: Type,
        context: JsonDeserializationContext
    ): RideSoundSettingsList {
        val sounds = when {
            element.isJsonArray -> {
                element.asJsonArray.map {
                    context.deserialize(it, RideSoundSettings::class.java)
                }
            }
            element.isJsonObject -> {
                listOf(context.deserialize<RideSoundSettings>(element, RideSoundSettings::class.java))
            }
            else -> {
                emptyList()
            }
        }
        return RideSoundSettingsList(sounds)
    }
}