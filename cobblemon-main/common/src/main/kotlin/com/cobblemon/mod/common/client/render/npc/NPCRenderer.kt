/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.npc

import com.cobblemon.mod.common.client.entity.NPCClientDelegate
import com.cobblemon.mod.common.client.render.item.HeldItemRenderer
import com.cobblemon.mod.common.client.render.models.blockbench.PosableEntityModel
import com.cobblemon.mod.common.client.render.models.blockbench.npc.PosableNPCModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.resources.ResourceLocation
import kotlin.math.min

class NPCRenderer(context: Context) : LivingEntityRenderer<NPCEntity, PosableEntityModel<NPCEntity>>(context, PosableNPCModel(), 0.5f) {
    override fun getTextureLocation(entity: NPCEntity): ResourceLocation {
        return VaryingModelRepository.getTexture(entity.resourceIdentifier, (entity.delegate as NPCClientDelegate))
    }

    private val heldItemRenderer = HeldItemRenderer()

    override fun scale(livingEntity: NPCEntity, poseStack: PoseStack, partialTickTime: Float) {
        poseStack.scale(livingEntity.renderScale, livingEntity.renderScale, livingEntity.renderScale)
    }

    override fun render(
        entity: NPCEntity,
        entityYaw: Float,
        partialTicks: Float,
        poseMatrix: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        val aspects = entity.aspects
        val clientDelegate = entity.delegate as NPCClientDelegate
        clientDelegate.currentAspects = aspects
        shadowRadius = min((entity.boundingBox.maxX - entity.boundingBox.minX), (entity.boundingBox.maxZ) - (entity.boundingBox.minZ)).toFloat() / 1.5F
        val model = VaryingModelRepository.getPoser(entity.resourceIdentifier, clientDelegate)
        this.model.posableModel = model
        model.context = this.model.context
        this.model.setupEntityTypeContext(entity)
        this.model.context.put(RenderContext.TEXTURE, getTextureLocation(entity))
        clientDelegate.updatePartialTicks(partialTicks)

        model.setLayerContext(buffer, clientDelegate, VaryingModelRepository.getLayers(entity.resourceIdentifier, clientDelegate))

        poseMatrix.pushPose()
        poseMatrix.scale(entity.npc.modelScale, entity.npc.modelScale, entity.npc.modelScale)
        super.render(entity, entityYaw, partialTicks, poseMatrix, buffer, packedLight)
        poseMatrix.popPose()
        model.red = 1F
        model.green = 1F
        model.blue = 1F
        model.resetLayerContext()

        if (entity.deathTime < 1) {
            //Render Held Item
            heldItemRenderer.renderOnModel(
                entity.mainHandItem,
                clientDelegate,
                poseMatrix,
                buffer,
                packedLight,
                false,
                entity
            )
        }

//        if (this.shouldRenderLabel(entity)) {
//            this.renderLabelIfPresent(entity, entity.displayName, poseMatrix, buffer, packedLight)
//        }
    }

    override fun shouldShowName(entity: NPCEntity): Boolean {
        if (entity.hideNameTag) {
            return false
        }
        return super.shouldShowName(entity)
    }
}