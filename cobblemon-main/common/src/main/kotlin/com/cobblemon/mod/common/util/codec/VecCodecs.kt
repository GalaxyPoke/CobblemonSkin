/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.codec

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import org.joml.Vector3f

val VECTOR3F_CODEC: Codec<Vector3f> = RecordCodecBuilder.create { instance ->
    instance.group(
        Codec.FLOAT.fieldOf("x").forGetter { it.x },
        Codec.FLOAT.fieldOf("y").forGetter { it.y },
        Codec.FLOAT.fieldOf("z").forGetter { it.z }
    ).apply(instance) { x, y, z -> Vector3f(x, y, z) }
}