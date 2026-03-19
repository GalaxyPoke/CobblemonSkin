/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue.input

import com.cobblemon.mod.common.api.dialogue.DialogueAction
import com.cobblemon.mod.common.api.dialogue.DialoguePredicate
import com.cobblemon.mod.common.api.dialogue.DialogueText
import com.cobblemon.mod.common.api.dialogue.FunctionDialogueAction
import com.cobblemon.mod.common.api.dialogue.FunctionDialoguePredicate
import com.cobblemon.mod.common.api.dialogue.WrappedDialogueText

/**
 * A choosable option as part of a [DialogueOptionSetInput].
 *
 * @author Hiroku
 * @since December 27th, 2023
 */
class DialogueOption(
    var text: DialogueText = WrappedDialogueText(),
    /** The value is a unique identifier so the client can tell the server what they selected. Don't forget this. */
    var value: String = "",
    /** The action to run when they click it. */
    var action: DialogueAction = FunctionDialogueAction { _, _ -> },
    val isVisible: DialoguePredicate = FunctionDialoguePredicate(),
    val isSelectable: DialoguePredicate = FunctionDialoguePredicate()
)