/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client

import org.joml.Quaternionf
import org.spongepowered.asm.mixin.Unique

interface RidingCameraInterface {
    // Used to help in camera rotation rendering when transitioning
    // from rolled to unrolled.
    @Unique
    fun `getCobblemon$returnTimer`(): Float

    @Unique
    fun `setCobblemon$returnTimer`(time: Float)

    @Unique
    fun `getCobblemon$lastHandledRotationTime`(): Double

    @Unique
    fun `setCobblemon$lastHandledRotationTime`(time: Double)

    @Unique
    fun `getCobblemon$frameTime`(): Double

    @Unique
    fun `setCobblemon$frameTime`(time: Double)

    @Unique
    fun `getCobblemon$partialTickTime`(): Float

    @Unique
    fun `setCobblemon$partialTickTime`(time: Float)

    @Unique
    fun `getCobblemon$rollAngleStart`(): Float

    @Unique
    fun `setCobblemon$rollAngleStart`(angle: Float)

    fun `getCobblemon$smoothRotation`(): Quaternionf?

    fun `setCobblemon$smoothRotation`(smoothRotation: Quaternionf)
}