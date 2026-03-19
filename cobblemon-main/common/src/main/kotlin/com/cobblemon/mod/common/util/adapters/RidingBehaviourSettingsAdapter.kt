/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.air.*
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.strategies.FallCompositeSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.strategies.FallStrategy
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.strategies.JumpStrategy
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.strategies.RunStrategy
import com.cobblemon.mod.common.api.riding.behaviour.types.land.HorseBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.land.HorseSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.land.MinekartBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.land.MinekartSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.land.VehicleBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.land.VehicleSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.liquid.*
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import net.minecraft.resources.ResourceLocation
import java.lang.reflect.Type

/**
 * Adapter for deserializing [RidingBehaviourSettings] types.
 *
 * @author landonjw
 */
object RidingBehaviourSettingsAdapter : JsonDeserializer<RidingBehaviourSettings?> {
    val types: MutableMap<ResourceLocation, Class<out RidingBehaviourSettings>> = mutableMapOf(
        BirdBehaviour.KEY to BirdSettings::class.java,
        DolphinBehaviour.KEY to DolphinSettings::class.java,
        HorseBehaviour.KEY to HorseSettings::class.java,
        BoatBehaviour.KEY to BoatSettings::class.java,
        GliderBehaviour.KEY to GliderSettings::class.java,
        HelicopterBehaviour.KEY to HelicopterSettings::class.java,
        JetBehaviour.KEY to JetSettings::class.java,
        BurstBehaviour.KEY to BurstSettings::class.java,
        VehicleBehaviour.KEY to VehicleSettings::class.java,
        MinekartBehaviour.KEY to MinekartSettings::class.java,
        HoverBehaviour.KEY to HoverSettings::class.java,
        RocketBehaviour.KEY to RocketSettings::class.java,
        SubmarineBehaviour.KEY to SubmarineSettings::class.java,
        CompositeBehaviour.KEY to CompositeSettings::class.java,

        /*
         Strategy registration. if you do not register a strategy here, it will not be deserialized.
         Register to CompositeSettings if you do not need to define a subclass.
         */
        FallStrategy.key to FallCompositeSettings::class.java,
        JumpStrategy.key to CompositeSettings::class.java,
        RunStrategy.key to CompositeSettings::class.java,
    )

    override fun deserialize(element: JsonElement, type: Type, context: JsonDeserializationContext): RidingBehaviourSettings? {
        val root = element.asJsonObject
        val key = root.get("key").asString
        val keyIdentifier = key.asIdentifierDefaultingNamespace()
        if (keyIdentifier == CompositeBehaviour.KEY) {
            val strategy = root.get("transitionStrategy").asString
            val strategyIdentifier = strategy.asIdentifierDefaultingNamespace()
            val behaviourType = types[strategyIdentifier]
            if (behaviourType == null) {
                Cobblemon.LOGGER.warn("Unknown strategy: $strategyIdentifier for composite behaviour: $key. Skipping.")
                return null
            }
            return context.deserialize(root, behaviourType)
        }
        else {
            val behaviourType = types[keyIdentifier]
            if (behaviourType == null) {
                Cobblemon.LOGGER.warn("Unknown riding behaviour encountered: $key. Skipping.")
                return null
            }
            return context.deserialize(element, behaviourType)
        }
    }
}
