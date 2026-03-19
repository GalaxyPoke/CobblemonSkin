/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.entity

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.entity.NPCSideDelegate
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.client.ClientMoLangFunctions
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.entity.npc.NPCEntity.Companion.HITBOX_EYES_HEIGHT
import com.cobblemon.mod.common.entity.npc.NPCEntity.Companion.HITBOX_HEIGHT
import com.cobblemon.mod.common.entity.npc.NPCEntity.Companion.HITBOX_SCALE
import com.cobblemon.mod.common.entity.npc.NPCEntity.Companion.HITBOX_WIDTH
import com.cobblemon.mod.common.entity.npc.NPCPlayerModelType
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.network.syncher.EntityDataAccessor

class NPCClientDelegate : PosableState(), NPCSideDelegate {
    lateinit var npcEntity: NPCEntity
    override val schedulingTracker
        get() = npcEntity.schedulingTracker
    override fun initialize(entity: NPCEntity) {
        this.npcEntity = entity
        this.age = entity.tickCount
    }

    override fun tick(entity: NPCEntity) {
        super.tick(entity)
        incrementAge(entity)
    }

    override fun onSyncedDataUpdated(data: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(data)
        if (data == NPCEntity.ASPECTS) {
            currentAspects = getEntity().entityData.get(NPCEntity.ASPECTS)
        } else if (data == NPCEntity.NPC_PLAYER_TEXTURE) {
            val currentTexture = getEntity().entityData.get(NPCEntity.NPC_PLAYER_TEXTURE)
            val textureResource = cobblemonResource(npcEntity.uuid.toString())
            if (currentTexture.model != NPCPlayerModelType.NONE) {
                Minecraft.getInstance().textureManager.register(textureResource, DynamicTexture(NativeImage.read(currentTexture.texture)))
                runtime.environment.setSimpleVariable("texture", StringValue(textureResource.toString()))
            }
        } else if (data == HITBOX_EYES_HEIGHT) {
            /*
             * The logic with all these hitbox updates here is a bit confusing. The issue is
             * that the hitbox on NPCEntity is a thing that can be left null to always rely
             * on the NPC class's definition of the hitbox, which can be useful if the goal is
             * to change the hitbox or class at some point and not have to manually update the
             * hitbox. However, we also need something being synced to the client for customized
             * hitbox properties so the client can recreate it. I landed on all this as the solution.
             *
             * This particular part, with the 3 properties, is making sure that we trigger the client
             * side update to the npcEntity.hitbox property which triggers a hitbox refresh.
             */
            npcEntity.hitboxEyesHeight = npcEntity.entityData.get(HITBOX_EYES_HEIGHT)
        } else if (data == HITBOX_HEIGHT) {
            npcEntity.hitboxHeight = npcEntity.entityData.get(HITBOX_HEIGHT)
        } else if (data == HITBOX_WIDTH) {
            npcEntity.hitboxWidth = npcEntity.entityData.get(HITBOX_WIDTH)
        } else if (data == HITBOX_SCALE) {
            npcEntity.refreshDimensions()
        }
    }

    override fun getEntity() = npcEntity

    override fun updatePartialTicks(partialTicks: Float) {
        this.currentPartialTicks = partialTicks
    }

    override fun addToStruct(struct: QueryStruct) {
        super.addToStruct(struct)
        struct.addFunctions(ClientMoLangFunctions.clientFunctions)
        struct.addFunctions(functions.functions)
        runtime.environment.query.addFunctions(struct.functions)
    }
}