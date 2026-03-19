/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.snowstorm

import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.util.codec.optionalFieldOfWithDefault
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import org.joml.AxisAngle4f
import org.joml.Matrix4f
import org.joml.Vector3f

class EmitterSpace(
    var scaling: ScalingMode = ScalingMode.WORLD
) {
    companion object {
        val CODEC: Codec<EmitterSpace> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.optionalFieldOfWithDefault("scaling", "world").forGetter { it.scaling.toString() }
            ).apply(instance) { scaling ->
                EmitterSpace(
                    ScalingMode.valueOf(scaling.uppercase())
                )
            }
        }
    }

    fun initializeEmitterMatrix(rootMatrix: MatrixWrapper, locatorMatrix: MatrixWrapper): MatrixWrapper {
        var rootRotation = rootMatrix.matrix.getRotation(AxisAngle4f())
        //When the locator hasn't yet been initialized, we start getting NaNs, so default to no rotation instead
        if (rootRotation.x.isNaN() || rootRotation.y.isNaN() || rootRotation.z.isNaN() || rootRotation.angle.isNaN()) {
            rootRotation = AxisAngle4f()
        }
        val scale = Vector3f(1f, 1f, 1f)

        if (scaling == ScalingMode.ENTITY) {
            locatorMatrix.matrix.getScale(scale)

            // If any value is zero we get NaNs, so default to a large enough value to be safe
            scale.x.coerceAtLeast(.01f)
            scale.y.coerceAtLeast(.01f)
            scale.z.coerceAtLeast(.01f)
        }

        //Presumably we will want to make the initial rotation configurable instead of always using root
        val particleRawMatrix = Matrix4f().scale(scale).rotate(rootRotation)
        return MatrixWrapper().updateMatrix(particleRawMatrix).updatePosition(locatorMatrix.getOrigin())
    }

    enum class ScalingMode {
        WORLD,
        ENTITY
    }
}