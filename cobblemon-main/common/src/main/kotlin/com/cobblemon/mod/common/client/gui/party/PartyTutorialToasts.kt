/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.party

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.text.darkGray
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.toast.CobblemonToast
import com.cobblemon.mod.common.client.keybind.boundKey
import com.cobblemon.mod.common.client.keybind.keybinds.DownShiftPartyBinding
import com.cobblemon.mod.common.client.keybind.keybinds.PartySendBinding
import com.cobblemon.mod.common.client.keybind.keybinds.UpShiftPartyBinding
import com.cobblemon.mod.common.net.messages.server.storage.party.PartyTutorialPacket
import com.cobblemon.mod.common.util.lang
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.Toast
import net.minecraft.client.gui.components.toasts.AdvancementToast
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth

object PartyTutorialToasts {

    private var toastAttached = false

    val upKey = UpShiftPartyBinding.boundKey()
    val downKey = DownShiftPartyBinding.boundKey()

    val upDisplay =
        if (upKey.type == InputConstants.Type.KEYSYM && upKey.value == InputConstants.KEY_UP)
            Component.literal("↑")
        else upKey.displayName

    val downDisplay =
        if (downKey.type == InputConstants.Type.KEYSYM && downKey.value == InputConstants.KEY_DOWN)
            Component.literal("↓")
        else downKey.displayName

    private val toast = CobblemonToast(
        Mth.createInsecureUUID(),
        CobblemonItems.POKEDEX_RED.defaultInstance,
        lang("ui.party.party_select_title").red(),
        lang(
            "ui.party.party_select_description",
            upDisplay,
            downDisplay,
            PartySendBinding.boundKey().displayName
        ).darkGray(),
        AdvancementToast.BACKGROUND_SPRITE,
        -1F,
        0
    )

    fun reset() {
        toastAttached = false
    }

    fun onOverlayRender(partySize: Int) {
        if (partySize <= 1) return

        if (!CobblemonClient.clientPlayerData.partySelectTutorialDone) {
            showToast()
        }
    }

    fun onArrowKeySwitchedSlot() {
        if (CobblemonClient.clientPlayerData.partySelectTutorialDone) return
        if (CobblemonClient.storage.party.count { it != null } < 2) return
        PartyTutorialPacket().sendToServer()
        hideToast()
    }

    fun onHoldKeyScrollSwitchedSlot() {
        if (CobblemonClient.clientPlayerData.partySelectTutorialDone) return
        if (CobblemonClient.storage.party.count { it != null } < 2) return
        PartyTutorialPacket().sendToServer()
        hideToast()
    }

    private fun showToast() {
        val minecraft = Minecraft.getInstance()
        if (!toastAttached) {
            toast.nextVisibility = Toast.Visibility.SHOW
            minecraft.toasts.addToast(toast)
            toastAttached = true
        }
    }

    private fun hideToast() {
        if (toastAttached) toast.nextVisibility = Toast.Visibility.HIDE
    }
}