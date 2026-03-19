/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.api.storage.StorePosition
import com.cobblemon.mod.common.pokemon.Pokemon

open class PCGUIConfiguration(
    val exitFunction: (PCGUI) -> Unit = { it.closeNormally(unlink = true) },
    val selectOverride: ((PCGUI, StorePosition, Pokemon?) -> Unit)? = null,
    var showParty: Boolean = true,
    val canSelect: (Pokemon) -> Boolean = { true }
)