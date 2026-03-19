/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.Equipable

class WearableItem(val name: String): CobblemonItem(Properties()), Equipable {
    companion object {
        const val MODEL_PATH = "item/wearable"
    }

    fun getModel3d(): ResourceLocation = cobblemonResource("${MODEL_PATH}/${this.name}")
    fun getModel2d(): ResourceLocation = cobblemonResource(this.name)

    override fun getEquipmentSlot(): EquipmentSlot { return EquipmentSlot.HEAD }
}