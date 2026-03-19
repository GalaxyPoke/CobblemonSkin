/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battles.interpreter

import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction

/**
 * An exception thrown when properties from a [BattleMessage] cannot be interpreted for an [InterpreterInstruction].
 *
 * @author Segfault Guy
 * @since November 10th, 2024
 */
class InvalidInstructionException(battleMessage: BattleMessage) : Exception("Failed to interpret ${battleMessage.rawMessage}")