/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client

import com.cobblemon.mod.common.ResourcePackActivationBehaviour
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.PackType

class CobblemonPack(
    val id: String,
    val name: String,
    val packType: PackType,
    val activationBehaviour: ResourcePackActivationBehaviour,
    val neededMods: Set<String> = setOf()
) {
    val displayName = Component.literal(name)
}