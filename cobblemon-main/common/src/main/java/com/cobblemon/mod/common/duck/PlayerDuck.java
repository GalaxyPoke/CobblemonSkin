/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.duck;

import org.joml.Vector3f;

public interface PlayerDuck {
	void setDriverInput(Vector3f driverInput);
	Vector3f getDriverInput();
	void setLastSentDriverInput(Vector3f driverInput);
	Vector3f getLastSentDriverInput();
}
