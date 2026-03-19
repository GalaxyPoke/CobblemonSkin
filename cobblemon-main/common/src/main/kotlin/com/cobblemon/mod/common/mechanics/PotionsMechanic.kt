/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mechanics

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.readExpressionLike
import com.cobblemon.mod.common.util.writeExpressionLike
import net.minecraft.network.RegistryFriendlyByteBuf

class PotionsMechanic(
    val potionRestoreAmount: ExpressionLike = "60".asExpressionLike(),
    val superPotionRestoreAmount: ExpressionLike = "100".asExpressionLike(),
    val hyperPotionRestoreAmount: ExpressionLike = "150".asExpressionLike(),
) {
    internal fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeExpressionLike(potionRestoreAmount)
        buffer.writeExpressionLike(superPotionRestoreAmount)
        buffer.writeExpressionLike(hyperPotionRestoreAmount)
    }

    companion object {
        internal fun decode(buffer: RegistryFriendlyByteBuf): PotionsMechanic {
            val mechanic = PotionsMechanic(
                buffer.readExpressionLike(),
                buffer.readExpressionLike(),
                buffer.readExpressionLike(),
            )

            return mechanic
        }
    }
}