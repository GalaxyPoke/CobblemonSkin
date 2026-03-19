/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.math.geometry

private const val RADIAN_IN_DEGREES = 57.2958f

fun Number.toRadians(): Float = this.toFloat() / RADIAN_IN_DEGREES
fun Number.toDegrees(): Float = this.toFloat() * RADIAN_IN_DEGREES