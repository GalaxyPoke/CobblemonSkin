/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import net.minecraft.world.entity.schedule.Activity

object CobblemonActivities {
    @JvmField
    val activities = mutableListOf<Activity>()
    @JvmField
    val BATTLING = register(Activity("battling"))
    @JvmField
    val ACTION_EFFECT = register(Activity("action_effect"))
    @JvmField
    val NPC_CHATTING = register(Activity("npc_chatting"))
    @JvmField
    val POKEMON_SLEEPING_ACTIVITY = register(Activity("pokemon_sleeping"))
    @JvmField
    val POKEMON_GROW_CROP = register(Activity("pokemon_grow_crop"))
    @JvmField
    val POKEMON_SLEEP_ON_TRAINER_BED = register(Activity("pokemon_sleep_on_trainer_bed"))
    @JvmField
    val POKEMON_HERD = register(Activity("pokemon_herd"))
    @JvmField
    val POKEMON_POLLINATION = register(Activity("pokemon_pollination"))

    @JvmStatic
    fun register(activity: Activity): Activity {
        activities.add(activity)
        return activity
    }
}