/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.cooking

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance

data class SerializableMobEffectInstance(
        val effect: ResourceLocation,
        val duration: Int,
        val amplifier: Int = 0,
        val ambient: Boolean = false,
        val visible: Boolean = true,
        val showIcon: Boolean = true
) {
    fun toInstance(): MobEffectInstance {
        val holder = BuiltInRegistries.MOB_EFFECT.getHolder(effect)
                .orElseThrow { IllegalArgumentException("Unknown MobEffect: $effect") }
        return MobEffectInstance(holder, duration, amplifier, ambient, visible, showIcon)
    }

    companion object {
        val CODEC: Codec<SerializableMobEffectInstance> = RecordCodecBuilder.create { builder ->
            builder.group(
                    ResourceLocation.CODEC.fieldOf("effect").forGetter { it.effect },
                    Codec.INT.fieldOf("duration").forGetter { it.duration },
                    Codec.INT.fieldOf("amplifier").forGetter { it.amplifier },
                    Codec.BOOL.optionalFieldOf("ambient", false).forGetter { it.ambient },
                    Codec.BOOL.optionalFieldOf("visible", true).forGetter { it.visible },
                    Codec.BOOL.optionalFieldOf("showIcon", true).forGetter { it.showIcon }
            ).apply(builder, ::SerializableMobEffectInstance)
        }
    }
}
