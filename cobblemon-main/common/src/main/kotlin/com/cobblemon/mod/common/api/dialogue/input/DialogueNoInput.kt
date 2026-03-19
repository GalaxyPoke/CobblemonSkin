/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue.input

import com.bedrockk.molang.runtime.struct.VariableStruct
import com.cobblemon.mod.common.api.dialogue.DialogueAction
import com.cobblemon.mod.common.api.dialogue.FunctionDialogueAction

/**
 * An input type that's really just waiting for the player to click or something.
 *
 * @author Hiroku
 * @since December 27th, 2023

 */
class DialogueNoInput(
    var action: DialogueAction = FunctionDialogueAction { dialogue, _ -> dialogue.incrementPage() }
) : DialogueInput {
    override var timeout: DialogueTimeout?
        get() = null
        set(_) {}

    override fun toMoLangStruct(activeInput: ActiveInput) = VariableStruct()
    override fun handle(activeInput: ActiveInput, value: String) {
        action(activeInput.activeDialogue)
    }
}