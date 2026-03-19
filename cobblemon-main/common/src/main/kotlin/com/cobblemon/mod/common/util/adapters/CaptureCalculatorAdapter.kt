/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculators
import com.cobblemon.mod.common.pokeball.catching.calculators.CobblemonCaptureCalculator
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 * A type adapter for [CaptureCalculator]s.
 * This will never fail to deserialize instead default to the [CobblemonCaptureCalculator] and log an error message.
 *
 * @author Licious
 * @since January 30th, 2023
 */
object CaptureCalculatorAdapter : JsonDeserializer<CaptureCalculator>, JsonSerializer<CaptureCalculator> {
    override fun deserialize(element: JsonElement, type: Type, context: JsonDeserializationContext): CaptureCalculator {
        val id = element.asString.lowercase()
        val captureCalculator = CaptureCalculators.fromId(id)
        if (captureCalculator == null) {
            Cobblemon.LOGGER.error("Failed to load CaptureCalculator from the ID {} defaulting to the {}", id, CobblemonCaptureCalculator::class.simpleName)
            return CobblemonCaptureCalculator
        }
        return captureCalculator
    }

    override fun serialize(calculator: CaptureCalculator, type: Type, context: JsonSerializationContext): JsonElement = JsonPrimitive(calculator.id().lowercase())

}