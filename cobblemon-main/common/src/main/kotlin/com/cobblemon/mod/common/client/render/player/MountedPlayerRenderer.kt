/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.player

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.riding.RidingAnimation
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockAnimationRepository
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.model.AnimationUtils
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.phys.Vec3

/**
 * @author landonjw
 */
object MountedPlayerRenderer {
    @JvmField
    var shouldApplyRootAnimation = false

    @JvmField
    val relevantPartsByName = mutableMapOf(
        "body" to ModelPart(emptyList(), emptyMap()),
    )

    val defaultAnimations = listOf(
        RidingAnimation(
            fileName = "player",
            animationName = "animation.player.default_ride_pose"
        )
    )

    fun animate(
        model: HumanoidModel<AbstractClientPlayer>,
        pokemonEntity: PokemonEntity,
        player: AbstractClientPlayer,
        headYaw: Float,
        headPitch: Float,
        ageInTicks: Float,
        limbSwing: Float,
        limbSwingAmount: Float
    ) {
        // We need to reset the root because it's a synthetic part, Minecraft won't do this for us.
        val root = relevantPartsByName["body"] ?: return
        root.x = 0F
        root.y = 0F
        root.z = 0F
        root.xRot = 0F
        root.yRot = 0F
        root.zRot = 0F

        val seatIndex = pokemonEntity.passengers.indexOf(player).takeIf { it != -1 && it < pokemonEntity.seats.size } ?: return
        val seat = pokemonEntity.seats[seatIndex]
        val animations = seat.poseAnimations
            ?.firstOrNull { it.poseTypes.isEmpty() || it.poseTypes.contains(pokemonEntity.getCurrentPoseType()) }?.animations
            ?: defaultAnimations
        val state = pokemonEntity.delegate as PokemonClientDelegate

        relevantPartsByName.values.forEach {
            it.xRot = 0F
            it.yRot = 0F
            it.zRot = 0F
        }

        state.runtime.environment.setSimpleVariable("age_in_ticks", DoubleValue(ageInTicks.toDouble()))
        state.runtime.environment.setSimpleVariable("limb_swing", DoubleValue(limbSwing.toDouble()))
        state.runtime.environment.setSimpleVariable("limb_swing_amount", DoubleValue(limbSwingAmount.toDouble()))
        state.runtime.environment.setSimpleVariable("head_yaw", DoubleValue(headYaw.toDouble()))
        state.runtime.environment.setSimpleVariable("head_pitch", DoubleValue(headPitch.toDouble()))

        // TODO add the q.player reference, requires a cached thing or something on the client idk

        for (animation in animations) {
            val bedrockAnimation = BedrockAnimationRepository.getAnimationOrNull(animation.fileName, animation.animationName) ?: continue
            bedrockAnimation.run(
                context = null,
                relevantPartsByName = relevantPartsByName,
                rootPart = null,
                state = state,
                animationSeconds = state.animationSeconds,
                limbSwing = limbSwing,
                limbSwingAmount = limbSwingAmount,
                ageInTicks = ageInTicks,
                intensity = 1F
            )
        }

        applyRegularAnimation(model, player, ageInTicks)
    }

    /**
     * This does what Minecraft would typically do for various additional body part
     * rendering thingies. It's largely copy paste.
     */
    fun applyRegularAnimation(
        model: HumanoidModel<AbstractClientPlayer>,
        player: AbstractClientPlayer,
        ageInTicks: Float
    ) {
        val rightHanded = player.mainArm == HumanoidArm.RIGHT
        if (player.isUsingItem) {
            val usingMainHand = player.usedItemHand == InteractionHand.MAIN_HAND
            if (usingMainHand == rightHanded) {
                model.poseRightArm(player)
            } else {
                model.poseLeftArm(player)
            }
        } else {
            val offHandIsTwoHanded: Boolean = if (rightHanded) model.leftArmPose.isTwoHanded else model.rightArmPose.isTwoHanded
            if (rightHanded != offHandIsTwoHanded) {
                model.poseLeftArm(player)
                model.poseRightArm(player)
            } else {
                model.poseRightArm(player)
                model.poseLeftArm(player)
            }
        }

        model.setupAttackAnimation(player, ageInTicks)

        if (model.rightArmPose != HumanoidModel.ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(model.rightArm, ageInTicks, 1.0F);
        }

        if (model.leftArmPose != HumanoidModel.ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(model.leftArm, ageInTicks, -1.0F);
        }

        model.hat.copyFrom(model.head)
    }

    const val RIDING_SITTING_OFFSET = -10.0
    fun animateRoot(stack: PoseStack) {
        val root = relevantPartsByName["body"] ?: return
        val rootOffset = Vec3(root.x.toDouble(), root.y.toDouble() + RIDING_SITTING_OFFSET, root.z.toDouble()).scale(1/16.0)
        val rotation = Vec3(root.xRot.toDouble(), root.yRot.toDouble(), root.zRot.toDouble())
        stack.mulPose(Axis.ZP.rotation(rotation.z.toFloat()))
        stack.mulPose(Axis.YP.rotation(rotation.y.toFloat()))
        stack.mulPose(Axis.XP.rotation(rotation.x.toFloat()))
        stack.translate(rootOffset.x.toFloat(), rootOffset.y.toFloat(), rootOffset.z.toFloat())
    }
}
