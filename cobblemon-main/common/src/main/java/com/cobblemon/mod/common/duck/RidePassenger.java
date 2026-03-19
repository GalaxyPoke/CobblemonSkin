/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.duck;

import net.minecraft.world.phys.Vec3;

public interface RidePassenger {
	void cobblemon$setRideXRot(float rideXRot);
	float cobblemon$getRideXRot();
	void cobblemon$setRideYRot(float rideYRot);
	float cobblemon$getRideYRot();
	void cobblemon$setRideEyePos(Vec3 rideEyePos);
	Vec3 cobblemon$getRideEyePos();
}
