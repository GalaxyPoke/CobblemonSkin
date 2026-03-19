/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.npc

import com.cobblemon.mod.common.CobblemonBehaviours
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.config.AddVariablesConfig
import com.cobblemon.mod.common.api.ai.config.ApplyBehaviours
import com.cobblemon.mod.common.api.ai.config.BehaviourConfig
import com.cobblemon.mod.common.api.npc.NPCClass
import com.mojang.serialization.Dynamic

object NPCBrain {
    fun configure(npcEntity: NPCEntity, npcClass: NPCClass, dynamic: Dynamic<*>) {
        var behaviourConfigurations: List<BehaviourConfig> = CobblemonBehaviours.autoNPCBehaviours.flatMap { it.configurations } + npcClass.behaviours
        if (npcEntity.behavioursAreCustom) {
            behaviourConfigurations = listOf(ApplyBehaviours().apply { behaviours.addAll(npcEntity.behaviours) })
        }
        behaviourConfigurations = behaviourConfigurations + AddVariablesConfig(npcClass.config)

        val ctx = BehaviourConfigurationContext()
        ctx.addMemories(CobblemonMemories.DIALOGUES)
        ctx.addMemories(CobblemonMemories.NPC_BATTLING)
        ctx.apply(npcEntity, behaviourConfigurations, dynamic)
        npcEntity.behaviours.clear()
        npcEntity.behaviours.addAll(ctx.appliedBehaviours)
    }
}