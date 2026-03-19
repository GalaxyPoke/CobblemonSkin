/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.pot

import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.util.lang
import net.minecraft.network.chat.Component

/**
 * Used for aprijuice quality.
 */
enum class CookingQuality {
    LOW {
        override fun getLang(): Component = lang("cooking.cooking_quality.low").red()
    },
    MEDIUM {
        override fun getLang(): Component = lang("cooking.cooking_quality.medium").yellow()
    },
    HIGH {
        override fun getLang(): Component = lang("cooking.cooking_quality.high").green()
    };

    abstract fun getLang(): Component
}