/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.codec

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

fun <A, B> pairCodec(codecA: Codec<A>, codecB: Codec<B>): Codec<Pair<A, B>> {
    return RecordCodecBuilder.create { instance ->
        instance.group(
            codecA.fieldOf("first").forGetter { it.first },
            codecB.fieldOf("second").forGetter { it.second },
        ).apply(instance) { a, b -> a to b }
    }
}