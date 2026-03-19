/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.reactive

/**
 * A specific exception that allows canceled transformations to occur in pipes.
 *
 * @author Hiroku
 * @since November 26th, 2021
 */
class NoTransformThrowable(val terminate: Boolean) : Throwable()