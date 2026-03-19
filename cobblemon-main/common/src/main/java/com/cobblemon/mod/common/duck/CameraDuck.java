/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.duck;

import com.mongodb.lang.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.joml.Vector3f;

public interface CameraDuck {
    BlockGetter cobblemon$getLevel();

    @Nullable
    Entity cobblemon$getEntity();

    Vector3f cobblemon$getForwards();

    Vector3f cobblemon$getUp();

    Vector3f cobblemon$getLeft();

    float cobblemon$getXRot();
    void cobblemon$setXRot(float xRot);

    float cobblemon$getYRot();
    void cobblemon$setYRot(float yRot);
}
