/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.molang

import com.bedrockk.molang.runtime.struct.VariableStruct
import java.util.UUID

interface MoLangDataStoreFactory {
    fun markDirty(uuid: UUID)
    fun load(uuid: UUID, filePath: String? = null) : VariableStruct
}