/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.storage

import com.cobblemon.mod.common.api.storage.pc.PCPosition
import com.cobblemon.mod.common.api.storage.pc.POKEMON_PER_BOX
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.gui.pc.PCGUI
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import java.util.UUID

class ClientPC(uuid: UUID, boxCount: Int) : ClientStorage<PCPosition>(uuid) {
    val boxes = MutableList(boxCount) { ClientBox() }
    override fun findByUUID(uuid: UUID): Pokemon? {
        boxes.forEach {
            it.forEach {
                if (it != null && it.uuid == uuid) {
                    return it
                }
            }
        }

        return null
    }

    override fun set(position: PCPosition, pokemon: Pokemon?) {
        val box = if (boxes.size > position.box) boxes[position.box] else return
        if (position.slot >= POKEMON_PER_BOX) {
            return
        }
        box.slots[position.slot] = pokemon
    }

    override fun get(position: PCPosition): Pokemon? {
        if (position.slot >= POKEMON_PER_BOX || position.box >= boxes.size) {
            return null
        }
        return boxes[position.box].slots[position.slot]
    }

    override fun getPosition(pokemon: Pokemon): PCPosition? {
        for (boxNumber in boxes.indices) {
            val box = boxes[boxNumber]
            for (slotNumber in box.slots.indices) {
                if (box.slots[slotNumber] == pokemon) {
                    return PCPosition(boxNumber, slotNumber)
                }
            }
        }
        return null
    }

    fun renameBox(boxNumber: Int, name: String?) {
        if (boxes.size > boxNumber) {
            boxes[boxNumber].name = if (name.isNullOrBlank()) null else Component.literal(name).bold()
        }
    }

    fun changeBoxWallpaper(boxNumber: Int, wallpaper: ResourceLocation) {
        if (boxes.size > boxNumber) {
            boxes[boxNumber].wallpaper = wallpaper
        }
    }
}