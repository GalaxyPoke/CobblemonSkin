/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.conditional

import net.minecraft.core.Holder
import net.minecraft.core.Registry

/**
 * A condition which applies to an entry in some [Registry].
 *
 * @author Hiroku
 * @since July 16th, 2022
 */
interface RegistryLikeCondition<T> {
    fun fits(t: T, registry: Registry<T>): Boolean {
        val holder = registry.wrapAsHolder(t)
        return holder != null && fits(holder)
    }

    fun fits(holder: Holder<T>): Boolean
}