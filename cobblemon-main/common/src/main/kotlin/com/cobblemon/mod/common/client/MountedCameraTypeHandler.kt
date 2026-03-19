/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.client.persisted.ClientPersistedData
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity

/**
 * The purpose here is to keep track of the last camera type that was used for each riding behaviour or just
 * the last overall, then change the camera type where necessary when mounting/dismounting or switching
 * between different riding behaviours. This is dependent on a config option, but the general idea is we
 * want the camera to be whatever the player feels most comfy with without them having to fiddle with it
 * more than once.
 */
object MountedCameraTypeHandler {
    var unmountedCameraType: CameraType = CameraType.FIRST_PERSON

    fun handleMount(passenger: Entity, vehicle: PokemonEntity) {
        val mc = Minecraft.getInstance()
        val key = vehicle.ridingController?.context?.settings?.key
        if (passenger != mc.player || key == null) {
            return
        }
        unmountedCameraType = mc.options.cameraType
        val data = ClientPersistedData.ridingPerspectives
        if (Cobblemon.config.rememberRidingCamera) {
            val lastCameraTypeForKey = data.perspectives[key]
            if (lastCameraTypeForKey != null) {
                mc.options.cameraType = lastCameraTypeForKey
            }
        }
    }

    fun handleDismount(passenger: Entity, vehicle: PokemonEntity) {
        val mc = Minecraft.getInstance()
        val key = vehicle.ridingController?.context?.settings?.key
        if (passenger != mc.player || key == null) {
            return
        }
        val currentCameraType = mc.options.cameraType
        if (currentCameraType != unmountedCameraType) {
            mc.options.cameraType = unmountedCameraType
        }
        val data = ClientPersistedData.ridingPerspectives
        data.updatePerspective(key, currentCameraType)
    }

    fun handleTransition(passenger: Entity, fromKey: ResourceLocation, toKey: ResourceLocation) {
        val mc = Minecraft.getInstance()
        if (passenger != mc.player) {
            return
        }
        val currentCameraType = mc.options.cameraType
        val data = ClientPersistedData.ridingPerspectives
        data.updatePerspective(fromKey, currentCameraType)
        if (Cobblemon.config.rememberRidingCamera) {
            val lastCameraTypeForKey = data.perspectives[toKey]
            if (lastCameraTypeForKey != null) {
                mc.options.cameraType = lastCameraTypeForKey
            }
        }
    }
}