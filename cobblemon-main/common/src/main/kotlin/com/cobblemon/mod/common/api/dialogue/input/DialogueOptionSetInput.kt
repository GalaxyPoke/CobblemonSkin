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
import com.cobblemon.mod.common.api.dialogue.ActiveDialogue

/**
 * A type of [DialogueInput] that has the player choose from a list of options.
 *
 * @author Hiroku
 * @since December 27th, 2023
 */
class DialogueOptionSetInput(
    var options: MutableList<DialogueOption>,
    override var timeout: DialogueTimeout? = null,
    /** Whether the buttons should be stacked vertically. */
    var vertical: Boolean = false,
) : DialogueInput {

    // JSON uses this. Basically so that the main constructor can force people to specify options
    constructor() : this(mutableListOf(), null, false)

    fun getVisibleOptions(activeDialogue: ActiveDialogue) = options.filter { it.isVisible(activeDialogue) }
    override fun toMoLangStruct(activeInput: ActiveInput) = QueryStruct(hashMapOf())
    override fun handle(activeInput: ActiveInput, value: String) {
        val option = options.firstOrNull { it.value == value }
        if (option != null) {
            if (!option.isSelectable(activeInput.activeDialogue)) {
                Cobblemon.LOGGER.warn("Dialogue option $value is not selectable but ${activeInput.activeDialogue.playerEntity.gameProfile.name} selected it anyway")
                activeInput.activeDialogue.close()
            } else if (!option.isVisible(activeInput.activeDialogue)) {
                Cobblemon.LOGGER.warn("Dialogue option $value is not visible but ${activeInput.activeDialogue.playerEntity.gameProfile.name} selected it anyway")
                activeInput.activeDialogue.close()
            }

            option.action(activeInput.activeDialogue, value)
        } else {
            Cobblemon.LOGGER.warn("No option with value $value found in dialogue option set")
        }
    }
}