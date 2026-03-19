/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.permission

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.util.cobblemonResource

data class CobblemonPermission(
    private val node: String,
    override val level: PermissionLevel
) : Permission {
    
    override val identifier = cobblemonResource(this.node)

    override val literal = "${Cobblemon.MODID}.${this.node}"
}
