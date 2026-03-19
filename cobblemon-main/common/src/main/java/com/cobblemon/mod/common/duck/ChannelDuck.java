/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.duck;

public interface ChannelDuck {
    void cobblemon$applyLowPassFilter(float gain, float hfGain);
    void cobblemon$inverseAttenuation(float rolloffFactor);
    void cobblemon$clearFilters();

//    void cobblemon$exponentialAttenuation(float rolloff, float refDistance, float maxDistance);
}
