/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue.input

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.dialogue.DialogueAction
import com.cobblemon.mod.common.api.dialogue.FunctionDialogueAction

/**
 * Input that has no input but has an action that will run after a delay.
 *
 * @author Hiroku
 * @since December 27th, 2023
 */
class DialogueAutoContinueInput : DialogueInput {
    val delay = 5F
    /** Whether the player can click to move on in a hurry or if they must wait. */
    var allowSkip = true
    var showTimer = false
    val action: DialogueAction = FunctionDialogueAction { dialogue, _ -> dialogue.incrementPage() }

    override var timeout: DialogueTimeout?
        get() = DialogueTimeout(duration = delay, showTimer = showTimer, action)
        set(_) {}

    override fun toMoLangStruct(activeInput: ActiveInput) = QueryStruct(hashMapOf())


    override fun handle(activeInput: ActiveInput, value: String) {
        if (!allowSkip) {
            return Cobblemon.LOGGER.warn("A no-skip dialogue received input from ${activeInput.activeDialogue.playerEntity.gameProfile.name}, is this person a hacker or something")
        }

        action.invoke(activeInput.activeDialogue)
    }
}