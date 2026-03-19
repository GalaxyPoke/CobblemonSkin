/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.pc

import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

/**
 * A server-defined wallpaper for PC boxes that are not immediately usable. Typically a command
 * or a code action is required to add the wallpaper to the unlock list.
 *
 * @author Hiroku
 * @since February 9th, 2025
 */
class UnlockablePCWallpaper {
    @Transient
    lateinit var id: ResourceLocation
    /** This is for when we provide a wallpaper but some server wants to remove it from the unlockables. */
    var enabled = true
    /** If unlocked with a notification toast, this is the name displayed in the toast. If blank, ??? will be used. */
    var displayName: String? = null
    var texture: ResourceLocation = cobblemonResource("dummy.png")
    // I feel like there's more that could be added to this. Things like whether it displays when still locked, and if so what kind of hint to provide.
    // That requires client side changes which means it requires The Expert which is why I'm not committing to it right this second.

    @Transient
    val struct = ObjectValue(this).also {
        it.addFunction("texture") { StringValue(texture.toString()) }
        it.addFunction("id") { StringValue(id.toString()) }
        it.addFunction("enabled") { DoubleValue(enabled) }
    }
}