/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.api.berry.Berries
import com.cobblemon.mod.common.block.PokeSnackBlock
import com.cobblemon.mod.common.block.PokeSnackBlock.Companion.CANDLE
import com.cobblemon.mod.common.block.PokeSnackBlock.Companion.getCandleById
import com.cobblemon.mod.common.block.entity.PokeSnackBlockEntity
import com.cobblemon.mod.common.client.render.atlas.CobblemonAtlases.BERRY_SPRITE_ATLAS
import com.cobblemon.mod.common.client.render.models.blockbench.repository.BerryModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.setPosition
import com.cobblemon.mod.common.util.math.geometry.Axis
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Direction
import net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING
import net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT

open class PokeSnackBlockEntityRenderer(ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<PokeSnackBlockEntity> {
    override fun render(blockEntity: PokeSnackBlockEntity, partialTick: Float, poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        poseStack.pushPose()

        val facing = blockEntity.blockState.getValue(FACING)
        val rotationAngle = when (facing) {
            Direction.NORTH -> 180F
            Direction.SOUTH -> 0F
            Direction.WEST -> 90F
            Direction.EAST -> -90F
            else -> 0f
        }

        poseStack.translate(0.5, 0.5, 0.5)
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotationAngle))
        poseStack.translate(-0.5, -0.5, -0.5)

        val hasCandle = renderCandle(blockEntity, poseStack, bufferSource, packedLight, packedOverlay)
        renderBerries(blockEntity, poseStack, bufferSource, packedLight, packedOverlay, hasCandle)

        poseStack.popPose()
    }

    fun renderCandle(blockEntity: PokeSnackBlockEntity, poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int): Boolean {
        val candleId = blockEntity.blockState.getValue(CANDLE)

        if (candleId > 0) {
            val candleBlock = getCandleById(blockEntity.blockState.getValue(CANDLE))

            poseStack.pushPose()
            poseStack.translate(0.0, PokeSnackBlock.CAKE_HEIGHT, 0.0)
            Minecraft.getInstance().blockRenderer.renderSingleBlock(
                candleBlock.defaultBlockState().setValue(LIT, blockEntity.blockState.getValue(LIT)),
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
            )
            poseStack.popPose()
            return true
        }
        return false
    }

    fun renderBerries(blockEntity: PokeSnackBlockEntity, poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int, hasCandle: Boolean) {
        // RenderType.entityCutoutNoCull for shading, as shading is absent in RenderType CobblemonRenderLayers.BERRY_LAYER
        val buffer = bufferSource.getBuffer(
            RenderType.entityCutoutNoCull(BERRY_SPRITE_ATLAS.textureAtlas.location())
        )

        val berryIngredients = (blockEntity.ingredientComponent?.ingredientIds?.take(3) ?: listOf())
            .mapNotNull { Berries.getByIdentifier(it) }

        berryIngredients.forEachIndexed { index, berry ->
            val model = BerryModelRepository.modelOf(berry.fruitModelIdentifier) ?: return

            when (berryIngredients.size) {
                1 -> {
                    val pos = berry.pokeSnackPositionings[0].position
                    val rot = berry.pokeSnackPositionings[0].rotation

                    if (hasCandle) {
                        model.setPosition(Axis.X_AXIS.ordinal, pos.x.toFloat())
                        model.setPosition(Axis.Y_AXIS.ordinal, pos.y.toFloat())
                        model.setPosition(Axis.Z_AXIS.ordinal, pos.z.toFloat())

                        model.setRotation(
                            Math.toRadians(rot.x).toFloat(),
                            Math.toRadians(rot.y).toFloat(),
                            Math.toRadians(rot.z).toFloat()
                        )
                    } else {
                        model.setPosition(Axis.X_AXIS.ordinal, 8F)
                        model.setPosition(Axis.Y_AXIS.ordinal, pos.y.toFloat())
                        model.setPosition(Axis.Z_AXIS.ordinal, 8F)

                        model.setRotation(
                            Math.toRadians(180.0).toFloat(),
                            Math.toRadians(0.0).toFloat(),
                            Math.toRadians(0.0).toFloat()
                        )
                    }
                }
                2 -> {
                    val pos = berry.pokeSnackPositionings[0].position
                    val rot = berry.pokeSnackPositionings[0].rotation
                    val isFirst = index == 0

                    model.setPosition(Axis.X_AXIS.ordinal, pos.x.toFloat())
                    model.setPosition(Axis.Y_AXIS.ordinal, pos.y.toFloat())
                    model.setPosition(Axis.Z_AXIS.ordinal, if (isFirst) pos.z.toFloat() else (16F - pos.z.toFloat()))

                    model.setRotation(
                        Math.toRadians(
                            if (isFirst) rot.x else (180.0 + (180.0 - rot.x))
                        ).toFloat(),
                        Math.toRadians(rot.y).toFloat(),
                        Math.toRadians(rot.z).toFloat()
                    )
                }
                else -> {
                    val pos = berry.pokeSnackPositionings[index].position
                    val rot = berry.pokeSnackPositionings[index].rotation

                    model.setPosition(Axis.X_AXIS.ordinal, pos.x.toFloat())
                    model.setPosition(Axis.Y_AXIS.ordinal, pos.y.toFloat())
                    model.setPosition(Axis.Z_AXIS.ordinal, pos.z.toFloat())

                    model.setRotation(
                        Math.toRadians(rot.x).toFloat(),
                        Math.toRadians(rot.y).toFloat(),
                        Math.toRadians(rot.z).toFloat()
                    )
                }
            }

            model.render(poseStack, buffer, packedLight, packedOverlay)
        }
    }
}
