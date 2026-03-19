/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue.input

import com.bedrockk.molang.runtime.struct.MoStruct
import com.cobblemon.mod.common.api.dialogue.DialoguePage

/**
 * A type of input to [DialoguePage]s. This is sealed because the client needs to know about it. Sorry.
 *
 * @author Hiroku
 * @since December 27th, 2023
 */
sealed interface DialogueInput {
    var timeout: DialogueTimeout?
    fun toMoLangStruct(activeInput: ActiveInput): MoStruct
    fun handle(activeInput: ActiveInput, value: String)
}