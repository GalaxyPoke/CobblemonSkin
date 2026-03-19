/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui

import com.cobblemon.mod.common.api.gui.renderSprite
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.client.render.SpriteType
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.util.math.toEulerXYZDegrees
import com.cobblemon.mod.common.util.toHex
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.atan

fun drawProfilePokemon(
    renderablePokemon: RenderablePokemon,
    matrixStack: PoseStack,
    rotation: Quaternionf,
    poseType: PoseType = PoseType.PROFILE,
    state: PosableState,
    partialTicks: Float,
    scale: Float = 20F,
    applyProfileTransform: Boolean = true,
    applyBaseScale: Boolean = false,
    r: Float = 1F,
    g: Float = 1F,
    b: Float = 1F,
    a: Float = 1F,
    headYaw: Float = 0f,
    headPitch: Float = 0f,
    blockLight: Int = 13
) = drawProfilePokemon(
    species = renderablePokemon.species.resourceIdentifier,
    matrixStack = matrixStack,
    rotation = rotation,
    poseType = poseType,
    state = state.also { it.currentAspects = renderablePokemon.aspects },
    partialTicks = partialTicks,
    scale = scale,
    applyProfileTransform = applyProfileTransform,
    applyBaseScale = applyBaseScale,
    r = r,
    g = g,
    b = b,
    a = a,
    headYaw = headYaw,
    headPitch = headPitch,
    blockLight = blockLight
)

fun drawProfilePokemon(
    species: ResourceLocation,
    matrixStack: PoseStack,
    rotation: Quaternionf,
    poseType: PoseType = PoseType.PROFILE,
    state: PosableState,
    partialTicks: Float,
    scale: Float = 20F,
    applyProfileTransform: Boolean = true,
    applyBaseScale: Boolean = false,
    doQuirks: Boolean = false,
    r: Float = 1F,
    g: Float = 1F,
    b: Float = 1F,
    a: Float = 1F,
    headYaw: Float = 0f,
    headPitch: Float = 0f,
    blockLight: Int = 13
) {
    RenderSystem.applyModelViewMatrix()
    matrixStack.scale(scale, scale, -scale)

    val sprite = VaryingModelRepository.getSprite(species, state, SpriteType.PROFILE)

    if (sprite == null) {

        val model = VaryingModelRepository.getPoser(species, state)
        val texture = VaryingModelRepository.getTexture(species, state)

        val context = RenderContext()
        model.context = context
        VaryingModelRepository.getTextureNoSubstitute(species, state).let { context.put(RenderContext.TEXTURE, it) }
        val baseScale = PokemonSpecies.getByIdentifier(species)!!.getForm(state.currentAspects).baseScale
        context.put(RenderContext.SCALE, baseScale)
        context.put(RenderContext.SPECIES, species)
        context.put(RenderContext.ASPECTS, state.currentAspects)
        context.put(RenderContext.RENDER_STATE, RenderContext.RenderState.PROFILE)
        context.put(RenderContext.POSABLE_STATE, state)
        context.put(RenderContext.DO_QUIRKS, doQuirks)

        state.currentModel = model

        val renderType = RenderType.entityCutout(texture)

        state.setPoseToFirstSuitable(poseType)
        state.updatePartialTicks(partialTicks)

        if (applyProfileTransform) {
            matrixStack.translate(
                model.profileTranslation.x,
                model.profileTranslation.y + 1.5 * model.profileScale,
                model.profileTranslation.z - 4.0
            )
            matrixStack.scale(model.profileScale, model.profileScale, 1 / model.profileScale)} else {
            matrixStack.translate(0F, 0F, -4.0F)
            if (applyBaseScale) {
                matrixStack.scale(baseScale, baseScale, 1 / baseScale)
            }
        }
        matrixStack.mulPose(rotation)

        model.applyAnimations(null, state, 0F, 0F, 0F, headYaw, headPitch)

        Lighting.setupForEntityInInventory()
        val entityRenderDispatcher = Minecraft.getInstance().entityRenderDispatcher
        rotation.conjugate()
        entityRenderDispatcher.overrideCameraOrientation(rotation)
        entityRenderDispatcher.setRenderShadow(true)

        val bufferSource = Minecraft.getInstance().renderBuffers().bufferSource()
        val buffer = bufferSource.getBuffer(renderType)
        val light1 = Vector3f(-1F, 1F, 1.0F)
        val light2 = Vector3f(1.3F, -1F, 1.0F)
        RenderSystem.setShaderLights(light1, light2)
        val packedLight = LightTexture.pack(blockLight, 0)

        val colour = toHex(r, g, b, a)
        model.withLayerContext(bufferSource, state, VaryingModelRepository.getLayers(species, state)) {
            model.render(context, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, colour)
            bufferSource.endBatch()
        }
        model.setDefault()
        entityRenderDispatcher.setRenderShadow(true)
        Lighting.setupFor3DItems()
    } else {
        renderSprite(matrixStack, sprite)
    }
}

const val HEAD_YAW_FACTOR = 40f
const val HEAD_PITCH_FACTOR = 20f
const val SMOOTHING_FACTOR = 0.05f

fun calculateHeadYawAndPitch(
    xCoordinate: Float,
    yCoordinate: Float,
    rotation: Quaternionf,
    mouseX: Int,
    mouseY: Int,
    currentYaw: Float,
    currentPitch: Float,
    resetToCenter: Boolean,
): Pair<Float, Float> {
    val horizontalAngle = atan(((xCoordinate - mouseX) / 40.0f).toDouble()).toFloat()
    val verticalAngle = atan(((yCoordinate - mouseY) / 40.0f).toDouble()).toFloat()

    val eulerXYZRotationDegrees = rotation.toEulerXYZDegrees()
    val targetYaw = if (!resetToCenter) horizontalAngle * HEAD_YAW_FACTOR - eulerXYZRotationDegrees.y else 0f
    val targetPitch = if (!resetToCenter) -verticalAngle * HEAD_PITCH_FACTOR - eulerXYZRotationDegrees.x else 0f

    val newYaw = Mth.rotLerp(SMOOTHING_FACTOR, currentYaw, targetYaw)
    val newPitch = Mth.rotLerp(SMOOTHING_FACTOR, currentPitch, targetPitch)

    return Pair(newYaw, newPitch)
}