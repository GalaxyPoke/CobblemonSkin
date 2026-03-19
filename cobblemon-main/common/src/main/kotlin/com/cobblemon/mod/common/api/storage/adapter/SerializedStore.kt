/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.adapter

import com.cobblemon.mod.common.api.storage.PokemonStore
import java.util.UUID

data class SerializedStore<S>(val storeClass: Class<out PokemonStore<*>>, val uuid: UUID, val serializedForm: S)