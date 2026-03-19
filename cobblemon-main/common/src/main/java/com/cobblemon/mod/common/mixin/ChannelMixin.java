/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.duck.ChannelDuck;
import com.mojang.blaze3d.audio.Channel;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.EXTEfx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Channel.class)
public abstract class ChannelMixin implements ChannelDuck {

    @Shadow
    private int source;

    // Id of filter applied when sound is obstructed
    private int cobblemon$lowPassFilterId = 0;

    @Override
    public void cobblemon$applyLowPassFilter(float gain, float hfGain) {
        // Delete old filter if it exists
        if (cobblemon$lowPassFilterId != 0) {
            AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, 0); // Detach filter from source
            EXTEfx.alDeleteFilters(cobblemon$lowPassFilterId);  // Delete old filter
            cobblemon$lowPassFilterId = 0;
        }

        // Generate a fresh filter
        int filterId = EXTEfx.alGenFilters();
        if (!EXTEfx.alIsFilter(filterId)) {
            System.err.println("Failed to create OpenAL low-pass filter");
            return;
        }

        EXTEfx.alFilteri(filterId, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
        EXTEfx.alFilterf(filterId, EXTEfx.AL_LOWPASS_GAIN, gain);
        EXTEfx.alFilterf(filterId, EXTEfx.AL_LOWPASS_GAINHF, hfGain);

        AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, filterId);

        // Save the new filter ID
        cobblemon$lowPassFilterId = filterId;
    }

    @Override
    public void cobblemon$inverseAttenuation(float rolloffFactor) {
        AL10.alSourcei(source, AL10.AL_DISTANCE_MODEL, AL10.AL_INVERSE_DISTANCE);
        AL10.alSourcef(source, AL10.AL_ROLLOFF_FACTOR,    rolloffFactor);
        AL10.alSourcef(source, AL10.AL_REFERENCE_DISTANCE, 1.0f);
        AL10.alSourcef(source, AL10.AL_MAX_DISTANCE,       0.0f);
    }

    @Override
    public void cobblemon$clearFilters() {
        AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, 0);

        if (EXTEfx.alIsFilter(cobblemon$lowPassFilterId)) {
            EXTEfx.alDeleteFilters(cobblemon$lowPassFilterId);
            cobblemon$lowPassFilterId = 0;
        }
    }
}
