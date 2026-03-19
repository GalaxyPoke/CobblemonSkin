/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.schedule.Activity

class SetDefaultActivity : BehaviourConfig {
    val activity: Activity = Activity.IDLE

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = emptyList<MoLangConfigVariable>()
    override fun configure(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) {
        behaviourConfigurationContext.defaultActivity = activity
    }
}