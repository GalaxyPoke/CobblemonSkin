/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour

import com.cobblemon.mod.common.api.riding.behaviour.types.air.*
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.land.HorseBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.land.VehicleBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.land.MinekartBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.liquid.BoatBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.liquid.BurstBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.liquid.DolphinBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.liquid.SubmarineBehaviour
import net.minecraft.resources.ResourceLocation

object RidingBehaviours {
    val behaviours = mutableMapOf<ResourceLocation, RidingBehaviour<RidingBehaviourSettings, RidingBehaviourState>>()

    init {
        register(BirdBehaviour.KEY, BirdBehaviour())
        register(DolphinBehaviour.KEY, DolphinBehaviour())
        register(HorseBehaviour.KEY, HorseBehaviour())
        register(BoatBehaviour.KEY, BoatBehaviour())
        register(GliderBehaviour.KEY, GliderBehaviour())
        register(HelicopterBehaviour.KEY, HelicopterBehaviour())
        register(JetBehaviour.KEY, JetBehaviour())
        register(BurstBehaviour.KEY, BurstBehaviour())
        register(VehicleBehaviour.KEY, VehicleBehaviour())
        register(MinekartBehaviour.KEY, MinekartBehaviour())
        register(HoverBehaviour.KEY, HoverBehaviour())
        register(RocketBehaviour.KEY, RocketBehaviour())
        register(SubmarineBehaviour.KEY, SubmarineBehaviour())
        register(CompositeBehaviour.KEY, CompositeBehaviour())
    }

    @JvmStatic
    fun register(key: ResourceLocation, behaviour: RidingBehaviour<out RidingBehaviourSettings, out RidingBehaviourState>) {
        if (behaviours.contains(key)) error("Behaviour already registered to key $key")
        behaviours[key] = behaviour as RidingBehaviour<RidingBehaviourSettings, RidingBehaviourState>
    }

    @JvmStatic
    fun get(key: ResourceLocation): RidingBehaviour<RidingBehaviourSettings, RidingBehaviourState> {
        if (!behaviours.contains(key)) error("Behaviour not registered to key $key")
        return behaviours[key]!!
    }
}